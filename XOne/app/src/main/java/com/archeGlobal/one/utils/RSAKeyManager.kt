package com.archeGlobal.one.utils

import android.content.Context
import android.util.Base64
import android.util.Log
import java.io.IOException
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class RSAKeyManager(private val context: Context) {
    
    companion object {
        private const val TAG = "RSAKeyManager"
        private const val RSA_ALGORITHM = "RSA"
    }
    
    /**
     * Load public key from PEM file in assets
     */
    fun loadPublicKeyFromPEM(fileName: String): PublicKey? {
        return try {
            val pemString = loadPEMFromAssets(fileName)
            val keyData = parsePEMToDER(pemString)
            
            if (keyData != null) {
                val keySpec = X509EncodedKeySpec(keyData)
                val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
                val publicKey = keyFactory.generatePublic(keySpec)
                Log.d(TAG, "Successfully loaded public key from $fileName.pem")
                publicKey
            } else {
                Log.e(TAG, "Failed to parse public key PEM data from $fileName.pem")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading public key from $fileName.pem: ${e.message}", e)
            null
        }
    }
    
    /**
     * Load private key from PEM file in assets
     */
    fun loadPrivateKeyFromPEM(fileName: String): PrivateKey? {
        return try {
            val pemString = loadPEMFromAssets(fileName)
            val keyData = parsePEMToDER(pemString)
            
            if (keyData != null) {
                val keySpec = PKCS8EncodedKeySpec(keyData)
                val keyFactory = KeyFactory.getInstance(RSA_ALGORITHM)
                val privateKey = keyFactory.generatePrivate(keySpec)
                Log.d(TAG, "Successfully loaded private key from $fileName.pem")
                privateKey
            } else {
                Log.e(TAG, "Failed to parse private key PEM data from $fileName.pem")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading private key from $fileName.pem: ${e.message}", e)
            null
        }
    }
    
    /**
     * Load PEM file content from assets
     */
    private fun loadPEMFromAssets(fileName: String): String {
        return try {
            val inputStream = context.assets.open("$fileName.pem")
            val content = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            Log.d(TAG, "Successfully read PEM file: $fileName.pem")
            content
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read PEM file: $fileName.pem", e)
            throw e
        }
    }
    
    /**
     * Parse PEM format to DER format
     */
    private fun parsePEMToDER(pemString: String): ByteArray? {
        return try {
            var pem = pemString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "")
                .replace("\n", "")
                .replace("\r", "")
                .trim()
            
            val keyData = Base64.decode(pem, Base64.DEFAULT)
            Log.d(TAG, "Successfully parsed PEM key, data size: ${keyData.size} bytes")
            keyData
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64 PEM data: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get the client private key
     */
    fun getClientPrivateKey(): PrivateKey? {
        return loadPrivateKeyFromPEM("client_private_key")
    }
    
    /**
     * Get the server public key
     */
    fun getServerPublicKey(): PublicKey? {
        return loadPublicKeyFromPEM("server_public_key")
    }
} 