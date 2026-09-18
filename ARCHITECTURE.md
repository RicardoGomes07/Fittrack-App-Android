# FitTrack — Architecture

Reference for the current implementation. Describes what exists in the codebase today, not the
roadmap. See `README.md` for the feature-facing overview and `ARCHITECTURE_DIAGRAM.md` for diagrams.

---

## 1. Overview

FitTrack is a **single-module Android application** (`:app`) built with Jetpack Compose. It is
**fully offline** — there is no network layer, no API client, and no remote sync. Room is the only
source of truth.

The app follows **MVVM with a repository layer**:

```
MainActivity
  └─ FitTrackTheme
       └─ AppNavHost ......................... Compose Navigation, routes from the Screen enum
            └─ XScreen (stateful) ............ injects a ViewModel via koinViewModel()
                 └─ XContent (stateless) ..... pure UI, @Preview-able
                      ↑
                   StateFlow
                      │
            ViewModel (androidx.lifecycle.ViewModel)
                      │
              Repository (thin DAO wrapper)
                      │
                   DAO (Room)
                      │
            FitTrackDatabase (SQLite, v7)
```

**Layer rules as implemented:**

- Room entities *are* the domain model. There is no separate DTO/domain split — `Exercise`, `User`,
  `Workout`, `WorkoutSession` and `WorkoutSet` are annotated `@Entity` and are passed all the way up
  to the UI.
- Repositories are thin pass-throughs over DAOs. They hold no caching, mapping or business logic.
- ViewModels own all presentation logic and expose `StateFlow` only.
- Composables never touch a repository or DAO directly.

**Note on package naming:** repositories live in `data/model/`, while the entities live in the
top-level `model/` package. The names are misleading; `data/model/` contains repositories.

---

## 2. Package layout

```
com.example.fittrack/
├── FitTrackApplication.kt    Application class; starts Koin
├── MainActivity.kt           Single activity; sets the Compose content
├── di/
│   └── AppModule.kt          The only Koin module (DB, DAOs, repositories, AuthManager, ViewModels)
├── data/
│   ├── AuthManager.kt        App-wide session holder
│   ├── PasswordHasher.kt     PBKDF2 hashing (JDK only, no library)
│   ├── InsertData.kt         Seed data (22 exercises) + raw-SQLite insert helper
│   ├── local/                FitTrackDatabase, 5 DAOs, Converters, Migrations (6 → 7)
│   └── model/                5 repositories (despite the package name)
├── model/                    Room entities + enums + the Screen route enum, query result types
│                             (SetHistory, SessionSummary) and ExerciseRecords (PR / 1RM logic)
└── ui/
    ├── navigation/
    │   └── AppNavHost.kt     All routes and navigation behaviour
    ├── screens/
    │   ├── auth/             LoginScreen, SignUpScreen, AuthViewModel
    │   ├── home/             HomeScreen, HomeViewModel, HomeUiState
    │   ├── exercises/        List, Detail, Add screens + 2 ViewModels
    │   ├── profile/          ProfileScreen, ProfileViewModel, ProfileUiState
    │   ├── workout/          ActiveWorkoutScreen, ActiveWorkoutViewModel, ActiveWorkoutUiState, formatting
    │   └── components/       Reusable widgets, grouped by feature (home/, exercise/, profile/)
    └── theme/                FitTrackColors, FitTrackTheme, typography
```

---

## 3. Entry points

| Entry point | File | Responsibility |
|---|---|---|
| `FitTrackApplication` | `FitTrackApplication.kt` | Declared as `android:name` in the manifest. Calls `startKoin { androidContext(...); modules(appModule) }`. Runs before any activity. |
| `MainActivity` | `MainActivity.kt` | The **only** activity. `LAUNCHER` intent filter, `windowSoftInputMode="adjustResize"`. Calls `enableEdgeToEdge()` then `setContent { FitTrackTheme { AppNavHost() } }`. |
| `AppNavHost` | `ui/navigation/AppNavHost.kt` | Declares every route. Start destination is `Screen.HOME`. |
| Room callback | `di/AppModule.kt` | `onCreate` and `onOpen` hooks that seed the exercise table (see §6). |

There are no services, broadcast receivers, content providers, or `WorkManager` jobs.

---

## 4. Main components

### Data layer

| Component | Responsibility |
|---|---|
| `FitTrackDatabase` | Room database, **version 7**, `exportSchema = false`, file name `fittrack_database`. Entities: `Exercise`, `User`, `Workout`, `WorkoutSession`, `WorkoutSet`. `workout_sessions` (FK `userId` → users, CASCADE; `startedAt`/`endedAt` epoch millis, active while `endedAt IS NULL`) and `sets` (FK `exerciseId` → exercises, RESTRICT; FK `sessionId` → sessions, CASCADE; `position`, `isCompleted`) are indexed on their foreign keys. |
| `MIGRATION_6_7` | `data/local/Migrations.kt`. Rebuilds `users` (hashes the existing plaintext password, adds `isLoggedIn`, unique `nickname`, `password` → `passwordHash`) and the two workout tables; `exercises` and `workouts` are untouched. Registered in `AppModule` next to `fallbackToDestructiveMigration(true)`. Its DDL was verified against Room's generated v7 schema. |
| `Converters` | Type converters for `UUID` ↔ `String`, `LocalDate` ↔ epoch-day `Long`, all enums ↔ name, and `List<UUID>` ↔ comma-joined `String`. |
| `ExerciseDao` | `getAllExercises(): Flow<List<Exercise>>`, get-by-id, insert (**ABORT** — a duplicate name throws instead of replacing the row, because logged sets now reference exercises), update, delete. |
| `UserDao` | `getLoggedInUser(): Flow<User?>` = `WHERE isLoggedIn = 1 LIMIT 1`; `getByNickname`; `insertUser` (ABORT, nickname is unique); `setLoggedInUser(id)` = `UPDATE users SET isLoggedIn = (id = :id)` (one statement switches the active account); `logout()` = `UPDATE users SET isLoggedIn = 0`. |
| `WorkoutSessionDao` | CRUD plus `getActiveSession(userId)` and `getRecentSessionSummaries(userId, limit)` (finished sessions, aggregated over *completed* sets with a `LEFT JOIN`). |
| `SetDao` | CRUD plus sets-for-session ordered by `position`, `getSetHistory(userId, exerciseId)` (completed sets joined to their session, for PRs), targeted `updateValues` / `updateCompleted`, and bulk deletes (`deleteSetsForExercise`, `deleteIncompleteSets`). |
| `WorkoutDao` | Standard CRUD. **Unused by any ViewModel** (reserved for workout templates). |
| `ExerciseRepository`, `UserRepository`, `WorkoutRepository`, `WorkoutSessionRepository`, `SetRepository` | One-line delegations to their DAO. |
| `AuthManager` | The session holder. Not a ViewModel — an app-scoped Koin `single`. `login` / `signUp` hash and verify on `Dispatchers.Default`; `signUp` returns `false` when the nickname is taken. See §5. |
| `PasswordHasher` | `hash` / `verify`, PBKDF2WithHmacSHA1, 120 000 iterations, random 16-byte salt, stored as `pbkdf2$iterations$saltHex$hashHex`, constant-time compare. SHA-1 variant because the SHA-2 variants need API 26 and `minSdk` is 24. |
| `insertInitialExercises(db)` | Seeds 22 exercises using raw `ContentValues` + `db.insert(..., CONFLICT_IGNORE, ...)` inside an explicit transaction. The Room callback receives a `SupportSQLiteDatabase`, so the seed uses raw SQL instead of DAOs. |

### Presentation layer

| ViewModel | Exposes | Notes |
|---|---|---|
| `HomeViewModel` | `uiState: StateFlow<HomeUiState>` | Formats the date label, picks a random motivation message, mirrors auth state, and follows `currentUser` → `hasActiveWorkout` + the 5 most recent finished sessions (`SessionSummary`). `streakDays` is still hardcoded to `0`. |
| `ExercisesViewModel` | `exercises: StateFlow<List<Exercise>>`, `loggedInUser` | Also serves `ExerciseAddScreen` via `addExercise(ExerciseInput)`. |
| `ExerciseDetailViewModel` | `exercise: StateFlow<Exercise?>`, `records: StateFlow<ExerciseRecords?>`, `loggedInUser` | `loadExercise(id: String)` parses the UUID from the route argument. `records` = `combine(exercise, currentUser)` → `SetRepository.getSetHistory` → `toExerciseRecords()`. |
| `ActiveWorkoutViewModel` | `uiState: StateFlow<ActiveWorkoutUiState>` | Live workout. `uiState` combines `isInitialized`, the user's active session, its sets, all exercises, and a 1 s ticker + `restEndsAt`. Writes go straight to Room (session survives process death; elapsed time is derived from the stored `startedAt`). The rest timer (default 90 s, ±15 s) is in-memory. Actions: `startWorkout`, `addExercise` / `addExerciseFromRoute`, `addSet` (prefilled from the exercise's previous set), `updateSetValues`, `toggleSetCompleted` (starts the rest timer), `deleteSet`, `removeExercise`, `adjustRest`, `skipRest`, `finishWorkout`, `discardWorkout`. |
| `ProfileViewModel` | `uiState`, `loggedInUser`, `isInitialized` | `combine(currentUser, _unitSystem)` → `User.toProfileUiState()`. Owns `toggleUnitSystem()` and `logout()`. |
| `AuthViewModel` | `uiState: StateFlow<AuthUiState>` | `AuthUiState(isLoading, error, isSuccess)`. Screens react to `isSuccess` in a `LaunchedEffect`, then call `resetState()`. Sign-up reports "Nickname already taken"; login reports "Invalid credentials". Both screens render `error`. |

### UI conventions

Six of the eight screens are split in two (Home, Exercises, ExerciseDetails, ExerciseAdd, Profile, ActiveWorkout).
`LoginScreen` and `SignUpScreen` are the exceptions: a single composable with no `*Content` and no
previews. **Keep the split for new screens:**

```kotlin
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinViewModel()) { … }   // stateful, injects

@Composable
fun ProfileContent(state: ProfileUiState, …) { … }                       // stateless, @Preview-able
```

Colors come from the custom `FitTrackColors` object rather than `MaterialTheme.colorScheme`; the
palette is a fixed dark theme.

Logic worth unit-testing is kept in plain top-level functions so it runs on the JVM without Android:
`toExerciseRecords` / `estimateOneRepMax` (`model/ExerciseRecords.kt`), `buildExerciseLogs` /
`restRemainingSeconds` (`ActiveWorkoutUiState.kt`), `formatDuration` / `formatWeight`
(`WorkoutFormat.kt`), and `PasswordHasher`.

---

## 5. How components communicate

**Dependency injection — Koin.** `di/AppModule.kt` is the single module. The database, every DAO,
every repository and `AuthManager` are registered as `single`; ViewModels use `viewModelOf(::X)`.
Composables obtain ViewModels with `koinViewModel()`, scoped to the navigation back-stack entry.

**Reactive state — Flow → StateFlow → Compose.** Data moves in one direction only:

```
Room Flow  →  Repository  →  ViewModel.stateIn(WhileSubscribed(5000))  →  collectAsStateWithLifecycle()
```

Events move back down as lambda callbacks (`onExerciseClick`, `onNavItemSelected`, `onLogout`), never
as direct calls into another ViewModel.

**Cross-screen state — `AuthManager`.** Auth state is *not* passed through navigation arguments.
`AuthManager` is an app-scoped singleton that observes `UserRepository.loggedInUser` in its own
`CoroutineScope(SupervisorJob() + Dispatchers.Main)` and republishes three `StateFlow`s:

| Flow | Meaning |
|---|---|
| `currentUser: StateFlow<User?>` | The active user, or `null`. |
| `isLoggedIn: StateFlow<Boolean>` | `currentUser != null`. **Not read by anything outside `AuthManager`** — consumers use `currentUser`. |
| `isInitialized: StateFlow<Boolean>` | `false` until the first DB emission. Only `ProfileScreen` waits on it; Home and Exercises don't, so they render the guest state (guest card, no add-FAB) until the first emission. |

`HomeViewModel`, `ProfileViewModel`, `ExercisesViewModel`, `ExerciseDetailViewModel` and
`ActiveWorkoutViewModel` all consume `AuthManager`, so a login or logout propagates everywhere without any navigation event.

**Navigation.** Routes are the names of the `Screen` enum (`Screen.HOME.name` etc.). The bottom nav
uses its own string keys — `"dashboard"`, `"exercises"`, `"profile"` — which `AppNavHost` maps to
routes inside each screen's `onNavItemSelected` lambda. Tab switches use
`popUpTo(startDestination) { saveState = true }` + `launchSingleTop` + `restoreState`.

`ACTIVE_WORKOUT` is the one route with an optional argument: `ACTIVE_WORKOUT?exerciseId={exerciseId}`
(nullable, default null). Home navigates to it bare; the exercise detail screen passes the exercise id,
which the workout screen hands to `addExerciseFromRoute` (guarded so recomposition cannot add it twice).

---

## 6. Important data flows

### Cold start and seeding

```
Process start
  → FitTrackApplication.onCreate()  → startKoin(appModule)
  → MainActivity                    → AppNavHost, start destination HOME
  → first database access (Room opens lazily; the first query is AuthManager's users Flow)
       ├─ onCreate(db)  → insertInitialExercises(db)          // first install only
       └─ onOpen(db)    → SELECT COUNT(*) FROM exercises
                          → if 0, insertInitialExercises(db)  // self-healing re-seed
  → AuthManager begins collecting UserRepository.loggedInUser
       → isInitialized flips to true, screens render their real state
```

The `onOpen` re-seed means an empty `exercises` table is always repopulated — including after a
destructive migration.

### Exercise list → detail

```
ExerciseDao.getAllExercises(): Flow
  → ExerciseRepository.getExercises()
  → ExercisesViewModel.exercises.stateIn(WhileSubscribed(5000))
  → ExercisesScreen → ExercisesContent → ExercisesList → ExerciseItem
  → onExerciseClick(id) → navigate("EXERCISE_DETAIL/$id")
  → ExerciseDetailsScreen: LaunchedEffect(exerciseId) { viewModel.loadExercise(it) }
  → UUID.fromString(id) → ExerciseRepository.getExerciseById() → _exercise StateFlow
```

### Adding an exercise

```
ExerciseAddScreen (local remember state)
  → ExerciseInput → ExercisesViewModel.addExercise()
  → Exercise(id = UUID.randomUUID(), …) → insertExercise() → popBackStack()
```

The list refreshes automatically because it is backed by a Room `Flow`. No manual invalidation.

### Authentication

```
SignUpScreen → AuthViewModel.signUp() → AuthManager.signUp()
  → nickname already exists?  yes → returns false → AuthUiState.error = "Nickname already taken"
  → PasswordHasher.hash()  (Dispatchers.Default)
  → UserRepository.signUp(): insertUser(isLoggedIn = false), then setLoggedInUser(id)
  → getLoggedInUser() Flow re-emits → AuthManager.currentUser → every subscribed ViewModel
  → AuthUiState.isSuccess = true → LaunchedEffect navigates to PROFILE

LoginScreen → AuthViewModel.login() → AuthManager.login()
  → UserRepository.findByNickname() → PasswordHasher.verify() (Dispatchers.Default) → setLoggedInUser(id)

Logout → UserDao.logout() = UPDATE users SET isLoggedIn = 0
  → Flow emits null → screens revert to their guest state. The account and its workout history remain.
```

The session marker is the `isLoggedIn` column on the user row (no separate store, no extra dependency).

### Workout logging

```
Home "Start workout" / "Resume workout"      ─┐
Exercise detail "Add to Today's Workout"     ─┴→ navigate ACTIVE_WORKOUT[?exerciseId=…]

ActiveWorkoutScreen: LaunchedEffect(addExerciseId) → ViewModel.addExerciseFromRoute()
  → ensureSession(): the user's session with endedAt IS NULL, else insert WorkoutSession(userId, startedAt = now)
  → appendSet(): insert WorkoutSet(position = last + 1, weight/reps copied from the exercise's previous set)

Edit weight/reps  → SetDao.updateValues(id, weight, reps)   (on every change; both values come from the row)
Mark set done     → SetDao.updateCompleted(id, true), restEndsAt = now + rest duration
uiState           = combine(isInitialized, active session, its sets, all exercises, ticker + restEndsAt)
Finish            → deleteIncompleteSets(); no sets left → delete the session, else set endedAt; popBackStack
Discard           → delete the session (its sets cascade)
```

An exercise is "in" the workout only by having sets: adding one inserts its first set, and deleting its
last set removes it. There is no separate workout-exercise table.

### Records and history

```
ExerciseDetailViewModel.records
  = combine(exercise, currentUser) → SetDao.getSetHistory(userId, exerciseId) → toExerciseRecords()
  → PersonalRecordsWidget (heaviest completed set; 1RM = best Epley estimate)

HomeViewModel: currentUser → combine(getActiveSession(userId), getRecentSessionSummaries(userId, 5))
  → HomeUiState.hasActiveWorkout / recentSessions → WorkoutActivitySection
```

---

## 7. Technologies and dependencies

Versions are centralised in `gradle/libs.versions.toml` (Gradle version catalog); the build uses
Kotlin DSL and KSP.

| Area | Choice | Version |
|---|---|---|
| Build | AGP | 9.4.0 |
| Language | Kotlin | 2.2.10 |
| Annotation processing | KSP | 2.2.10-2.0.2 |
| UI | Jetpack Compose (BOM) | 2026.02.01 |
| Design system | Material 3 + `material-icons-extended` | via BOM |
| DI | Koin (`koin-android`, `koin-androidx-compose`) | 4.2.2 |
| Persistence | Room (`runtime`, `ktx`, `compiler`) | 2.7.0-alpha13 |
| Navigation | `navigation-compose` | 2.10.0 |
| Concurrency | Coroutines + Flow | via Kotlin |
| Desugaring | `desugar_jdk_libs` | 2.1.4 |
| Password hashing | `javax.crypto` PBKDF2 (platform, no library) | — |

**SDK levels:** `minSdk 24`, `targetSdk 37`, `compileSdk 37`, Java 11.

**Core library desugaring is required, not optional.** The model layer uses `java.time.LocalDate`,
which is API 26+. `isCoreLibraryDesugaringEnabled = true` is what keeps `minSdk 24` viable — do not
remove it.

**Implicit dependency:** `collectAsStateWithLifecycle` is used across all screens but
`androidx.lifecycle:lifecycle-runtime-compose` is **not declared** in `app/build.gradle.kts`; the
project compiles because other Compose libraries (`navigation-compose`, `activity-compose`) depend on
it. Declare it explicitly before it breaks.

### Testing

| Test | Scope | Status |
|---|---|---|
| `PasswordHasherTest` | JVM. Round trip, wrong password, salting, format, malformed/legacy stored values. | Runs: `./gradlew :app:testDebugUnitTest` |
| `ExerciseRecordsTest` | JVM. Epley 1RM, PR selection and tie-breaks, bodyweight, empty input. | Runs |
| `ActiveWorkoutLogicTest` | JVM. Exercise grouping/order, rest-timer rounding, duration and weight formatting. | Runs |
| `WorkoutLoggingDaoTest` | Instrumented, in-memory Room. Login switching / logout keeps the account, unique nickname, duplicate exercise rejected, FK RESTRICT / CASCADE, active session, summaries (completed sets, per user, limit, `LEFT JOIN`), set history, targeted set updates, bulk deletes. | **Compiles; not run** (no device or emulator) |
| `Migration6To7Test` | Instrumented. Builds a real v6 database by hand, opens it through Room with `MIGRATION_6_7` (Room validates the result against the v7 entities), checks the account, hash, login state and exercises survive and the new tables enforce foreign keys. | **Compiles; not run** |
| `DatabaseSeedingTest` | Instrumented. Deletes the DB in `@Before`, injects `FitTrackDatabase` via `KoinTest`, asserts the exercise table is seeded. | Pre-existing |
| `ExampleInstrumentedTest`, `ExampleUnitTest` | Unmodified project templates. | Pre-existing |

There are no ViewModel tests (repositories are concrete classes and `kotlinx-coroutines-test` is not a
dependency) and no Compose UI tests.

---

## 8. Known limitations and unfinished features

### Verification status

The workout feature and the auth change were built and unit-tested on the JVM, but **never run on a
device or emulator** (none was available). Specifically unobserved: Room's on-device validation of
`MIGRATION_6_7`, the two instrumented test classes, and every new screen at runtime. The migration's
SQL was checked separately: its statements are identical to Room's generated v7 DDL, and running them on
a hand-built v6 database (SQLite 3.46, foreign keys on) produced a structurally identical schema with the
data preserved. Treat a first run on a real device with an existing v6 install as the acceptance test.

### Authentication

Account storage and session state are separated: logout no longer deletes anything, login is reachable,
passwords are hashed, and nicknames are unique. Remaining limits:

- The session is a flag on the user row (`isLoggedIn`), so account and session still share a table.
  Fine for a single-device app; a real multi-account or sync story would want a separate store.
- Nicknames are case-sensitive (`Alex` and `alex` are different accounts). No password rules, lockout,
  change-password, profile editing or account deletion.
- PBKDF2WithHmacSHA1 at 120 000 iterations (the SHA-2 variants need API 26; `minSdk` is 24).
- `AuthManager.isLoggedIn` is still read by nothing, and its scope (`Dispatchers.Main`) is never cancelled.
- Signing up while a session exists is not reachable from the UI; if it were, the new account would
  replace the active one.

### Workout logging

Implemented: live session with elapsed and rest timers, sets with weight/reps, completion, history on
Home, and real PRs on the exercise detail screen. Limitations:

- Sets record only `reps` and `weight` (**kg only**; the Profile kg/lbs toggle does not affect these
  screens). `ExerciseType.DURATION` / `DISTANCE` exercises (creatable in the Add screen, none seeded)
  have no duration or distance field. Plank is seeded as `BODYWEIGHT` and is logged in reps.
- One active workout per user. Finished sessions cannot be edited or deleted and have no detail screen;
  Home lists only the 5 most recent summaries.
- The rest timer is in-memory (lost if the process dies), has no sound, vibration or notification, and
  its length resets to 90 s when the ViewModel is recreated.
- Leaving an empty active workout keeps it "active" (Home shows "Resume workout") until it is finished
  or discarded. Finishing keeps completed sets only, and discards a workout with none.
- New sets are prefilled from the exercise's previous set *in the same workout*, not from history.
- PR definition: the heaviest completed set (ties: more reps, then most recent); 1RM is the best Epley
  estimate over completed sets with reps > 0; bodyweight exercises show best reps. Sets count as soon as
  they are marked done, including in a workout still in progress.
- **Templates are not implemented.** `Workout` (table, DAO, repository) is still unused and its
  `exerciseIds` is still a comma-joined string; a junction table is needed first. `WorkoutSession` no
  longer has a `workoutId`. Analytics is not started (nav item still commented out in
  `FitTrackBottomNav.kt:28`).
- Workout history is stored per `userId` and cascades with the user, but there is no UI to delete an
  account, and no UI to delete an exercise (an exercise with logged sets cannot be deleted: FK RESTRICT).

### Placeholder UI not backed by data

- `ProfileUiState` statistics (`workouts`, `tonnage`, `hoursTrained`, `avgSessionMinutes`,
  `monthlyPRs`, `consistency`, `streakDays`, `prs`) are still hardcoded to `0`/empty in
  `User.toProfileUiState()`, while the Profile UI shows fixed strings such as "Updated 2h ago", "Synced
  with Pixel Watch 3" and "17:30 Gym time alert active". `isPro` defaults to `true`. The data to compute
  most of these now exists (sessions and sets) but is not wired.
- `HomeViewModel.streakDays` is still `0`.
- The kg/lbs toggle changes the label only; **no value conversion is applied**.
- Dead controls with empty `onClick`: the detail screen's video demo button, "Account Security &
  Privacy", and all `SettingsItem` rows.

### Correctness and tech debt

- **Duplicate exercise names are now rejected** (insert is ABORT, name is unique) instead of silently
  replacing the row, but the failure is swallowed: `ExercisesViewModel.addExercise` catches and discards
  it and the Add screen pops back regardless, so the user sees nothing.
- **Other exceptions are swallowed.** `ExerciseDetailViewModel.loadExercise` catches and discards; an
  invalid UUID leaves the detail screen blank with no error shown.
- **Empty is rendered as loading.** `ExercisesScreen` shows "Loading exercises…" whenever the list is
  empty.
- **`imageRes` stores an `R.drawable` int in the database.** Resource ids are not guaranteed stable
  across builds while seeded rows persist. Prefer storing the drawable *name*.
- **Destructive migrations are still enabled** as the fallback (`fallbackToDestructiveMigration(true)`,
  `exportSchema = false`). Only 6 → 7 has a real migration; any other older version wipes the data.
  Export schemas and use `MigrationTestHelper` before shipping.
- Home and Exercises do not wait for `AuthManager.isInitialized`, so they render the guest state until
  the first emission; only Profile and the workout screen do.
- **Bottom-nav selection is hardcoded per screen** rather than derived from
  `currentBackStackEntryAsState()`.
- `di/AppModule.kt` has unused imports (`ContentValues`, `SQLiteDatabase`, `R`).
- `DatabaseSeedingTest` asserts at least 13 exercises; the seed actually contains 22.
