package com.vidyarthibus.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    val currentUser: FirebaseUser? get() = auth.currentUser

    /**
     * FR-01: Only allow college email domains.
     * Adjust the domain list to match your institution.
     */
    private val allowedDomains = listOf(
        "@college.edu",
        "@vidyarthi.ac.in",
        "@vb.com",
        "@student.edu.in",
        "@gmail.com"
    )

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (!isCollegeEmail(email)) {
                return Result.failure(Exception("Only college email addresses are allowed."))
            }
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (!isCollegeEmail(email)) {
                return Result.failure(Exception("Only college email addresses are allowed."))
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user!!.sendEmailVerification().await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() = auth.signOut()

    private fun isCollegeEmail(email: String): Boolean =
        allowedDomains.any { email.lowercase().endsWith(it) }
}