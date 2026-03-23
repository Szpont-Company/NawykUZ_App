package com.SzpontCompany.check.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await


class AuthViewModel : ViewModel() {
   private val auth by lazy { FirebaseAuth.getInstance() }

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
}