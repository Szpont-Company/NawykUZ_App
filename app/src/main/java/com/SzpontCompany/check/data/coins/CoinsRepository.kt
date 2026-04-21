package com.SzpontCompany.check.data.coins

import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await

class CoinsRepository() {
    private val functions = Firebase.functions("europe-central2")

    suspend fun earnCoins(actionType: String) : Result<Int> {
        return try {
            val result = functions
                .getHttpsCallable("earnCoins")
                .call(mapOf("actionType" to actionType))
                .await()

            val data = result.data as Map<String, Any>
            val addedCoins = data["added"] as Int

            Result.success(addedCoins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}