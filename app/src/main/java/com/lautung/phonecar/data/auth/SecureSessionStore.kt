package com.lautung.phonecar.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredSession(
    val userId: String,
    val username: String,
    val role: String,
    val refreshToken: String,
)

interface SessionStore {
    fun read(): StoredSession?
    fun write(session: StoredSession)
    fun clear()
}

class SecureSessionStore(context: Context) : SessionStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun read(): StoredSession? {
        val encrypted = preferences.getString(KEY_REFRESH_TOKEN, null) ?: return null
        val iv = preferences.getString(KEY_IV, null) ?: return null
        val userId = preferences.getString(KEY_USER_ID, null) ?: return null
        val username = preferences.getString(KEY_USERNAME, null) ?: return null
        val role = preferences.getString(KEY_ROLE, null) ?: return null
        return runCatching {
            StoredSession(userId, username, role, decrypt(encrypted, iv))
        }.getOrElse {
            clear()
            null
        }
    }

    override fun write(session: StoredSession) {
        val encrypted = encrypt(session.refreshToken)
        preferences.edit {
            putString(KEY_USER_ID, session.userId)
            putString(KEY_USERNAME, session.username)
            putString(KEY_ROLE, session.role)
            putString(KEY_REFRESH_TOKEN, encrypted.value)
            putString(KEY_IV, encrypted.iv)
        }
    }

    override fun clear() {
        preferences.edit { clear() }
    }

    private fun encrypt(value: String): EncryptedValue {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        return EncryptedValue(
            value = Base64.encodeToString(cipher.doFinal(value.toByteArray()), Base64.NO_WRAP),
            iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
        )
    }

    private fun decrypt(value: String, iv: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)),
        )
        return cipher.doFinal(Base64.decode(value, Base64.NO_WRAP)).decodeToString()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build(),
            )
            generateKey()
        }
    }

    private data class EncryptedValue(val value: String, val iv: String)

    private companion object {
        const val PREFERENCES_NAME = "phonecar_session"
        const val KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "phonecar_refresh_token"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
        const val KEY_ROLE = "role"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_IV = "refresh_token_iv"
    }
}
