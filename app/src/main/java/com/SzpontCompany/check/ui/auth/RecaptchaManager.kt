package com.SzpontCompany.check.ui.auth

import android.app.Application
import android.util.Log
import com.SzpontCompany.check.config.FirebaseConfig
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaClient
import com.google.android.recaptcha.RecaptchaException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.functions.FirebaseFunctions

class RecaptchaManager(
    private val application: Application,
    private val scope: CoroutineScope,
    private val siteKey: String = "6LdJmpYsAAAAABW4_tXEZl0T6by3ov_P2d8jd5wK",
    private val functions: FirebaseFunctions = FirebaseConfig.functions
) {
    private var client: RecaptchaClient? = null

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _verified = MutableStateFlow<Boolean?>(null)
    val verified: StateFlow<Boolean?> = _verified.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isReady: Boolean get() = client != null

    init {
        initialize()
    }

    private fun initialize() {
        scope.launch {
            try {
                client = Recaptcha.fetchClient(application, siteKey)
                Log.d(TAG, "reCAPTCHA client initialized")
            } catch (e: RecaptchaException) {
                Log.e(TAG, "Failed to initialize reCAPTCHA client", e)
                _error.value = "Failed to initialize reCAPTCHA: ${e.message}"
            }
        }
    }

    fun execute(action: RecaptchaAction = RecaptchaAction.SIGNUP) {
        val safeClient = client ?: run {
            _error.value = "reCAPTCHA client not initialized"
            return
        }

        scope.launch {
            try {
                _error.value = null
                val result = safeClient.execute(action).getOrThrow()
                _token.value = result
                Log.d(TAG, "reCAPTCHA executed successfully, token: $result")

                verifyToken(action)
                    .onSuccess { //score ->
                        _verified.value = true // temporary commented score >= 0.5f
                    }
                    .onFailure {
                        _error.value = it.message
                    }
            } catch (e: RecaptchaException) {
                Log.e(TAG, "Error executing reCAPTCHA", e)
                _error.value = "Error executing reCAPTCHA: ${e.message}"
            }
        }
    }

    fun clearToken() {
        _token.value = null
    }

    fun clearError() {
        _error.value = null
    }

    private companion object {
        const val TAG = "RecaptchaManager"
    }

    suspend fun verifyToken(action: RecaptchaAction = RecaptchaAction.SIGNUP): Result<Float> {
        val currentToken = _token.value ?: return Result.failure(Exception("Brak tokenu"))

        return try {
            val data = hashMapOf(
                "token" to currentToken,
                "action" to action.action  // "signup", "login" itp.
            )

            val result = functions
                .getHttpsCallable("verifyRecaptcha")
                .call(data)
                .await()

            val scoreRaw = (result.data as Map<*, *>)["score"]
            val score = when (scoreRaw) {
                is Float -> scoreRaw
                is Double -> scoreRaw.toFloat()
                is Int -> scoreRaw.toFloat()
                else -> 0f
            }
            Result.success(score)
        } catch (e: Exception) {
            Log.e(TAG, "reCAPTCHA verify error: ${e.message}")
            Result.failure(e)
        }
    }


}