package com.kp.momoney.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.kp.momoney.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GoogleAuthClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {
    
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    fun signInWithIntent(intent: Intent): Flow<Result<FirebaseUser>> = flow {
        emit(runCatching { signInWithIntentSuspend(intent) })
    }
    
    private suspend fun signInWithIntentSuspend(intent: Intent): FirebaseUser {
        // Get GoogleSignInAccount from intent
        val account = suspendCancellableCoroutine<GoogleSignInAccount> { continuation ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            task.addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val account = result.result
                    if (account != null) {
                        continuation.resume(account)
                    } else {
                        continuation.resumeWithException(IllegalStateException("Google Sign-In account is null"))
                    }
                } else {
                    continuation.resumeWithException(result.exception ?: Exception("Google Sign-In failed"))
                }
            }
        }
        
        // Create Firebase credential
        val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        
        // Sign in to Firebase
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.signInWithCredential(credential)
            task.addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        continuation.resume(user)
                    } else {
                        continuation.resumeWithException(IllegalStateException("User not available after sign in"))
                    }
                } else {
                    continuation.resumeWithException(result.exception ?: Exception("Firebase sign-in failed"))
                }
            }
        }
    }
}

