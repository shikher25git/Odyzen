package com.accountability.core

import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.experimental.and

object TotpUtils {

    // Base32 Alphabet
    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun generateSecret(length: Int = 20): String {
        return (1..length)
            .map { BASE32_CHARS.random() }
            .joinToString("")
    }

    fun validate(secretBase32: String, code: String, window: Int = 1): Boolean {
        val secretBytes = base32Decode(secretBase32) ?: return false
        val currentInterval = floor(System.currentTimeMillis() / 1000.0 / 30.0).toLong()

        for (i in -window..window) {
            val generated = generateTOTP(secretBytes, currentInterval + i)
            if (generated == code) return true
        }
        return false
    }

    private fun generateTOTP(secret: ByteArray, interval: Long): String {
        val hash = hmacSha1(secret, interval)
        val offset = hash[hash.size - 1].toInt() and 0xf

        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)

        val otp = binary % 1_000_000
        return otp.toString().padStart(6, '0')
    }

    private fun hmacSha1(secret: ByteArray, interval: Long): ByteArray {
        try {
            val key = SecretKeySpec(secret, "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(key)
            val buffer = ByteBuffer.allocate(8)
            buffer.putLong(interval)
            return mac.doFinal(buffer.array())
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException(e)
        }
    }

    private fun base32Decode(base32: String): ByteArray? {
        val cleaned = base32.trim().uppercase().replace(" ", "")
        // Remove padding
        val withoutPadding = cleaned.replace("=+$".toRegex(), "")
        if (withoutPadding.isEmpty()) return ByteArray(0)

        // Count how many bits we have
        // Each char is 5 bits
        val totalBits = withoutPadding.length * 5
        val totalBytes = totalBits / 8
        val result = ByteArray(totalBytes)

        var buffer = 0
        var bufferLength = 0
        var byteIndex = 0

        for (char in withoutPadding) {
            val value = BASE32_CHARS.indexOf(char)
            if (value == -1) return null // Invalid char

            buffer = (buffer shl 5) or value
            bufferLength += 5

            if (bufferLength >= 8) {
                result[byteIndex++] = (buffer shr (bufferLength - 8)).toByte()
                bufferLength -= 8
            }
        }
        return result
    }
}
