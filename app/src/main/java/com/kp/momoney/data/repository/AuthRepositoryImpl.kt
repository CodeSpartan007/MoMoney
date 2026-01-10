package com.kp.momoney.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kp.momoney.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Result<FirebaseUser>> {
        return authFlow { signIn(email, password) }
    }

    override fun register(email: String, password: String): Flow<Result<FirebaseUser>> {
        return authFlow { createUser(email, password) }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Result<Unit>> = flow {
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    private fun authFlow(action: suspend () -> FirebaseUser): Flow<Result<FirebaseUser>> = flow {
        emit(runCatching { action() })
    }

    private suspend fun signIn(email: String, password: String): FirebaseUser {
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.signInWithEmailAndPassword(email, password)
            task.addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resumeWithException(IllegalStateException("User not available after sign in"))
                    }
                } else {
                    continuation.resumeWithException(result.exception ?: Exception("Login failed"))
                }
            }

        }
    }

    private suspend fun createUser(email: String, password: String): FirebaseUser {
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.createUserWithEmailAndPassword(email, password)
            task.addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resumeWithException(IllegalStateException("User not available after registration"))
                    }
                } else {
                    continuation.resumeWithException(result.exception ?: Exception("Registration failed"))
                }
            }

        }
    }
}

