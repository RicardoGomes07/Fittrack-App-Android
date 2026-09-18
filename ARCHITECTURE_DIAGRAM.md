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
    end

    subgraph VM["ViewModels"]
        HVM["HomeViewModel"]
        EVM["ExercisesViewModel"]
        DVM["ExerciseDetailViewModel"]
        PVM["ProfileViewModel"]
        AVM["AuthViewModel"]
    end

    subgraph Data["Data layer"]
        Auth["AuthManager<br/>(app-wide session state)"]
        URepo["UserRepository"]
        ERepo["ExerciseRepository"]
        UDao["UserDao"]
        EDao["ExerciseDao"]
        Seed["Room callback + insertInitialExercises<br/>(22 exercises)"]
        Unused["WorkoutRepository, WorkoutSessionRepository,<br/>SetRepository + their 3 DAOs"]
        DB[("FitTrackDatabase<br/>Room, version 6")]
    end

    File[("fittrack_database<br/>SQLite file on device")]

    App -->|"startKoin"| Module
    Main -->|"setContent"| Nav
    Module -.->|"provides"| VM
    Module -.->|"provides"| Data

    Nav --> Home & Ex & Detail & Add & Prof & Login & Sign

    Home -->|"koinViewModel"| HVM
    Ex -->|"koinViewModel"| EVM
    Add -->|"koinViewModel"| EVM
    Detail -->|"koinViewModel"| DVM
    Prof -->|"koinViewModel"| PVM
    Login -->|"koinViewModel"| AVM
    Sign -->|"koinViewModel"| AVM

    HVM --> Auth
    PVM --> Auth
    AVM --> Auth
    EVM --> Auth
    EVM --> ERepo
    DVM --> Auth
    DVM --> ERepo

    Auth --> URepo
    URepo --> UDao
    ERepo --> EDao
    UDao --> DB
    EDao --> DB
    Unused -.-> DB
    Seed -->|"onCreate, and onOpen if exercises is empty"| DB
    DB --> File

    classDef unused stroke-dasharray: 5 5
    class Unused unused
```

Notes:
- `ExercisesViewModel` serves two screens (list and add). Each screen gets its own instance (scoped to
  its navigation entry), so they stay in sync through the Room `Flow`, not through shared VM state.
- Screens never reach repositories or DAOs directly.

---

## 2. Data flows

### 2.1 Cold start and seeding

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
    opt first install
        DB->>DB: onCreate seeds 22 exercises
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
    Repo->>Room: insert (REPLACE)
    Room-->>EVM: getAllExercises Flow re-emits the full list
    EVM-->>List: list updates, no manual refresh
```

### 2.3 Authentication and session propagation

```mermaid
sequenceDiagram
    participant Sign as SignUpScreen
    participant AVM as AuthViewModel
    participant Auth as AuthManager
    participant Repo as UserRepository
    participant Room as UserDao and Room
    participant VMs as Home, Profile, Exercises, Detail ViewModels
    participant Nav as AppNavHost
    participant Prof as ProfileScreen
    participant PVM as ProfileViewModel

    Note over Sign,Nav: Sign up
    Sign->>AVM: signUp(name, nickname, password, ...)
    AVM->>Auth: signUp(...)
    Auth->>Repo: signUp(new User)
    Repo->>Room: insertUser (REPLACE)
    par session propagation
        Room-->>Repo: users Flow re-emits (SELECT * FROM users LIMIT 1)
        Repo-->>Auth: loggedInUser
        Auth-->>VMs: currentUser StateFlow
    and screen result
        AVM-->>Sign: uiState.isSuccess = true
        Sign->>Nav: onSignUpSuccess, navigate PROFILE
    end

    Note over Prof,Room: Logout
    Prof->>PVM: logout()
    PVM->>Auth: logout()
    Auth->>Repo: logout()
    Repo->>Room: DELETE FROM users
    Room-->>Auth: users Flow emits null
    Auth-->>VMs: currentUser = null, screens show guest state
```

Known issue visible in this flow: the `users` table is both the account store and the session marker,
so logout deletes the account, and `LoginScreen` can only be reached while the table is empty (where
`login()` always fails). See *Authentication is structurally incomplete* in `ARCHITECTURE.md` §8.

---

## 3. Not in the diagrams (exists in code, unused)

- `Workout`, `WorkoutSession`, `WorkoutSet` entities, their DAOs and repositories: registered in Koin,
  no ViewModel or screen uses them (shown dashed in §1).
- `AuthManager.isLoggedIn`: no reader outside `AuthManager` itself.
- The Analytics bottom-nav item is commented out (`FitTrackBottomNav.kt:28`); there is no analytics
  route in `AppNavHost`.
