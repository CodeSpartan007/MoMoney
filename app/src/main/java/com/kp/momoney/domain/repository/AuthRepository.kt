package com.kp.momoney.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Result<FirebaseUser>>
    fun register(email: String, password: String): Flow<Result<FirebaseUser>>
    fun sendPasswordResetEmail(email: String): Flow<Result<Unit>>
    fun logout()
    fun getCurrentUser(): FirebaseUser?
}


