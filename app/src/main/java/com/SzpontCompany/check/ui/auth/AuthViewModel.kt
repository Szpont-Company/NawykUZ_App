package com.SzpontCompany.check.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import com.SzpontCompany.check.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.SzpontCompany.check.data.habit.Habit
import com.SzpontCompany.check.data.habit.HabitRepository
import com.SzpontCompany.check.data.user.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
   private val auth by lazy { FirebaseAuth.getInstance() }
   val recaptcha = RecaptchaManager(application, viewModelScope)
   private val userRepository by lazy { UserRepository.getInstance(application.applicationContext) }
   private val habitRepository by lazy { HabitRepository() }

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set
    var name by mutableStateOf("")
        private set

    fun onEmailChange(newEmail: String) {
        email = newEmail.trim()
        emailError = null
    }
    fun onPasswordChange(newPassword: String) {
        password = newPassword
        passwordError = null
    }
    fun onNameChange(newName: String) {
        name = newName
    }
    fun resetPassword(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = resetPassword(email)
            onResult(result)
        }
    }
    private fun cleanCredentials() {
        email = ""
        password = ""
        name = ""
        emailError = null
        passwordError = null
    }

    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    fun validateCredentials(): Boolean {
        var valid = true

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email format"
            valid = false
        }
        if (password.length < 8) {
            passwordError = "Password must be at least 8 characters"
            valid = false
        }

        return valid
    }

    var needsNickname by mutableStateOf(false)
        private set

   val isLoggedIn: Boolean
        get() = auth.currentUser != null


    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser?> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credential = credentialManager.getCredential(context, request).credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val googleUser = authResult.user
            val name = googleUser?.displayName ?: ""
            val email = googleUser?.email ?: ""
            val uid = googleUser?.uid ?: ""

            val docRef = Firebase.firestore.collection("users").document(uid)
            val snapshot = docRef.get().await()

            if(!snapshot.exists()) {
                userRepository.saveUserData(uid, name, email)
                this.needsNickname = true
            } else {
                this.needsNickname = !isNicknameSet(uid)
            }

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
                cleanCredentials()
                this.needsNickname = !isNicknameSet(authResult.user!!.uid)
                Result.success(authResult.user!!)
            } else {
                auth.signOut()
                Result.failure(Exception("Email_not_verified"))
            }
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("EmailSignIn", "Invalid credentials: ${e.message}")
            Result.failure(Exception("Invalid_credentials"))
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.e("EmailSignIn", "User not found: ${e.message}")
            Result.failure(Exception("Account_not_found"))
        }
        catch (e: Exception) {
            Log.e("EmailSignIn", "Error: ${e::class.simpleName} - ${e.message}")
            Result.failure(Exception(e))
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
        } catch (e: FirebaseAuthUserCollisionException) {
            Log.e("EmailSignUp", "Email in use: ${e.message}")
            Result.failure(Exception("Email_already_in_use"))
        }
        catch (e: Exception) {
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

    fun signOut() {
        userRepository.clearCache()
        auth.signOut()
    }

    suspend fun isNicknameSet(uid: String) : Boolean {
        val document = Firebase.firestore.collection("users")
            .document(uid)
            .get()
            .await()
        val nickname = document.getString("nickname") ?: ""
        return nickname.isNotBlank()
    }

    suspend fun saveNickname(nickname: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("No user logged in"))

        val db = Firebase.firestore
        val nicknameRef = db.collection("nicknames").document(nickname)
        val userRef = db.collection("users").document(uid)

        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(nicknameRef)

                if (snapshot.exists()) {
                    throw Exception("Nickname already taken")
                }

                transaction.set(nicknameRef, mapOf("uid" to uid))
                transaction.set(userRef, mapOf("nickname" to nickname), SetOptions.merge())
            }.await()

            needsNickname = false
            userRepository.clearCache()
            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun saveFirstHabit(dailySteps: Int, context: Context): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("No user logged in"))

            // defensive: onboarding input -> Firestore (stepGoal)
            val safeGoal = dailySteps
                .coerceAtLeast(1)
                .coerceAtMost(100_000)

            // 1) zapisz cel kroków w dokumencie usera
            Firebase.firestore
                .collection("users")
                .document(uid)
                .set(mapOf("stepGoal" to safeGoal), SetOptions.merge())
                .await()

            // 2) utwórz/aktualizuj specjalny nawyk kroków w subkolekcji habits
            val stepHabit = Habit(
                name = context.getString(R.string.habit_steps_name),
                icon = "🚶",
                colorName = "Mint",
                frequency = "Daily",
                dailyGoal = safeGoal,
                unit = context.getString(R.string.habit_steps_unit),
                difficulty = context.getString(R.string.habit_steps_difficulty),
                isActive = true,
                isStepsHabit = true
            )

            habitRepository.upsertStepsHabit(stepHabit)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving first habit: ${e.message}")
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "AuthViewModel"
        const val WEB_CLIENT_ID =
            "919945083217-onoqped8qp5v39cp18eth082qd6supbt.apps.googleusercontent.com"
    }
}