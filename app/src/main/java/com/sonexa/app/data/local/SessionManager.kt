package com.sonexa.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SavedAccount(
    val userId: String,
    val name: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String? = null
)

class SessionManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = createSecurePrefs(appContext)
    private val gson = Gson()

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)?.takeIf { it.isNotBlank() }
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var pendingOtpEmail: String?
        get() = prefs.getString(KEY_PENDING_OTP_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_PENDING_OTP_EMAIL, value).apply()

    var audioQuality: String
        get() = prefs.getString(KEY_AUDIO_QUALITY, "Lossless") ?: "Lossless"
        set(value) = prefs.edit().putString(KEY_AUDIO_QUALITY, value).apply()

    var preferredLanguages: List<String>
        get() = prefs.getString(KEY_PREFERRED_LANGUAGES, null)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
        set(value) = prefs.edit()
            .putString(KEY_PREFERRED_LANGUAGES, value.joinToString(","))
            .apply()

    fun getSavedAccounts(): List<SavedAccount> {
        val raw = prefs.getString(KEY_SAVED_ACCOUNTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SavedAccount>>() {}.type
            gson.fromJson(raw, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addOrUpdateAccount(account: SavedAccount) {
        val current = getSavedAccounts().toMutableList()
        current.removeAll { it.userId == account.userId || it.email.equals(account.email, ignoreCase = true) }
        current.add(0, account)
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, gson.toJson(current)).apply()
    }

    fun switchAccount(targetUserId: String): Boolean {
        val accounts = getSavedAccounts()
        val match = accounts.firstOrNull { it.userId == targetUserId } ?: return false
        saveSession(
            accessToken = match.accessToken,
            refreshToken = match.refreshToken,
            userId = match.userId,
            email = match.email,
            name = match.name
        )
        return true
    }

    fun removeAccount(targetUserId: String) {
        val current = getSavedAccounts().toMutableList()
        current.removeAll { it.userId == targetUserId }
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, gson.toJson(current)).apply()
    }

    fun saveSession(
        accessToken: String?,
        refreshToken: String?,
        userId: String?,
        email: String?,
        name: String?
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .remove(KEY_PENDING_OTP_EMAIL)
            .apply()

        if (!accessToken.isNullOrBlank() && !userId.isNullOrBlank()) {
            addOrUpdateAccount(
                SavedAccount(
                    userId = userId,
                    name = name?.takeIf { it.isNotBlank() } ?: email?.substringBefore("@") ?: "User",
                    email = email.orEmpty(),
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        }
    }

    @Synchronized
    fun updateTokens(accessToken: String?, refreshToken: String?) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        val userId = this.userId
        if (!accessToken.isNullOrBlank() && !userId.isNullOrBlank()) {
            addOrUpdateAccount(
                SavedAccount(
                    userId = userId,
                    name = userName ?: userEmail?.substringBefore("@") ?: "User",
                    email = userEmail.orEmpty(),
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        }
    }

    fun clearSession() {
        val accounts = getSavedAccounts()
        prefs.edit().clear().apply()
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, gson.toJson(accounts)).apply()
    }

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "sonexa_session_encrypted"
        private const val LEGACY_PREFS_NAME = "sonexa_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PENDING_OTP_EMAIL = "pending_otp_email"
        private const val KEY_AUDIO_QUALITY = "audio_quality"
        private const val KEY_PREFERRED_LANGUAGES = "preferred_languages"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }
        }

        private fun createSecurePrefs(context: Context): SharedPreferences {
            val encrypted = runCatching {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }.getOrElse {
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
            migrateLegacyPrefs(context, encrypted)
            return encrypted
        }

        private fun migrateLegacyPrefs(context: Context, target: SharedPreferences) {
            val legacy = context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            if (legacy.all.isEmpty()) return
            if (target.getString(KEY_ACCESS_TOKEN, null).isNullOrBlank()
                && !legacy.getString(KEY_ACCESS_TOKEN, null).isNullOrBlank()
            ) {
                target.edit()
                    .putString(KEY_ACCESS_TOKEN, legacy.getString(KEY_ACCESS_TOKEN, null))
                    .putString(KEY_REFRESH_TOKEN, legacy.getString(KEY_REFRESH_TOKEN, null))
                    .putString(KEY_USER_ID, legacy.getString(KEY_USER_ID, null))
                    .putString(KEY_USER_EMAIL, legacy.getString(KEY_USER_EMAIL, null))
                    .putString(KEY_USER_NAME, legacy.getString(KEY_USER_NAME, null))
                    .putString(KEY_PREFERRED_LANGUAGES, legacy.getString(KEY_PREFERRED_LANGUAGES, null))
                    .putString(KEY_SAVED_ACCOUNTS, legacy.getString(KEY_SAVED_ACCOUNTS, null))
                    .apply()
            }
            legacy.edit().clear().apply()
        }
    }
}
