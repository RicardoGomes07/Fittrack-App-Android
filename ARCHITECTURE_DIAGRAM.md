# FitTrack — Architecture Diagrams

Diagrams of the **current implementation**. Companion to [`ARCHITECTURE.md`](ARCHITECTURE.md), which
holds the details and known limitations. Written in [Mermaid](https://mermaid.js.org/) — renders on
GitHub and in Android Studio's Markdown preview.

**External services:** none. The app is fully offline (no network libraries, no `uses-permission` in
the manifest). The only persistent store is the on-device SQLite file managed by Room.

---

## 1. Components

Solid arrows are call/dependency direction (screen → ViewModel → repository → DAO). Data travels
back up as `Flow` / `StateFlow`. Dashed nodes exist in code but have **no consumer**.

```mermaid
flowchart TB
    subgraph Android["Android runtime"]
        App["FitTrackApplication"]
        Main["MainActivity"]
    end

    subgraph DI["Koin"]
        Module["appModule<br/>(di/AppModule.kt)"]
    end

    subgraph UI["UI layer - Jetpack Compose"]
        Nav["AppNavHost"]
        Home["HomeScreen"]
        Ex["ExercisesScreen"]
        Detail["ExerciseDetailsScreen"]
        Add["ExerciseAddScreen"]
        Prof["ProfileScreen"]
        Login["LoginScreen"]
        Sign["SignUpScreen"]
        Work["ActiveWorkoutScreen"]
    end

    subgraph VM["ViewModels"]
        HVM["HomeViewModel"]
        EVM["ExercisesViewModel"]
        DVM["ExerciseDetailViewModel"]
        PVM["ProfileViewModel"]
        AVM["AuthViewModel"]
        WVM["ActiveWorkoutViewModel"]
    end

    subgraph Data["Data layer"]
        Auth["AuthManager<br/>(app-wide session state)"]
        Hasher["PasswordHasher<br/>(PBKDF2)"]
        URepo["UserRepository"]
        ERepo["ExerciseRepository"]
        SessRepo["WorkoutSessionRepository"]
        SRepo["SetRepository"]
        UDao["UserDao"]
        EDao["ExerciseDao"]
        SessDao["WorkoutSessionDao"]
        SDao["SetDao"]
        Seed["Room callback + insertInitialExercises<br/>(22 exercises)"]
        Mig["MIGRATION_6_7"]
        Unused["WorkoutRepository + WorkoutDao<br/>(reserved for templates)"]
        DB[("FitTrackDatabase<br/>Room, version 7")]
    end

    File[("fittrack_database<br/>SQLite file on device")]

    App -->|"startKoin"| Module
    Main -->|"setContent"| Nav
    Module -.->|"provides"| VM
    Module -.->|"provides"| Data

    Nav --> Home & Ex & Detail & Add & Prof & Login & Sign & Work

    Home -->|"koinViewModel"| HVM
    Ex -->|"koinViewModel"| EVM
    Add -->|"koinViewModel"| EVM
    Detail -->|"koinViewModel"| DVM
    Prof -->|"koinViewModel"| PVM
    Login -->|"koinViewModel"| AVM
    Sign -->|"koinViewModel"| AVM
    Work -->|"koinViewModel"| WVM

    HVM --> Auth
    HVM --> SessRepo
    PVM --> Auth
    AVM --> Auth
    EVM --> Auth
    EVM --> ERepo
    DVM --> Auth
    DVM --> ERepo
    DVM --> SRepo
    WVM --> Auth
    WVM --> ERepo
    WVM --> SessRepo
    WVM --> SRepo

    Auth --> URepo
    Auth --> Hasher
    URepo --> UDao
    ERepo --> EDao
    SessRepo --> SessDao
    SRepo --> SDao
    UDao --> DB
    EDao --> DB
    SessDao --> DB
    SDao --> DB
    Unused -.-> DB
    Seed -->|"onCreate, and onOpen if exercises is empty"| DB
    Mig -->|"on upgrade from v6"| DB
    DB --> File

    classDef unused stroke-dasharray: 5 5
    class Unused unused
```

Notes:
- `ExercisesViewModel` serves two screens (list and add). Each screen gets its own instance (scoped to
  its navigation entry), so they stay in sync through the Room `Flow`, not through shared VM state.
- Screens never reach repositories or DAOs directly.
- `ActiveWorkoutScreen` is reached from Home (no argument) and from the exercise detail screen
  (`?exerciseId=`). Both routes use the same destination.

---

## 2. Data flows

### 2.1 Cold start, seeding and migration

```mermaid
sequenceDiagram
    participant OS as Android
    participant App as FitTrackApplication
    participant Act as MainActivity
    participant Koin as Koin appModule
    participant Auth as AuthManager
    participant DB as Room FitTrackDatabase

    OS->>App: onCreate
    App->>Koin: startKoin(appModule)
    OS->>Act: launch
    Act->>Act: setContent, AppNavHost starts at HOME
    Note over Act,Koin: HomeViewModel is created and injects AuthManager
    Koin->>Auth: create AuthManager
    Auth->>DB: collect users Flow (first query opens the DB)
    alt first install
        DB->>DB: onCreate seeds 22 exercises
    else existing v6 database
        DB->>DB: MIGRATION_6_7, then Room validates the schema
    end
    DB->>DB: onOpen seeds again only if exercises table is empty
    DB-->>Auth: first emission (a User or null)
    Auth->>Auth: isInitialized = true
```

### 2.2 Exercises: list, detail, add

```mermaid
sequenceDiagram
    actor User
    participant List as ExercisesScreen
    participant EVM as ExercisesViewModel
    participant Repo as ExerciseRepository
    participant Room as ExerciseDao and Room
    participant Nav as AppNavHost
    participant Det as ExerciseDetailsScreen
    participant DVM as ExerciseDetailViewModel

    Note over EVM,Room: List (reactive)
    EVM->>Repo: getExercises()
    Repo->>Room: getAllExercises() Flow
    Room-->>EVM: exercises StateFlow (WhileSubscribed 5s)
    EVM-->>List: collectAsStateWithLifecycle

    Note over User,DVM: Detail
    User->>List: tap exercise
    List->>Nav: onExerciseClick(id)
    Nav->>Det: navigate EXERCISE_DETAIL/id
    Det->>DVM: loadExercise(id)
    DVM->>Repo: getExerciseById(UUID)
    Repo->>Room: query by id
    Room-->>DVM: Exercise (exercise StateFlow)
    DVM-->>Det: render

    Note over User,Room: Add (only shown when logged in)
    User->>List: tap add button
    List->>Nav: onAddExerciseClick
    Nav-->>User: show ExerciseAddScreen
    User->>EVM: submit form, addExercise(ExerciseInput)
    EVM->>Repo: insertExercise(new Exercise)
    Repo->>Room: insert (ABORT on a duplicate name)
    Room-->>EVM: getAllExercises Flow re-emits the full list
    EVM-->>List: list updates, no manual refresh
```

A duplicate name makes the insert throw; the ViewModel swallows it, so the user sees nothing happen.

### 2.3 Authentication and session propagation

```mermaid
sequenceDiagram
    participant Sign as SignUpScreen
    participant AVM as AuthViewModel
    participant Auth as AuthManager
    participant Hash as PasswordHasher
    participant Repo as UserRepository
    participant Room as UserDao and Room
    participant VMs as Home, Profile, Exercises, Detail, Workout ViewModels
    participant Nav as AppNavHost
    participant Prof as ProfileScreen
    participant PVM as ProfileViewModel

    Note over Sign,Nav: Sign up
    Sign->>AVM: signUp(name, nickname, password, ...)
    AVM->>Auth: signUp(...)
    Auth->>Repo: findByNickname
    Repo->>Room: getByNickname
    alt nickname already taken
        Auth-->>AVM: false
        AVM-->>Sign: error "Nickname already taken"
    else free
        Auth->>Hash: hash(password) on Dispatchers.Default
        Auth->>Repo: signUp(new User)
        Repo->>Room: insertUser (isLoggedIn = false)
        Repo->>Room: setLoggedInUser(id)
        par session propagation
            Room-->>Repo: users Flow re-emits (WHERE isLoggedIn = 1)
            Repo-->>Auth: loggedInUser
            Auth-->>VMs: currentUser StateFlow
        and screen result
            AVM-->>Sign: uiState.isSuccess = true
            Sign->>Nav: onSignUpSuccess, navigate PROFILE
        end
    end

    Note over Sign,Room: Login (LoginScreen and AuthViewModel take the same path)
    Auth->>Repo: findByNickname
    Auth->>Hash: verify(password, stored hash) on Dispatchers.Default
    Auth->>Repo: setLoggedIn(id)
    Repo->>Room: setLoggedInUser(id)
    Room-->>VMs: currentUser updates

    Note over Prof,Room: Logout
    Prof->>PVM: logout()
    PVM->>Auth: logout()
    Auth->>Repo: logout()
    Repo->>Room: UPDATE users SET isLoggedIn = 0
    Room-->>Auth: users Flow emits null
    Auth-->>VMs: currentUser = null, screens show guest state
```

The account and its workout history are kept on logout. The session is the `isLoggedIn` flag on the
user row.

### 2.4 Logging a workout

```mermaid
sequenceDiagram
    actor User
    participant Entry as Home or ExerciseDetailsScreen
    participant Nav as AppNavHost
    participant Scr as ActiveWorkoutScreen
    participant WVM as ActiveWorkoutViewModel
    participant Sess as WorkoutSessionRepository
    participant Sets as SetRepository
    participant Room as Room

    User->>Entry: Start workout, or Add to Today's Workout
    Entry->>Nav: navigate ACTIVE_WORKOUT (optional exerciseId)
    Nav->>Scr: show
    Scr->>WVM: addExerciseFromRoute(exerciseId) if present
    WVM->>Sess: getActiveSession(userId)
    alt no active session
        WVM->>Sess: insertWorkoutSession(userId, startedAt = now)
    end
    WVM->>Sets: insertSet (position = last + 1, prefilled from previous set)
    Room-->>WVM: session and sets Flows re-emit
    WVM-->>Scr: uiState (exercises, elapsed time, rest timer)

    loop each set
        User->>Scr: type weight and reps
        Scr->>WVM: updateSetValues(id, weight, reps)
        WVM->>Sets: updateValues
        User->>Scr: mark set done
        Scr->>WVM: toggleSetCompleted(set)
        WVM->>Sets: updateCompleted
        WVM->>WVM: restEndsAt = now + rest duration
        WVM-->>Scr: rest countdown, adjust or skip
    end

    User->>Scr: Finish
    Scr->>WVM: finishWorkout(onDone)
    WVM->>Sets: deleteIncompleteSets(sessionId)
    alt no sets left
        WVM->>Sess: deleteWorkoutSession
    else
        WVM->>Sess: updateWorkoutSession(endedAt = now)
    end
    WVM-->>Scr: onDone, navigate back
```

The elapsed time is derived from the stored `startedAt` on a 1 s ticker, so an in-progress workout
survives process death. Only the rest timer is in-memory.

### 2.5 Personal records and history

```mermaid
sequenceDiagram
    participant Det as ExerciseDetailsScreen
    participant DVM as ExerciseDetailViewModel
    participant Sets as SetRepository
    participant Home as HomeScreen
    participant HVM as HomeViewModel
    participant Sess as WorkoutSessionRepository

    Note over Det,Sets: Personal records
    Det->>DVM: collect records
    DVM->>Sets: getSetHistory(userId, exerciseId)
    Sets-->>DVM: completed sets with session start time
    DVM->>DVM: toExerciseRecords (heaviest set, best Epley 1RM)
    DVM-->>Det: PersonalRecordsWidget

    Note over Home,Sess: Home history
    Home->>HVM: collect uiState
    HVM->>Sess: getActiveSession(userId)
    HVM->>Sess: getRecentSessionSummaries(userId, 5)
    Sess-->>HVM: hasActiveWorkout and recent SessionSummary list
    HVM-->>Home: WorkoutActivitySection
```

---

## 3. Not in the diagrams (exists in code, unused)

- `Workout` entity, `WorkoutDao` and `WorkoutRepository`: registered in Koin, no ViewModel or screen uses
  them (shown dashed in §1). Reserved for workout templates, which are not implemented.
- `AuthManager.isLoggedIn`: no reader outside `AuthManager` itself.
- The Analytics bottom-nav item is commented out (`FitTrackBottomNav.kt:28`); there is no analytics
  route in `AppNavHost`.
- Profile statistics are still hardcoded placeholders, although the data to compute them now exists.
