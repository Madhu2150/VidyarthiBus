package com.vidyarthibus.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.vidyarthibus.data.model.BusRoute
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RouteRepository @Inject constructor(
    private val db: FirebaseDatabase
) {

    private val routesRef = db.getReference("routes")

    /** FR-02: Real-time list of all registered routes */
    fun observeAllRoutes(): Flow<List<BusRoute>> = callbackFlow {
        val listener = routesRef.addValueEventListener(object :
            com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val routes = snapshot.children.mapNotNull {
                    it.getValue(BusRoute::class.java)
                }
                trySend(routes)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { routesRef.removeEventListener(listener) }
    }

    /** FR-09: Observe cancellation status for a single route */
    fun observeRoute(routeId: String): Flow<BusRoute?> = callbackFlow {
        val ref = routesRef.child(routeId)
        val listener = ref.addValueEventListener(object :
            com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                trySend(snapshot.getValue(BusRoute::class.java))
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    /** FR-09: Route captain posts cancellation */
    suspend fun postCancellation(routeId: String, message: String): Result<Unit> {
        return try {
            routesRef.child(routeId).updateChildren(
                mapOf(
                    "isCancelled" to true,
                    "cancellationMessage" to message
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}