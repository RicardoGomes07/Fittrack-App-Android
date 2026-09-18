package com.example.fittrack

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fittrack.data.local.FitTrackDatabase
import com.example.fittrack.model.Equipment
import com.example.fittrack.model.Exercise
import com.example.fittrack.model.ExerciseType
import com.example.fittrack.model.MuscleGroup
import com.example.fittrack.model.User
import com.example.fittrack.model.WorkoutSession
import com.example.fittrack.model.WorkoutSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Room behaviour behind auth and workout logging, on an in-memory database (no Koin, no seeding). */
@RunWith(AndroidJUnit4::class)
class WorkoutLoggingDaoTest {

    private lateinit var db: FitTrackDatabase

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, FitTrackDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun user(nickname: String) = User(name = "Test", nickname = nickname, passwordHash = "hash")

    private fun exercise(name: String) = Exercise(
        name = name,
        muscleGroup = MuscleGroup.CHEST,
        exerciseType = ExerciseType.WEIGHT_REPS,
        equipment = Equipment.BARBELL
    )

    private fun set(exercise: Exercise, session: WorkoutSession, position: Int, reps: Int, weight: Double, done: Boolean) =
        WorkoutSet(exerciseId = exercise.id, sessionId = session.id, reps = reps, weight = weight, position = position, isCompleted = done)

    // ---- auth

    @Test
    fun logout_keepsTheAccountAndLoginSwitchesTheActiveUser() = runBlocking {
        val a = user("a")
        val b = user("b")
        db.userDao().insertUser(a)
        db.userDao().insertUser(b)
        assertNull(db.userDao().getLoggedInUser().first())

        db.userDao().setLoggedInUser(a.id)
        assertEquals(a.id, db.userDao().getLoggedInUser().first()?.id)

        db.userDao().setLoggedInUser(b.id)
        assertEquals(b.id, db.userDao().getLoggedInUser().first()?.id)

        db.userDao().logout()
        assertNull(db.userDao().getLoggedInUser().first())
        assertNotNull("logout must not delete the account", db.userDao().getByNickname("a"))
    }

    @Test
    fun nickname_mustBeUnique() = runBlocking {
        db.userDao().insertUser(user("dup"))
        assertTrue(runCatching { db.userDao().insertUser(user("dup")) }.isFailure)
    }

    // ---- exercises

    @Test
    fun addingADuplicateExerciseName_isRejectedAndKeepsTheOriginal() = runBlocking {
        val original = exercise("Row")
        db.exerciseDao().insertExercise(original)
        assertTrue(runCatching { db.exerciseDao().insertExercise(exercise("Row")) }.isFailure)
        assertEquals(original.id, db.exerciseDao().getAllExercises().first().single().id)
    }

    @Test
    fun exerciseWithLoggedSets_cannotBeDeleted() = runBlocking {
        val u = user("u"); val ex = exercise("Row")
        val session = WorkoutSession(userId = u.id, startedAt = 1)
        db.userDao().insertUser(u); db.exerciseDao().insertExercise(ex); db.workoutSessionDao().insertWorkoutSession(session)
        db.setDao().insertSet(set(ex, session, 0, 5, 50.0, true))

        assertTrue(runCatching { db.exerciseDao().deleteExercise(ex) }.isFailure)
    }

    // ---- sessions and sets

    @Test
    fun activeSession_isTheOneWithoutAnEndTime() = runBlocking {
        val u = user("u")
        db.userDao().insertUser(u)
        val finished = WorkoutSession(userId = u.id, startedAt = 1, endedAt = 2)
        val active = WorkoutSession(userId = u.id, startedAt = 10)
        db.workoutSessionDao().insertWorkoutSession(finished)
        db.workoutSessionDao().insertWorkoutSession(active)

        assertEquals(active.id, db.workoutSessionDao().getActiveSession(u.id).first()?.id)

        db.workoutSessionDao().updateWorkoutSession(active.copy(endedAt = 20))
        assertNull(db.workoutSessionDao().getActiveSession(u.id).first())
    }

    @Test
    fun recentSummaries_countCompletedSetsOfFinishedSessionsOfThatUserOnly() = runBlocking {
        val me = user("me"); val other = user("other")
        val bench = exercise("Bench"); val squat = exercise("Squat")
        db.userDao().insertUser(me); db.userDao().insertUser(other)
        db.exerciseDao().insertExercise(bench); db.exerciseDao().insertExercise(squat)

        val done = WorkoutSession(userId = me.id, startedAt = 1000, endedAt = 5000)
        val running = WorkoutSession(userId = me.id, startedAt = 9000)
        val others = WorkoutSession(userId = other.id, startedAt = 2000, endedAt = 3000)
        listOf(done, running, others).forEach { db.workoutSessionDao().insertWorkoutSession(it) }

        db.setDao().insertSet(set(bench, done, 0, 5, 100.0, true))   // 500
        db.setDao().insertSet(set(bench, done, 1, 5, 100.0, true))   // 500
        db.setDao().insertSet(set(squat, done, 2, 10, 50.0, false))  // not completed: ignored
        db.setDao().insertSet(set(bench, running, 0, 5, 100.0, true))
        db.setDao().insertSet(set(bench, others, 0, 5, 100.0, true))

        val summaries = db.workoutSessionDao().getRecentSessionSummaries(me.id, 10).first()

        val only = summaries.single()
        assertEquals(done.id, only.id)
        assertEquals(1, only.exerciseCount)
        assertEquals(2, only.setCount)
        assertEquals(1000.0, only.volume, 0.0)
    }

    @Test
    fun recentSummaries_areNewestFirstAndRespectTheLimit() = runBlocking {
        val u = user("u"); db.userDao().insertUser(u)
        val sessions = (1..3).map { WorkoutSession(userId = u.id, startedAt = it * 1000L, endedAt = it * 1000L + 1) }
        sessions.forEach { db.workoutSessionDao().insertWorkoutSession(it) }

        val summaries = db.workoutSessionDao().getRecentSessionSummaries(u.id, 2).first()

        assertEquals(listOf(sessions[2].id, sessions[1].id), summaries.map { it.id })
        assertEquals(0, summaries.first().setCount) // LEFT JOIN keeps sessions with no sets
    }

    @Test
    fun setHistory_isCompletedSetsOfThatExerciseAndUser() = runBlocking {
        val me = user("me"); val other = user("other")
        val bench = exercise("Bench"); val squat = exercise("Squat")
        db.userDao().insertUser(me); db.userDao().insertUser(other)
        db.exerciseDao().insertExercise(bench); db.exerciseDao().insertExercise(squat)
        val mine = WorkoutSession(userId = me.id, startedAt = 777)
        val theirs = WorkoutSession(userId = other.id, startedAt = 888)
        db.workoutSessionDao().insertWorkoutSession(mine); db.workoutSessionDao().insertWorkoutSession(theirs)

        db.setDao().insertSet(set(bench, mine, 0, 5, 100.0, true))
        db.setDao().insertSet(set(bench, mine, 1, 3, 110.0, false)) // not completed
        db.setDao().insertSet(set(squat, mine, 2, 5, 140.0, true))  // other exercise
        db.setDao().insertSet(set(bench, theirs, 0, 5, 200.0, true)) // other user

        val history = db.setDao().getSetHistory(me.id, bench.id).first()

        assertEquals(1, history.size)
        assertEquals(100.0, history.single().weight, 0.0)
        assertEquals(777L, history.single().startedAt)
    }

    @Test
    fun deletingASession_removesItsSets() = runBlocking {
        val u = user("u"); val ex = exercise("Row")
        val session = WorkoutSession(userId = u.id, startedAt = 1)
        db.userDao().insertUser(u); db.exerciseDao().insertExercise(ex); db.workoutSessionDao().insertWorkoutSession(session)
        db.setDao().insertSet(set(ex, session, 0, 5, 50.0, true))

        db.workoutSessionDao().deleteWorkoutSession(session)

        assertTrue(db.setDao().getSetsForSession(session.id).first().isEmpty())
    }

    @Test
    fun setEdits_touchOnlyTheirOwnColumns() = runBlocking {
        val u = user("u"); val ex = exercise("Row")
        val session = WorkoutSession(userId = u.id, startedAt = 1)
        db.userDao().insertUser(u); db.exerciseDao().insertExercise(ex); db.workoutSessionDao().insertWorkoutSession(session)
        val s = set(ex, session, 0, 5, 50.0, false)
        db.setDao().insertSet(s)

        db.setDao().updateCompleted(s.id, true)
        db.setDao().updateValues(s.id, 62.5, 8)

        val stored = db.setDao().getSetsForSession(session.id).first().single()
        assertEquals(62.5, stored.weight, 0.0)
        assertEquals(8, stored.reps)
        assertTrue("updating values must not reset completion", stored.isCompleted)
    }

    @Test
    fun deleteIncompleteSets_and_deleteSetsForExercise() = runBlocking {
        val u = user("u"); val bench = exercise("Bench"); val squat = exercise("Squat")
        val session = WorkoutSession(userId = u.id, startedAt = 1)
        db.userDao().insertUser(u); db.exerciseDao().insertExercise(bench); db.exerciseDao().insertExercise(squat)
        db.workoutSessionDao().insertWorkoutSession(session)
        db.setDao().insertSet(set(bench, session, 0, 5, 50.0, true))
        db.setDao().insertSet(set(bench, session, 1, 5, 50.0, false))
        db.setDao().insertSet(set(squat, session, 2, 5, 80.0, true))

        db.setDao().deleteIncompleteSets(session.id)
        assertEquals(listOf(0, 2), db.setDao().getSetsForSession(session.id).first().map { it.position })

        db.setDao().deleteSetsForExercise(session.id, squat.id)
        assertEquals(listOf(0), db.setDao().getSetsForSession(session.id).first().map { it.position })
    }
}
