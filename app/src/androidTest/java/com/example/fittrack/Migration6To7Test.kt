package com.example.fittrack

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fittrack.data.PasswordHasher
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.data.local.MIGRATION_6_7
import com.example.fittrack.model.WorkoutSession
import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * Builds a genuine v6 database by hand (schema copied from the v6 Room output; there are no exported
 * schema files), then opens it through Room with MIGRATION_6_7. Room validates the migrated schema
 * against the v7 entities on open, so a mismatch fails here.
 */
@RunWith(AndroidJUnit4::class)
class Migration6To7Test {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val dbName = "migration_6_7_test"
    private val userId = UUID.randomUUID().toString()
    private val exerciseId = UUID.randomUUID().toString()

    @Before
    fun createV6Database() {
        context.deleteDatabase(dbName)
        val helper = object : SQLiteOpenHelper(context, dbName, null, 6) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `muscleGroup` TEXT NOT NULL, `description` TEXT NOT NULL, `exerciseType` TEXT NOT NULL, `equipment` TEXT NOT NULL, `imageRes` INTEGER, PRIMARY KEY(`id`))")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercises_name` ON `exercises` (`name`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` TEXT NOT NULL, `workoutId` TEXT NOT NULL, `date` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `sets` (`id` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `reps` INTEGER NOT NULL, `weight` REAL NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `workouts` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `exerciseIds` TEXT NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `nickname` TEXT NOT NULL, `password` TEXT NOT NULL, `weight` REAL NOT NULL, `height` INTEGER NOT NULL, `birthDate` INTEGER, `memberSince` INTEGER NOT NULL, `goalWeight` REAL, `gender` TEXT NOT NULL, `level` INTEGER NOT NULL, `xp` INTEGER NOT NULL, PRIMARY KEY(`id`))")

                db.execSQL(
                    "INSERT INTO users (id, name, nickname, password, weight, height, birthDate, memberSince, goalWeight, gender, level, xp) " +
                        "VALUES ('$userId', 'Alex', 'alex', 'secret', 72.5, 180, NULL, 19000, NULL, 'MALE', 3, 250)"
                )
                db.execSQL(
                    "INSERT INTO exercises (id, name, muscleGroup, description, exerciseType, equipment, imageRes) " +
                        "VALUES ('$exerciseId', 'Chest Press', 'CHEST', '', 'WEIGHT_REPS', 'BARBELL', NULL)"
                )
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        helper.writableDatabase.close()
        helper.close()
    }

    @After
    fun cleanUp() {
        context.deleteDatabase(dbName)
    }

    private fun openMigrated(): FitTrackDatabase =
        Room.databaseBuilder(context, FitTrackDatabase::class.java, dbName)
            .addMigrations(MIGRATION_6_7)
            .build()

    @Test
    fun existingAccount_survivesStaysLoggedInAndHasItsPasswordHashed() = runBlocking {
        val db = openMigrated()
        try {
            val user = db.userDao().getByNickname("alex")
            assertNotNull("the account must survive the migration", user)
            user!!
            assertEquals("Alex", user.name)
            assertEquals(72.5, user.weight, 0.0)
            assertEquals(3, user.level)
            assertEquals(250, user.xp)
            assertTrue("v6 treated any stored user as logged in", user.isLoggedIn)
            assertNotEquals("secret", user.passwordHash)
            assertTrue(PasswordHasher.verify("secret", user.passwordHash))
            assertFalse(PasswordHasher.verify("wrong", user.passwordHash))
            assertEquals(user.id.toString(), db.userDao().getLoggedInUser().first()?.id?.toString())
        } finally {
            db.close()
        }
    }

    @Test
    fun exercises_areUntouched() = runBlocking {
        val db = openMigrated()
        try {
            assertEquals(listOf("Chest Press"), db.exerciseDao().getAllExercises().first().map { it.name })
        } finally {
            db.close()
        }
    }

    @Test
    fun rebuiltWorkoutTables_acceptWorkoutsAndEnforceForeignKeys() = runBlocking {
        val db = openMigrated()
        try {
            val session = WorkoutSession(userId = UUID.fromString(userId), startedAt = 1)
            db.workoutSessionDao().insertWorkoutSession(session)
            db.setDao().insertSet(
                WorkoutSet(exerciseId = UUID.fromString(exerciseId), sessionId = session.id, reps = 5, weight = 60.0, position = 0)
            )
            assertEquals(1, db.setDao().getSetsForSession(session.id).first().size)

            val orphan = WorkoutSession(userId = UUID.randomUUID(), startedAt = 1)
            assertTrue("a session for an unknown user must be rejected", runCatching {
                db.workoutSessionDao().insertWorkoutSession(orphan)
            }.isFailure)
        } finally {
            db.close()
        }
    }
}
