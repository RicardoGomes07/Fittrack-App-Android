package com.example.fittrack.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fittrack.data.PasswordHasher

/**
 * v6 -> v7
 * - users: `password` becomes a PBKDF2 `passwordHash`, `isLoggedIn` is added, `nickname` becomes unique.
 *   In v6 any stored user counted as logged in, so existing rows keep that state.
 * - workout_sessions / sets: rebuilt with foreign keys and the fields needed for logging.
 *   They had no consumers in v6, so no data is lost. `exercises` and `workouts` are untouched.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `users_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `nickname` TEXT NOT NULL, " +
                "`passwordHash` TEXT NOT NULL, `weight` REAL NOT NULL, `height` INTEGER NOT NULL, `birthDate` INTEGER, " +
                "`memberSince` INTEGER NOT NULL, `goalWeight` REAL, `gender` TEXT NOT NULL, `level` INTEGER NOT NULL, " +
                "`xp` INTEGER NOT NULL, `isLoggedIn` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "INSERT INTO users_new (id, name, nickname, passwordHash, weight, height, birthDate, memberSince, " +
                "goalWeight, gender, level, xp, isLoggedIn) " +
                "SELECT id, name, nickname, password, weight, height, birthDate, memberSince, " +
                "goalWeight, gender, level, xp, 1 FROM users"
        )
        val legacyPasswords = mutableListOf<Pair<String, String>>()
        db.query("SELECT id, passwordHash FROM users_new").use { cursor ->
            while (cursor.moveToNext()) legacyPasswords += cursor.getString(0) to cursor.getString(1)
        }
        legacyPasswords.forEach { (id, plain) ->
            db.execSQL("UPDATE users_new SET passwordHash = ? WHERE id = ?", arrayOf(PasswordHasher.hash(plain), id))
        }
        db.execSQL("DROP TABLE `users`")
        db.execSQL("ALTER TABLE `users_new` RENAME TO `users`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_nickname` ON `users` (`nickname`)")

        db.execSQL("DROP TABLE IF EXISTS `sets`")
        db.execSQL("DROP TABLE IF EXISTS `workout_sessions`")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, " +
                "`startedAt` INTEGER NOT NULL, `endedAt` INTEGER, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sessions_userId` ON `workout_sessions` (`userId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sets` (`id` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, " +
                "`reps` INTEGER NOT NULL, `weight` REAL NOT NULL, `position` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , " +
                "FOREIGN KEY(`sessionId`) REFERENCES `workout_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sets_exerciseId` ON `sets` (`exerciseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sets_sessionId` ON `sets` (`sessionId`)")
    }
}
