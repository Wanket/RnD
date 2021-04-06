package security

import java.security.MessageDigest

object PasswordHasher {
    private val digest = MessageDigest.getInstance("SHA-256")

    fun getHash(salt: ByteArray, password: String) =
        digest.digest(digest.digest(password.toByteArray()) + salt)
}
