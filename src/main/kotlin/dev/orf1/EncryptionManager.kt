package dev.orf1

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class EncryptionManager {
    private val md: MessageDigest = MessageDigest.getInstance("SHA-512")
    private val sr: SecureRandom = SecureRandom.getInstanceStrong()

    fun generateToken(): String {
        val pool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..32)
            .map { sr.nextInt(pool.size) }
            .map(pool::get)
            .joinToString("")
    }

    fun hash(input: String): String {
        try {
            val messageDigest = md.digest(input.toByteArray())
            val no = BigInteger(1, messageDigest)
            var hashtext = no.toString(16)
            while (hashtext.length < 32) {
                hashtext = "0$hashtext"
            }
            return hashtext
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}