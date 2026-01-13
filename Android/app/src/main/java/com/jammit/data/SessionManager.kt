package com.jammit.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "jammit_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "token"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: String) {
        getSharedPreferences(context).edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ID, null)
    }

    fun saveToken(context: Context, token: String) {
        getSharedPreferences(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_TOKEN, null)
    }

    fun clearSession(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}
