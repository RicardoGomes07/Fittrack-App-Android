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
            FitTrackDatabase (SQLite, v6)
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
│   ├── InsertData.kt         Seed data (22 exercises) + raw-SQLite insert helper
│   ├── local/                FitTrackDatabase, 5 DAOs, Converters
│   └── model/                5 repositories (despite the package name)
├── model/                    Room entities + enums + the Screen route enum
└── ui/
    ├── navigation/
    │   └── AppNavHost.kt     All routes and navigation behaviour
    ├── screens/
    │   ├── auth/             LoginScreen, SignUpScreen, AuthViewModel
    │   ├── home/             HomeScreen, HomeViewModel, HomeUiState
    │   ├── exercises/        List, Detail, Add screens + 2 ViewModels
    │   ├── profile/          ProfileScreen, ProfileViewModel, ProfileUiState
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
| `FitTrackDatabase` | Room database, **version 6**, `exportSchema = false`, file name `fittrack_database`. Entities: `Exercise`, `User`, `Workout`, `WorkoutSession`, `WorkoutSet`. |
| `Converters` | Type converters for `UUID` ↔ `String`, `LocalDate` ↔ epoch-day `Long`, all enums ↔ name, and `List<UUID>` ↔ comma-joined `String`. |
| `ExerciseDao` | `getAllExercises(): Flow<List<Exercise>>`, get-by-id, insert (REPLACE), update, delete. |
| `UserDao` | `getLoggedInUser(): Flow<User?>` = `SELECT * FROM users LIMIT 1`; `login(nickname, password)`; `insertUser` (REPLACE); `logout()` = `DELETE FROM users`. |
| `WorkoutDao`, `WorkoutSessionDao`, `SetDao` | Standard CRUD. **Currently unused by any ViewModel.** |
| `ExerciseRepository`, `UserRepository`, `WorkoutRepository`, `WorkoutSessionRepository`, `SetRepository` | One-line delegations to their DAO. |
| `AuthManager` | The session holder. Not a ViewModel — an app-scoped Koin `single`. See §5. |
| `insertInitialExercises(db)` | Seeds 22 exercises using raw `ContentValues` + `db.insert(..., CONFLICT_IGNORE, ...)` inside an explicit transaction. The Room callback receives a `SupportSQLiteDatabase`, so the seed uses raw SQL instead of DAOs. |

### Presentation layer

| ViewModel | Exposes | Notes |
|---|---|---|
| `HomeViewModel` | `uiState: StateFlow<HomeUiState>` | Formats the date label, picks a random motivation message, mirrors auth state. `streakDays` is hardcoded to `0`. |
| `ExercisesViewModel` | `exercises: StateFlow<List<Exercise>>`, `loggedInUser` | Also serves `ExerciseAddScreen` via `addExercise(ExerciseInput)`. |
| `ExerciseDetailViewModel` | `exercise: StateFlow<Exercise?>`, `loggedInUser` | `loadExercise(id: String)` parses the UUID from the route argument. |
| `ProfileViewModel` | `uiState`, `loggedInUser`, `isInitialized` | `combine(currentUser, _unitSystem)` → `User.toProfileUiState()`. Owns `toggleUnitSystem()` and `logout()`. |
| `AuthViewModel` | `uiState: StateFlow<AuthUiState>` | `AuthUiState(isLoading, error, isSuccess)`. Screens react to `isSuccess` in a `LaunchedEffect`, then call `resetState()`. |

### UI conventions

Five of the seven screens are split in two (Home, Exercises, ExerciseDetails, ExerciseAdd, Profile).
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

`HomeViewModel`, `ProfileViewModel`, `ExercisesViewModel` and `ExerciseDetailViewModel` all consume
`AuthManager`, so a login or logout propagates everywhere without any navigation event.

**Navigation.** Routes are the names of the `Screen` enum (`Screen.HOME.name` etc.). The bottom nav
uses its own string keys — `"dashboard"`, `"exercises"`, `"profile"` — which `AppNavHost` maps to
routes inside each screen's `onNavItemSelected` lambda. Tab switches use
`popUpTo(startDestination) { saveState = true }` + `launchSingleTop` + `restoreState`.

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
SignUpScreen → AuthViewModel.signUp() → AuthManager.signUp() → UserRepository.signUp()
  → UserDao.insertUser(user)
  → users table now has a row
  → UserDao.getLoggedInUser() Flow re-emits
  → AuthManager.currentUser updates
  → every subscribed ViewModel updates
  → AuthUiState.isSuccess = true → LaunchedEffect navigates to PROFILE
```

Logout runs `DELETE FROM users`, the Flow emits `null`, and all screens revert to their guest state.

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

**SDK levels:** `minSdk 24`, `targetSdk 37`, `compileSdk 37`, Java 11.

**Core library desugaring is required, not optional.** The model layer uses `java.time.LocalDate`,
which is API 26+. `isCoreLibraryDesugaringEnabled = true` is what keeps `minSdk 24` viable — do not
remove it.

**Implicit dependency:** `collectAsStateWithLifecycle` is used across all screens but
`androidx.lifecycle:lifecycle-runtime-compose` is **not declared** in `app/build.gradle.kts`; it
is not a direct dependency: the project compiles because other Compose libraries
(`navigation-compose`, `activity-compose`) depend on it. Declare it explicitly before it breaks.

### Testing

| Test | Scope |
|---|---|
| `DatabaseSeedingTest` | Instrumented. Deletes the DB in `@Before`, injects `FitTrackDatabase` via `KoinTest`, asserts the exercise table is seeded. |
| `ExampleInstrumentedTest`, `ExampleUnitTest` | Unmodified project templates. |

There are no ViewModel, repository or `AuthManager` unit tests, and no Compose UI tests.

---

## 8. Known limitations and unfinished features

### Authentication is structurally incomplete

The `users` table serves as **both the account store and the session marker**, which makes the
implemented login flow unreachable:

- `logout()` is `DELETE FROM users`, so logging out **permanently deletes the account**.
- `getLoggedInUser()` is `SELECT * FROM users LIMIT 1`, so while any account row exists the app is
  always logged in. There is no state in which a returning user can log in, and
  `AuthManager.login()` therefore only ever fails. `LoginScreen` is effectively unreachable.
- `AuthManager.login()` calls `userRepository.signUp(user)` to "mark as logged in" — a REPLACE of the
  row it just read, i.e. a no-op.
- Passwords are stored and compared in **plaintext**.
- `nickname` has no unique index and signup performs no duplicate check.

Fixing this requires separating account storage from session storage (e.g. keep all users in the
table, persist the active user id in DataStore) and hashing passwords. **This blocks every
user-scoped feature.**

### The workout domain is scaffolding only

`Workout`, `WorkoutSession` and `WorkoutSet` have entities, DAOs and repositories registered in Koin,
but **no ViewModel or screen consumes them**. There is no session logging and no analytics; the
Analytics bottom-nav item is commented out (`ui/screens/components/home/FitTrackBottomNav.kt:28`).

The current schema cannot support the planned features as-is:

- `Workout.exerciseIds` is a comma-joined `String`, not a junction table — it cannot be queried or
  joined and has no referential integrity.
- There are **no foreign keys and no indices** on `WorkoutSet.exerciseId` / `WorkoutSet.sessionId` /
  `WorkoutSession.workoutId`, so deletes leave orphan rows.
- `WorkoutSession` has only a `LocalDate` (no start/end timestamps) and no `userId`.
- `WorkoutSet` has no RPE, no set ordering and no duration/distance fields, although
  `ExerciseType.DURATION` and `ExerciseType.DISTANCE` exist.

Redesign the schema **before** building the logging UI.

### Placeholder UI not backed by data

- `ProfileUiState` statistics (`workouts`, `tonnage`, `hoursTrained`, `avgSessionMinutes`,
  `monthlyPRs`, `consistency`, `streakDays`, `prs`) are all hardcoded to `0`/empty in
  `User.toProfileUiState()`, while the Profile UI displays fixed strings such as "Updated 2h ago",
  "Synced with Pixel Watch 3" and "17:30 Gym time alert active". `isPro` defaults to `true`.
- `PersonalRecordsWidget()` in `ExerciseDetailsScreen.kt:341` takes no parameters — its 105 kg PR,
  114 kg 1RM and "Logged Oct 18, 2024" are literals.
- The kg/lbs toggle changes the label only; **no value conversion is applied**.
- Dead controls with empty `onClick`: the detail screen's `BottomStickyBar` ("Add to Today's
  Workout", video demo), "Account Security & Privacy", and all `SettingsItem` rows.
- `HomeScreen.kt:102` — the logged-in dashboard is a bare "Your Activity" heading (`TODO`).

### Correctness issues to be aware of

- **Duplicate exercise names silently replace existing rows.** `exercises.name` carries a unique
  index and `insertExercise` uses `OnConflictStrategy.REPLACE`, but `addExercise` generates a fresh
  `UUID`. Adding an existing name deletes the old row and inserts a new id, orphaning any sets that
  referenced it. There is no duplicate check in the Add screen.
- **Exceptions are swallowed.** `ExercisesViewModel.addExercise` and
  `ExerciseDetailViewModel.loadExercise` catch and discard. An invalid UUID leaves the detail screen
  blank with no error shown.
- **Empty is rendered as loading.** `ExercisesScreen` shows "Loading exercises…" whenever the list is
  empty, so a genuinely empty table spins forever. There is no empty or error state.
- **`imageRes` stores an `R.drawable` int in the database.** Resource ids are not guaranteed stable
  across builds while seeded rows persist, so icons can break after a rebuild. Prefer storing the
  drawable *name* and resolving it at render time.
- **Destructive migrations are enabled.** `fallbackToDestructiveMigration(true)` with
  `exportSchema = false` at version 6 means every schema change wipes user data. Acceptable
  pre-release; switch to exported schemas and real migrations before shipping.
- **Bottom-nav selection is hardcoded per screen** rather than derived from
  `currentBackStackEntryAsState()`.
- `AuthManager` holds an app-lifetime scope on `Dispatchers.Main` that is never cancelled;
  `Dispatchers.Default` would be more appropriate for DB-driven work.
- `di/AppModule.kt` has unused imports (`ContentValues`, `SQLiteDatabase`, `R`).
- `DatabaseSeedingTest` asserts at least 13 exercises; the seed actually contains 22.
