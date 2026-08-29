package com.sonexa.app.data.local

import android.content.Context
import android.content.SharedPreferences
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

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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

    var pendingOtpCode: String?
        get() = prefs.getString(KEY_PENDING_OTP_CODE, null)
        set(value) = prefs.edit().putString(KEY_PENDING_OTP_CODE, value).apply()

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
        } catch (e: Exception) {
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

    fun clearSession() {
        val accounts = getSavedAccounts()
        prefs.edit().clear().apply()
        // preserve saved accounts list so switching is convenient
        prefs.edit().putString(KEY_SAVED_ACCOUNTS, gson.toJson(accounts)).apply()
    }

    fun isLoggedIn(): Boolean = !accessToken.isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "sonexa_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PENDING_OTP_EMAIL = "pending_otp_email"
        private const val KEY_PENDING_OTP_CODE = "pending_otp_code"
        private const val KEY_PREFERRED_LANGUAGES = "preferred_languages"
        private const val KEY_SAVED_ACCOUNTS = "saved_accounts"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }
        }
    }
}
