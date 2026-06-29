package com.palletflow.data.util

import java.security.MessageDigest

object PasswordHasher {
    fun hash(password: String): String {
        require(password.isNotEmpty()) { "Password cannot be empty" }
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
