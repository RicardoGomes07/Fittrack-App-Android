package com.example.fittrack.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun verify_acceptsTheOriginalPassword() {
        val stored = PasswordHasher.hash("correct horse")
        assertTrue(PasswordHasher.verify("correct horse", stored))
    }

    @Test
    fun verify_rejectsAWrongPassword() {
        val stored = PasswordHasher.hash("correct horse")
        assertFalse(PasswordHasher.verify("Correct horse", stored))
        assertFalse(PasswordHasher.verify("", stored))
    }

    @Test
    fun hash_isSaltedSoEqualPasswordsDiffer() {
        assertNotEquals(PasswordHasher.hash("same"), PasswordHasher.hash("same"))
    }

    @Test
    fun hash_neverContainsThePlaintextAndUsesTheDocumentedFormat() {
        val stored = PasswordHasher.hash("hunter2")
        assertFalse(stored.contains("hunter2"))
        val parts = stored.split("$")
        assertEquals(4, parts.size)
        assertEquals("pbkdf2", parts[0])
        assertEquals("120000", parts[1])
    }

    @Test
    fun verify_rejectsMalformedStoredValuesInsteadOfThrowing() {
        listOf("", "plaintext", "pbkdf2\$1\$00", "pbkdf2\$x\$00\$00", "pbkdf2\$1000\$0\$00", "pbkdf2\$1000\$zz\$00", "md5\$1000\$00\$00")
            .forEach { assertFalse("should reject '$it'", PasswordHasher.verify("plaintext", it)) }
    }

    @Test
    fun verify_rejectsAnUnhashedLegacyPassword() {
        // v6 stored passwords in plaintext; the migration hashes them, but a raw value must never verify.
        assertFalse(PasswordHasher.verify("secret", "secret"))
    }
}
