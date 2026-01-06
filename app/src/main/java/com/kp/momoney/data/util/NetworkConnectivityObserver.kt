package com.kp.momoney.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ConnectivityObserver using Android's ConnectivityManager.
 * Observes network connectivity changes and emits status updates via Flow.
 */
@Singleton
class NetworkConnectivityObserver @Inject constructor(
    @ApplicationContext private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(ConnectivityObserver.Status.Available)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                trySend(ConnectivityObserver.Status.Losing)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(ConnectivityObserver.Status.Lost)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                trySend(ConnectivityObserver.Status.Unavailable)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Emit initial status
        val currentStatus = getCurrentConnectivityStatus()
        trySend(currentStatus)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Gets the current connectivity status synchronously.
     */
    private fun getCurrentConnectivityStatus(): ConnectivityObserver.Status {
        val network = connectivityManager.activeNetwork ?: return ConnectivityObserver.Status.Unavailable
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectivityObserver.Status.Unavailable

        return when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> {
                ConnectivityObserver.Status.Available
            }
            else -> ConnectivityObserver.Status.Unavailable
        }
    }
}

