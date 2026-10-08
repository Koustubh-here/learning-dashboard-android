package com.example.learningdashboard.data.local

import android.content.Context

/**
 * Remembers that the user is signed in across app restarts.
 * Prototype only: production would store an auth token in EncryptedSharedPreferences / Android Keystore.
 */
class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("logged_in", false)
        set(value) = prefs.edit().putBoolean("logged_in", value).apply()
}