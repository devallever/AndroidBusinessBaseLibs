package app.allever.android.sample.im.database

import java.security.MessageDigest

object PasswordUtils {

    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(rawPassword: String, storedHash: String): Boolean {
        return hash(rawPassword) == storedHash
    }
}