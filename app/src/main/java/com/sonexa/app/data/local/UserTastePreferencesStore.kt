package com.sonexa.app.data.local

import android.content.Context
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.model.UserTasteControls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserTastePreferencesStore {
    private val gson = RetrofitClient.gson
    private const val PREFS_NAME = "zynera_taste_preferences"
    private const val KEY_PREFS = "user_taste_controls"

    private val _tasteControls = MutableStateFlow(UserTasteControls())
    val tasteControls: StateFlow<UserTasteControls> = _tasteControls.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PREFS, null)
        if (!json.isNullOrBlank()) {
            try {
                _tasteControls.value = gson.fromJson(json, UserTasteControls::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateControls(context: Context, updated: UserTasteControls) {
        _tasteControls.value = updated
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = gson.toJson(updated)
            prefs.edit().putString(KEY_PREFS, json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
