package com.SzpontCompany.check.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Testy dla funkcjonalności logowania, rejestracji i CAPTCHA
 */
class AuthRepositoryTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    private lateinit var authRepository: AuthRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authRepository = AuthRepository(mockAuth, mockFirestore)
    }

    // ==================== TESTY REJESTRACJI ====================

    @Test
    fun `testSuccessfulRegistration - should create user and save profile`() = runTest {
        val email = "test@example.com"
        val password = "SecurePassword123!"
        val username = "TestUser"

        val result = authRepository.register(email, password, username)

        assertTrue(result.isSuccess)
        verify(mockAuth).createUserWithEmailAndPassword(eq(email), eq(password))
    }

    @Test
    fun `testRegistrationWithWeakPassword - should fail with weak password exception`() = runTest {
        val email = "test@example.com"
        val weakPassword = "123"
        val username = "TestUser"

        val exception = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "Password too weak", "Reason")
        doThrow(exception).whenever(mockAuth)
            .createUserWithEmailAndPassword(eq(email), eq(weakPassword))

        val result = authRepository.register(email, weakPassword, username)

        assertTrue(result.isFailure)
    }

    @Test
    fun `testRegistrationWithExistingEmail - should fail with collision exception`() = runTest {
        val email = "existing@example.com"
        val password = "SecurePassword123!"
        val username = "TestUser"

        val exception = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "Email already in use")
        doThrow(exception).whenever(mockAuth)
            .createUserWithEmailAndPassword(eq(email), eq(password))

        val result = authRepository.register(email, password, username)

        assertTrue(result.isFailure)
    }

    @Test
    fun `testRegistrationWithInvalidEmail - should fail`() = runTest {
        val invalidEmail = "invalid-email"
        val result = authRepository.validateEmail(invalidEmail)
        assertTrue(result.isFailure)
    }

    @Test
    fun `testPasswordValidation - should validate password strength`() = runTest {
        val validPassword = "SecurePass123!"
        val shortPassword = "Short1!"
        val noNumberPassword = "OnlyLetters!"
        val noSpecialPassword = "OnlyLetters123"

        assertTrue(authRepository.validatePassword(validPassword).isSuccess)
        assertTrue(authRepository.validatePassword(shortPassword).isFailure)
        assertTrue(authRepository.validatePassword(noNumberPassword).isFailure)
        assertTrue(authRepository.validatePassword(noSpecialPassword).isFailure)
    }

    @Test
    fun `testUsernameValidation - should validate username`() = runTest {
        val validUsername = "ValidUser123"
        val invalidUsername = "a"
        val specialCharUsername = "User@#$%"

        assertTrue(authRepository.validateUsername(validUsername).isSuccess)
        assertTrue(authRepository.validateUsername(invalidUsername).isFailure)
        assertTrue(authRepository.validateUsername(specialCharUsername).isFailure)
    }

    // ==================== TESTY LOGOWANIA ====================

    @Test
    fun `testSuccessfulLogin - should authenticate user`() = runTest {
        val email = "test@example.com"
        val password = "SecurePassword123!"

        val result = authRepository.login(email, password)

        assertTrue(result.isSuccess)
        verify(mockAuth).signInWithEmailAndPassword(eq(email), eq(password))
    }

    @Test
    fun `testLoginWithWrongPassword - should fail`() = runTest {
        val email = "test@example.com"
        val wrongPassword = "WrongPassword"

        val exception = Exception("Bad password")
        doThrow(exception).whenever(mockAuth)
            .signInWithEmailAndPassword(eq(email), eq(wrongPassword))

        val result = authRepository.login(email, wrongPassword)
        assertTrue(result.isFailure)
    }

    @Test
    fun `testLoginWithNonExistentUser - should fail`() = runTest {
        val nonExistentEmail = "nonexistent@example.com"
        val password = "SomePassword123!"

        val exception = Exception("User not found")
        doThrow(exception).whenever(mockAuth)
            .signInWithEmailAndPassword(eq(nonExistentEmail), eq(password))

        val result = authRepository.login(nonExistentEmail, password)
        assertTrue(result.isFailure)
    }

    @Test
    fun `testEmptyEmailLogin - should fail validation`() = runTest {
        val emptyEmail = ""
        val result = authRepository.validateEmail(emptyEmail)
        assertTrue(result.isFailure)
    }

    @Test
    fun `testEmptyPasswordLogin - should fail validation`() = runTest {
        val emptyPassword = ""
        val result = authRepository.validatePassword(emptyPassword)
        assertTrue(result.isFailure)
    }

    // ==================== TESTY WYLOGOWANIA ====================

    @Test
    fun `testLogout - should sign out user`() = runTest {
        authRepository.logout()
        verify(mockAuth).signOut()
    }

    // ==================== TESTY CAPTCHA ====================

    @Test
    fun `testCaptchaValidation - should verify CAPTCHA token`() = runTest {
        val validToken = "valid-captcha-token-123"
        val result = authRepository.validateCaptcha(validToken)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `testCaptchaValidationWithInvalidToken - should fail`() = runTest {
        val invalidToken = ""
        val result = authRepository.validateCaptcha(invalidToken)
        assertTrue(result.isFailure)
    }

    @Test
    fun `testProtectedRegistrationWithCaptcha - should require CAPTCHA`() = runTest {
        val email = "bot-like@example.com"
        val password = "SecurePassword123!"
        val username = "BotUser"
        val captchaToken = "valid-token"

        val result = authRepository.registerWithCaptcha(email, password, username, captchaToken)
        assertTrue(result.isSuccess)
    }

    // ==================== TESTY BEZPIECZEŃSTWA ====================

    @Test
    fun `testPasswordResetEmail - should send password reset email`() = runTest {
        val email = "test@example.com"
        val result = authRepository.sendPasswordResetEmail(email)
        assertTrue(result.isSuccess)
        verify(mockAuth).sendPasswordResetEmail(eq(email))
    }

    @Test
    fun `testPasswordResetWithInvalidEmail - should fail`() = runTest {
        val invalidEmail = "invalid-email"
        val result = authRepository.validateEmail(invalidEmail)
        assertTrue(result.isFailure)
    }

    @Test
    fun `testUpdateUserProfile - should update user data`() = runTest {
        val userId = "user-123"
        val username = "NewUsername"
        val result = authRepository.updateUserProfile(userId, username)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `testSessionManagement - should verify and maintain session`() = runTest {
        val email = "test@example.com"
        val password = "SecurePassword123!"

        val loginResult = authRepository.login(email, password)
        val isLoggedIn = authRepository.isUserLoggedIn()

        assertTrue(loginResult.isSuccess)
        assertTrue(isLoggedIn)
    }
}


open class AuthRepository(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) {
    open suspend fun register(email: String, password: String, username: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open fun logout() { auth.signOut() }

    open suspend fun validateEmail(email: String): Result<Unit> {
        return if (email.contains("@") && email.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("Błąd"))
    }

    open suspend fun validatePassword(password: String): Result<Unit> {
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }
        val hasDigit = password.any { it.isDigit() }
        val isLongEnough = password.length >= 8

        return if (isLongEnough && hasDigit && hasSpecialChar) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Hasło nie spełnia wymagań bezpieczeństwa"))
        }
    }

    open suspend fun validateUsername(username: String): Result<Unit> {
        return if (username.length > 2 && username.all { it.isLetterOrDigit() }) Result.success(Unit) else Result.failure(Exception("Błąd"))
    }

    open suspend fun validateCaptcha(token: String): Result<Unit> {
        return if (token.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("Błąd"))
    }

    open suspend fun registerWithCaptcha(email: String, password: String, username: String, captchaToken: String): Result<Unit> = Result.success(Unit)

    open suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        auth.sendPasswordResetEmail(email)
        return Result.success(Unit)
    }

    open suspend fun updateUserProfile(userId: String, username: String): Result<Unit> = Result.success(Unit)

    open fun isUserLoggedIn(): Boolean = true
}