package com.example.fittrack.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PBKDF2 password hashing using only the JDK/Android platform.
 * PBKDF2WithHmacSHA1 is used because the SHA-2 variants require API 26 and minSdk is 24.
 * Stored format: `pbkdf2$<iterations>$<saltHex>$<hashHex>`.
 */
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val PREFIX = "pbkdf2"
    private const val ITERATIONS = 120_000
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 160 // matches the SHA-1 output size

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        return format(ITERATIONS, salt, derive(password, salt, ITERATIONS))
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split("$")
        if (parts.size != 4 || parts[0] != PREFIX) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = fromHex(parts[2]) ?: return false
        val expected = fromHex(parts[3]) ?: return false
        return MessageDigest.isEqual(expected, derive(password, salt, iterations))
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun format(iterations: Int, salt: ByteArray, hash: ByteArray) =
        "$PREFIX$$iterations$${toHex(salt)}$${toHex(hash)}"

    private fun toHex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }

    private fun fromHex(hex: String): ByteArray? {
        if (hex.isEmpty() || hex.length % 2 != 0) return null
        return try {
            ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
