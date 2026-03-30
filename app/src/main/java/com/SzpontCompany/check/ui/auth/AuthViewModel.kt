package com.SzpontCompany.check.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
   private val auth by lazy { FirebaseAuth.getInstance() }
   val recaptcha = RecaptchaManager(application, viewModelScope)
   private val userRepository = UserRepository()

   val isLoggedIn: Boolean
        get() = auth.currentUser != null


    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser?> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val credential = try {
                // Defaultowe logowanie Googlem - wymaga blokady ekranu do działania
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                credentialManager.getCredential(context, request).credential
            } catch (e: NoCredentialException) {
                Log.e("GoogleSignIn", "No credential found: ${e.message}")
                // Fallback logowania
                val sigInOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
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
                auth.signOut()
                Result.failure(Exception("Email_not_verified"))
            }
        } catch (e: Exception) {
            Log.e("EmailSignIn", "Error: ${e::class.simpleName} - ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()

            val user = authResult.user ?: throw Exception("User creation failed")
            user.sendEmailVerification().await()

            userRepository.saveUserData(user.uid, name, email)
            auth.signOut()

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

    fun getUserAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(auth.currentUser)
        }

        auth.addAuthStateListener(authStateListener)

        awaitClose { auth.removeAuthStateListener(authStateListener) }
        }

    val currentUser: StateFlow<FirebaseUser?> = getUserAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = auth.currentUser
        )

    private companion object {
        const val TAG = "AuthViewModel"
        const val WEB_CLIENT_ID =
            "919945083217-onoqped8qp5v39cp18eth082qd6supbt.apps.googleusercontent.com"
    }
}