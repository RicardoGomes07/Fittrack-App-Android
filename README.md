# FitTrack 🏋️‍♂️

FitTrack is a modern Android application designed to help users track their workouts, exercises, and fitness progress. Built with Jetpack Compose and following the latest Android development best practices.

## 🚀 Features (Current State)

- **Personalized Dashboard**: A landing page featuring a personalized greeting, the current date, a streak counter, and rotating motivational messages.
- **Exercise Library**: A comprehensive list of exercises categorized by muscle group with custom-designed Vector Drawable icons.
- **Rich Detailed View**:
    - **Form & Execution**: Step-by-step instructions for each exercise.
    - **Biomechanics**: Information on bar path and tempo.
    - **Personal Records**: Tracking for current best rep PR and calculated 1RM.
- **Seamless Navigation**: Multi-screen navigation using a bottom navigation bar for quick access to Dashboard, Exercises, Analytics, and Profile.
- **Robust Persistence**: Powered by Room database with automatic seeding of initial exercise data.
- **Custom Design System**: A bespoke dark-themed UI built on Material 3 with a consistent color palette and typography.
- **Automatic Data Seeding**: Initial database setup with a curated list of 22 exercises covering various muscle groups, equipment types, and exercise styles.
- **Clean Architecture**: Organized into clearly defined layers (Data, Domain/Model, UI) using the MVVM pattern.

## 🗄️ Data Management

The application uses **Room Persistence Library** to manage its data. To provide immediate value on first launch, it includes an automatic seeding mechanism:

- **Initial Seed**: 22 standard exercises (Chest Press, Deadlift, Bulgarian Split Squat, etc.).
- **Metadata**: Each exercise includes name, muscle group, description, exercise type (Weight/Reps vs Bodyweight), and required equipment (Barbell, Dumbbell, Cable, Machine, or None).
- **Implementation**: Handled via a `RoomDatabase.Callback()` that triggers `onCreate`, calling a specialized `insertInitialExercises` utility using raw SQLite `insertWithOnConflict` for efficiency.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
- **Dependency Injection**: [Koin](https://insert-koin.io/) (Kotlin-native DI)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) (SQLite abstraction)
- **Navigation**: [Jetpack Navigation](https://developer.android.com/guide/navigation) (Compose Navigation)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Concurrency**: Kotlin Coroutines & Flow
- **State Management**: StateFlow & collectAsStateWithLifecycle
- **Build System**: Gradle Kotlin DSL + Version Catalogs (libs.versions.toml) + KSP

## 📁 Project Structure

```text
app/src/main/java/com/example/fittrack/
├── data/
│   ├── local/        # Room DB, DAOs, Type Converters
│   └── model/        # Repositories (Data source abstraction)
├── di/               # Koin dependency injection modules
├── model/            # Domain entities (Exercise, Workout, Set, etc.)
├── ui/
│   ├── navigation/   # NavHost, NavController, and Screen routes
│   ├── screens/      # Feature-specific screens and ViewModels
│   │   ├── home/         # Dashboard/Greeting logic
│   │   ├── exercises/    # List, Details, and Add Exercise features
│   │   ├── profile/      # User profile management (WIP)
│   │   └── components/   # Reusable UI widgets (TopBar, BottomNav, Badges)
│   └── theme/        # Custom FitTrack Material3 Design System
└── FitTrackApplication.kt  # App entry point and Koin init
```

## ⚙️ Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/FitTrack.git
   ```
2. **Open in Android Studio**: (Ladybug or later recommended)
3. **Build & Run**: The database will automatically seed with default exercises on the first launch.

## 🧪 Testing

The project includes instrumentation tests to ensure data integrity:
- **DatabaseSeedingTest**: Verifies that the Room database correctly seeds default exercises on creation.

Run tests via Android Studio or command line:
```bash
./gradlew connectedDebugAndroidTest
```

## 🛤 Roadmap

- [ ] **Workout Session Logging**: Record reps, weights, and RPE during a live session.
- [ ] **Analytics & Progress**: Visual charts and statistics for muscle group volume and strength trends.
- [ ] **Custom Workout Templates**: Build and save personal routines.
- [ ] **User Profile**: Customization options for goals and personal info.
- [ ] **Advanced PR History**: Full history of personal records over time.

---
Developed as a clean, modern fitness tracking solution.
