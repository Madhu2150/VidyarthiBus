package com.vidyarthibus.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vidyarthibus.data.model.CrowdReport
import com.vidyarthibus.data.model.CrowdStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CrowdRepository @Inject constructor(
    private val db: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val reportsRef = db.getReference("crowd_reports")

    /**
     * FR-03 / FR-04: Observe live crowd status for a route.
     * Computes aggregate count from active (non-expired) reports.
     * Report TTL is enforced server-side by Cloud Function (FR-06).
     */
    fun observeCrowdStatus(routeId: String): Flow<CrowdStatus> = callbackFlow {
        val ref = reportsRef.child(routeId)
        val listener = ref.addValueEventListener(object :
            com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val now = System.currentTimeMillis()
                val ttl = 15 * 60 * 1000L  // 15 min in ms

                // Client-side filter as safety net; server Cloud Function is primary
                val activeCount = snapshot.children.count { child ->
                    val report = child.getValue(CrowdReport::class.java)
                    report != null && (now - report.timestamp) < ttl
                }

                trySend(
                    CrowdStatus(
                        routeId = routeId,
                        activeReporters = activeCount,
                        lastUpdated = now
                    )
                )
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * FR-04 / FR-05: Submit a crowd report.
     * proximityVerified must be true (checked by LocationService before calling).
     * NFR-10: Only a boolean is stored — never raw coordinates.
     */
    suspend fun submitReport(
        routeId: String,
        proximityVerified: Boolean
    ): Result<Unit> {
        if (!proximityVerified) {
            return Result.failure(Exception("You must be near the bus route to report."))
        }

        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated."))

        return try {
            // Use userId as key so one user = one active report per route
            val hashedKey = userId.hashCode().toString()
            val report = CrowdReport(
                reportId = hashedKey,
                routeId = routeId,
                userId = hashedKey,           // anonymised
                timestamp = System.currentTimeMillis(),
                proximityVerified = true
            )
            reportsRef.child(routeId).child(hashedKey).setValue(report).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Remove a user's own report (e.g., they got off the bus) */
    suspend fun removeReport(routeId: String): Result<Unit> {
        val userId = auth.currentUser?.uid
            ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val hashedKey = userId.hashCode().toString()
            reportsRef.child(routeId).child(hashedKey).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** FR-07: Load shared auto contacts for a route */
    fun observeAlternatives(routeId: String): Flow<List<com.vidyarthibus.data.model.SharedAutoContact>> =
        callbackFlow {
            val ref = db.getReference("alternatives").child(routeId)
            val listener = ref.addValueEventListener(object :
                com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val contacts = snapshot.children.mapNotNull {
                        it.getValue(com.vidyarthibus.data.model.SharedAutoContact::class.java)
                    }
                    trySend(contacts)
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    close(error.toException())
                }
            })
            awaitClose { ref.removeEventListener(listener) }
        }
}