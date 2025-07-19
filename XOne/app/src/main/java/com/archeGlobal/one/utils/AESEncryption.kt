package com.archeGlobal.one.utils

import android.util.Log
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESEncryption(private val key: ByteArray, private val iv: ByteArray) {

    companion object {
        private const val TAG = "AESEncryption"
        private const val AES_ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding" // Android uses PKCS5 which is equivalent to PKCS7 for AES
        private const val AES_KEY_SIZE = 32 // 256 bits
        private const val AES_IV_SIZE = 16 // 128 bits

        /**
         * Generate a random AES key
         */
        fun generateRandomKey(): ByteArray {
            val keyBytes = ByteArray(AES_KEY_SIZE)
            SecureRandom().nextBytes(keyBytes)
            return keyBytes
        }

        /**
         * Generate a random initialization vector
         */
        fun generateRandomIV(): ByteArray {
            val ivBytes = ByteArray(AES_IV_SIZE)
            SecureRandom().nextBytes(ivBytes)
            return ivBytes
        }
    }

    /**
     * Encrypt data using AES-256-CBC with PKCS5 padding
     */
    fun encrypt(data: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val keySpec = SecretKeySpec(key, AES_ALGORITHM)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encryptedData = cipher.doFinal(data)

            Log.d(TAG, "Successfully encrypted data, size: ${encryptedData.size} bytes")
            encryptedData
        } catch (e: Exception) {
            Log.e(TAG, "AES encryption failed: ${e.message}", e)
            null
        }
    }

    /**
     * Decrypt data using AES-256-CBC with PKCS5 padding
     */
    fun decrypt(encryptedData: ByteArray): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val keySpec = SecretKeySpec(key, AES_ALGORITHM)
            val ivSpec = IvParameterSpec(iv)

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decryptedData = cipher.doFinal(encryptedData)

            Log.d(TAG, "Successfully decrypted data, size: ${decryptedData.size} bytes")
            decryptedData
        } catch (e: Exception) {
            Log.e(TAG, "AES decryption failed: ${e.message}", e)
            null
        }
    }

    /**
     * Encrypt string data
     */
    fun encrypt(data: String): ByteArray? {
        return encrypt(data.toByteArray(Charsets.UTF_8))
    }

    /**
     * Decrypt to string
     */
    fun decryptToString(encryptedData: ByteArray): String? {
        val decrypted = decrypt(encryptedData)
        return if (decrypted != null) {
            String(decrypted, Charsets.UTF_8)
        } else {
            null
        }
    }
}