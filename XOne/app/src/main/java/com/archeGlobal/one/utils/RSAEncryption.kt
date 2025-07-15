package com.archeGlobal.one.utils

import android.util.Log
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

class RSAEncryption {
    
    companion object {
        private const val TAG = "RSAEncryption"
        private const val RSA_ALGORITHM = "RSA"
        private const val TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding" // Equivalent to iOS .rsaEncryptionOAEPSHA256
        
        /**
         * Encrypt data using RSA public key with OAEP SHA-256 padding
         */
        fun encrypt(data: ByteArray, publicKey: PublicKey): ByteArray? {
            return try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, publicKey)
                val encryptedData = cipher.doFinal(data)
                
                Log.d(TAG, "Successfully encrypted data with RSA, size: ${encryptedData.size} bytes")
                encryptedData
            } catch (e: Exception) {
                Log.e(TAG, "RSA encryption failed: ${e.message}", e)
                null
            }
        }
        
        /**
         * Decrypt data using RSA private key with OAEP SHA-256 padding
         */
        fun decrypt(encryptedData: ByteArray, privateKey: PrivateKey): ByteArray? {
            return try {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, privateKey)
                val decryptedData = cipher.doFinal(encryptedData)
                
                Log.d(TAG, "Successfully decrypted data with RSA, size: ${decryptedData.size} bytes")
                decryptedData
            } catch (e: Exception) {
                Log.e(TAG, "RSA decryption failed: ${e.message}", e)
                null
            }
        }
    }
} 