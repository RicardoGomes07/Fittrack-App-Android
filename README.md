# FitTrack 🏋️‍♂️

FitTrack is a modern Android application designed to help users track their workouts, exercises, and fitness progress. Built with Jetpack Compose and following the latest Android development best practices.

## 🚀 Features (Current State)

- **Exercise Library**: A pre-seeded database of 13 common exercises across different muscle groups (Chest, Back, Legs, etc.).
- **Visual Feedback**: Custom-designed Vector Drawable icons for each exercise.
- **Detailed View**: View specific information about each exercise.
- **Robust Persistence**: Powered by Room database with full CRUD capabilities.
- **Clean Architecture**: Organized into data, model, and UI layers using the MVVM pattern.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
- **Dependency Injection**: [Koin](https://insert-koin.io/) (Kotlin-native DI)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) (SQLite abstraction)
- **Navigation**: [Jetpack Navigation](https://developer.android.com/guide/navigation) (Compose Navigation)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Concurrency**: Kotlin Coroutines & Flow
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
│   ├── navigation/   # NavHost and Screen definitions
│   ├── screens/      # Compose Screens and corresponding ViewModels
│   └── theme/        # Material3 Design System implementation
└── FitTrackApplication.kt  # App entry point and Koin init
```

## ⚙️ Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/FitTrack.git
   ```
2. **Open in Android Studio**: (Koala or later recommended)
3. **Build & Run**: The database will automatically seed with default exercises on the first launch.

## 🧪 Testing

The project includes instrumentation tests to ensure data integrity:
- **DatabaseSeedingTest**: Verifies that the Room database correctly seeds default exercises on creation.

Run tests via Android Studio or command line:
```bash
./gradlew connectedDebugAndroidTest
```

## 🛤 Roadmap

- [ ] **Workout Creation**: Build custom workout routines selecting from the exercise library.
- [ ] **Log Sets**: Record reps and weights for each exercise during a session.
- [ ] **Progress Tracking**: Charts and statistics for muscle group volume and strength increases.
- [ ] **Image Library**: Replace placeholders with high-quality exercise demonstrations.

---
Developed as a clean, modern fitness tracking solution.
