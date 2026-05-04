package com.SzpontCompany.check.data.map

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RouteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveRoute(route: Route) {
        val uid = auth.currentUser?.uid ?: throw Exception("Brak zalogowanego użytkownika")

        val docRef = firestore.collection("users").document(uid).collection("routes").document()
        val routeWithId = route.copy(id = docRef.id)

        docRef.set(routeWithId).await()
    }

    fun getRoutesHistory(): Flow<List<Route>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid).collection("routes")
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val routes = snapshot.documents.mapNotNull { it.toObject(Route::class.java) }
                    trySend(routes)
                }
            }

        awaitClose { listener.remove() }
    }
}