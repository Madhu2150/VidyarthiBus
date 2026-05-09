package com.vidyarthibus.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vidyarthibus.data.model.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    private val usersCollection = firestore.collection("users")

    /** Save user profile after registration */
    suspend fun createUserProfile(
        name: String,
        email: String
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not authenticated"))

            val profile = UserProfile(
                uid          = uid,
                email        = email,
                name         = name,
                selectedRoute = "",
                createdAt    = System.currentTimeMillis()
            )

            usersCollection.document(uid).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get user profile */
    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not authenticated"))

            val doc = usersCollection.document(uid).get().await()
            val profile = doc.toObject(UserProfile::class.java)
                ?: return Result.failure(Exception("Profile not found"))

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Update last selected route */
    suspend fun updateSelectedRoute(routeId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Not authenticated"))

            usersCollection.document(uid)
                .update("selectedRoute", routeId)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}