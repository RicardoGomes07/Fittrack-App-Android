package com.example.fittrack

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fittrack.data.local.FitTrackDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
class DatabaseSeedingTest : KoinTest {

    private val database: FitTrackDatabase by inject()

    @Before
    fun setup() {
        // Delete the database to ensure onCreate is called
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase("fittrack_database")
    }

    @Test
    fun testDatabaseSeeding() = runBlocking {
        // Now that onCreate is synchronous in AppModule, we don't need a long sleep
        // but a small delay for the Flow to emit might be helpful
        val exercises = database.exerciseDao().getAllExercises().first()
        
        // We inserted 13 exercises in AppModule
        assertTrue("Database should be seeded with exercises. Found: ${exercises.size}", exercises.isNotEmpty())
        assertTrue("Expected at least 13 exercises, found ${exercises.size}", exercises.size >= 13)
        
        val chestExercises = exercises.filter { it.muscleGroup.name == "CHEST" }
        assertTrue("Should have chest exercises", chestExercises.isNotEmpty())
    }
}
