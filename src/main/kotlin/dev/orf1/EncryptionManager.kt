package dev.orf1

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class EncryptionManager {
    fun generateToken(): String {
        val pool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..16)
            .map { i -> kotlin.random.Random.nextInt(0, pool.size) }
            .map(pool::get)
            .joinToString("")
    }

    fun hash(input: String): String {
        try {
            val md = MessageDigest.getInstance("SHA-512")
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