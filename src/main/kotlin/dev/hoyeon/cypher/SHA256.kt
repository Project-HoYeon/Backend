package dev.hoyeon.cypher

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object SHA256 {
    @Throws(NoSuchAlgorithmException::class)
    fun encrypt(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(text.toByteArray())
        return bytesToHex(md.digest())
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val builder = StringBuilder()
        for (b in bytes) {
            builder.append(String.format("%02x", b))
        }
        return builder.toString()
    }
}