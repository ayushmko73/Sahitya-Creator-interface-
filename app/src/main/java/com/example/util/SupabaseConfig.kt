package com.example.util

import android.content.Context
import com.example.BuildConfig

/**
 * Configuration manager for Supabase backend integration.
 * Reads SUPABASE_URL and SUPABASE_ANON_KEY from BuildConfig generated via .env / Secrets Gradle Plugin,
 * or from dynamic SharedPreferences saved by the user in-app.
 */
object SupabaseConfig {
    private var customUrl: String? = null
    private var customKey: String? = null

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("supabase_config_prefs", Context.MODE_PRIVATE)
        customUrl = prefs.getString("custom_supabase_url", null)
        customKey = prefs.getString("custom_supabase_key", null)
    }

    fun saveCredentials(context: Context, url: String, key: String) {
        val prefs = context.getSharedPreferences("supabase_config_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("custom_supabase_url", url.trim())
            .putString("custom_supabase_key", key.trim())
            .apply()
        customUrl = url.trim()
        customKey = key.trim()
    }

    val supabaseUrl: String
        get() {
            if (!customUrl.isNullOrBlank()) return customUrl!!
            val url = BuildConfig.SUPABASE_URL
            if (url.isNotBlank() && url != "YOUR_SUPABASE_PROJECT_URL") return url
            return "YOUR_SUPABASE_PROJECT_URL"
        }

    val supabaseAnonKey: String
        get() {
            if (!customKey.isNullOrBlank()) return customKey!!
            val key = BuildConfig.SUPABASE_ANON_KEY
            if (key.isNotBlank() && key != "YOUR_SUPABASE_ANON_KEY") return key
            return "YOUR_SUPABASE_ANON_KEY"
        }

    fun isConfigured(): Boolean {
        val url = supabaseUrl
        val key = supabaseAnonKey
        return url.isNotBlank() && 
               url != "YOUR_SUPABASE_PROJECT_URL" &&
               key.isNotBlank() && 
               key != "YOUR_SUPABASE_ANON_KEY"
    }
}

