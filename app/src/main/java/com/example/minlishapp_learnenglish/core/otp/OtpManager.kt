package com.example.minlishapp_learnenglish.core.otp

import android.content.Context

class OtpManager(context: Context) {
    private val prefs = context.getSharedPreferences("forgot_password_otp", Context.MODE_PRIVATE)

    fun saveOtp(email: String, otp: String) {
        val expiry = System.currentTimeMillis() + 5 * 60 * 1000
        prefs.edit()
            .putString("email", email)
            .putString("otp", otp)
            .putLong("expiry", expiry)
            .putBoolean("verified", false)
            .apply()
    }

    fun getEmail(): String? = prefs.getString("email", null)

    fun isOtpValid(inputOtp: String): Boolean {
        val savedOtp = prefs.getString("otp", null) ?: return false
        val expiry = prefs.getLong("expiry", 0)
        if (System.currentTimeMillis() > expiry) return false
        return savedOtp == inputOtp
    }

    fun markVerified() {
        prefs.edit().putBoolean("verified", true).apply()
    }

    fun isVerified(): Boolean = prefs.getBoolean("verified", false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
