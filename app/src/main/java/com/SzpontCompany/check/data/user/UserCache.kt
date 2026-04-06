package com.SzpontCompany.check.data.user

import android.content.Context
import androidx.core.content.edit

class UserCache(private val context: Context) {
    private val prefs = context.getSharedPreferences("user_cache", Context.MODE_PRIVATE)

    fun save(user: User) {
        prefs.edit {
            putString("uid", user.uid)
                .putString("name", user.name)
                .putString("email", user.email)
                .putString("nickname", user.nickname)
                .putBoolean("isAdmin", user.isAdmin)
                .putLong("updatedAt", System.currentTimeMillis())
        }
    }

    fun get(uid: String) : User? {
        val cachedUid = prefs.getString("uid", null) ?: return null
        if (cachedUid != uid) return null
        val name = prefs.getString("name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val nickname = prefs.getString("nickname", "") ?: ""
        val isAdmin = prefs.getBoolean("isAdmin", false)

        return User(uid, name, email, nickname, isAdmin)
    }

    fun isValid() : Boolean {
        val updatedAt = prefs.getLong("updatedAt", 0)
        val ttl = 6 * 60 * 60 * 1000
        return System.currentTimeMillis() - updatedAt < ttl
    }

    fun getLastUid(): String? {
        return prefs.getString("uid", null)
    }

    fun clear() {
        prefs.edit {clear()}
    }
}