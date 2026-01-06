package com.kp.momoney.data.util

import kotlinx.coroutines.flow.Flow

/**
 * Interface for observing network connectivity status.
 */
interface ConnectivityObserver {
    /**
     * Observes the network connectivity status.
     * @return Flow of connectivity Status
     */
    fun observe(): Flow<Status>

    /**
     * Enum representing different network connectivity states.
     */
    enum class Status {
        Available,    // Network is available
        Unavailable,  // Network is unavailable
        Losing,       // Network is losing connection
        Lost          // Network connection is lost
    }
}

