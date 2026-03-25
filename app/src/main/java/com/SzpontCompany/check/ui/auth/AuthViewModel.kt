package com.SzpontCompany.check.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.RectangleShape
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import com.SzpontCompany.check.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaClient
import com.google.android.recaptcha.RecaptchaException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
   private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var recaptchaClient: RecaptchaClient
    private val recaptchaScope = CoroutineScope(Dispatchers.IO)

    init {
        initializeRecaptcha()
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser?> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val credential = try {
                // Defaultowe logowanie Googlem - wymaga blokady ekranu do działania
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("919945083217-onoqped8qp5v39cp18eth082qd6supbt.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(true)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                credentialManager.getCredential(context, request).credential
            } catch (e: NoCredentialException) {
                Log.e("GoogleSignIn", "No credential found: ${e.message}")
                // Fallback logowania
                val sigInOption = GetSignInWithGoogleOption.Builder("919945083217-onoqped8qp5v39cp18eth082qd6supbt.apps.googleusercontent.com")
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(sigInOption)
                    .build()
                credentialManager.getCredential(context, request).credential
            }

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()

            Result.success(authResult.user)
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            if(authResult.user?.isEmailVerified == true) {
                Result.success(authResult.user!!)
            } else {
                Result.failure(Exception("Email not verified"))
            }
        } catch (e: Exception) {
            Log.e("EmailSignIn", "Error: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user!!.sendEmailVerification()

            val user = authResult.user ?: throw Exception("User creation failed")

            val userData = mapOf(
                "uid" to user.uid,
                "name" to name,
                "email" to email
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .set(userData, SetOptions.merge())
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Log.e("EmailSignUp", "Error: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
       return try {
           auth.sendPasswordResetEmail(email).await()
           Result.success(Unit)
       } catch (e: Exception) {
           Log.e("PasswordReset", "Error: ${e::class.simpleName} - ${e.message}")
           Result.failure(e)
       }
    }

    private fun initializeRecaptcha() {
        recaptchaScope.launch {
            try {
                recaptchaClient = Recaptcha.fetchClient(
                    getApplication(),"6LdJmpYsAAAAABW4_tXEZl0T6by3ov_P2d8jd5wK"
                )

            } catch(e: RecaptchaException) {
                Log.e("Recaptcha", "Error fetching client: ${e.message}")
            }
        }
    }
    private val _recaptchaToken = MutableStateFlow<String?>(null)
    val recaptchaToken: StateFlow<String?> = _recaptchaToken.asStateFlow()

    private val _recaptchaError = MutableStateFlow<String?>(null)
    val recaptchaError: StateFlow<String?> = _recaptchaError.asStateFlow()

    fun executeCaptcha() {
        recaptchaScope.launch {
            try {
                val token = recaptchaClient.execute(RecaptchaAction.SIGNUP).getOrThrow()
                _recaptchaToken.value = token
            } catch (e: RecaptchaException) {
                _recaptchaError.value = e.message
                Log.e("Recaptcha", "Error executing: ${e.message}")
            }
        }
    }
}