package com.kp.momoney.util

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.FirebaseNetworkException

/**
 * Maps Firebase authentication exceptions to user-friendly error messages.
 * 
 * @param e The throwable exception to map
 * @return A human-readable error message
 */
fun mapAuthException(e: Throwable): String {
    return when {
        e is FirebaseNetworkException -> "Network error. Check your connection."
        e is FirebaseAuthException -> {
            when (e.errorCode) {
                "ERROR_USER_NOT_FOUND",
                "ERROR_USER_DISABLED" -> "Account not found. Please sign up."
                "ERROR_WRONG_PASSWORD",
                "ERROR_INVALID_EMAIL",
                "ERROR_INVALID_CREDENTIAL" -> "Incorrect email or password."
                "ERROR_EMAIL_ALREADY_IN_USE",
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "An account with this email already exists."
                else -> "An unknown error occurred. Please try again."
            }
        }
        else -> "An unknown error occurred. Please try again."
    }
}

