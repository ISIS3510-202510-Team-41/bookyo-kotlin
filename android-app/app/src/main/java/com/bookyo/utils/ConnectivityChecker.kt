package com.bookyo.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Utility class to check and monitor network connectivity
 */
class ConnectivityChecker(context: Context) {

    private val connectivityManager = context.getSystemService<ConnectivityManager>()

    /**
     * Check if the device currently has an active internet connection
     */
    fun isConnected(): Boolean {
        val networkCapabilities = connectivityManager?.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    /**
     * Observe connectivity changes as a Flow
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        if (connectivityManager == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        // Initial connectivity status
        trySend(isConnected())

        // Callback when network state changes
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch { channel.send(true) }
            }

            override fun onLost(network: Network) {
                launch { channel.send(isConnected()) }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // Check if network has internet capability
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                launch { channel.send(hasInternet) }
            }
        }

        try {
            // Use registerDefaultNetworkCallback instead of registerNetworkCallback
            // This doesn't require CHANGE_NETWORK_STATE permission
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Fallback to just returning the current state
            trySend(isConnected())
        }

        // Cleanup when Flow collector is cancelled
        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                // Ignore unregister errors
            }
        }
    }.distinctUntilChanged()
}

@Composable
fun rememberConnectivityChecker(context: Context): ConnectivityChecker {
    return remember { ConnectivityChecker(context) }
}