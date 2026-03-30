package com.SzpontCompany.check.ui.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveUserData(uid: String, name: String, email: String) {
        val userData = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email
        )
        firestore
            .collection("users")
            .document(uid)
            .set(userData, SetOptions.merge())
            .await()
    }
}