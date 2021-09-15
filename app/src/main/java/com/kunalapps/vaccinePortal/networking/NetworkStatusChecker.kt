package com.kunalapps.vaccinePortal.networking

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class NetworkStatusChecker(private val context: Context) {

    fun isConnectedToInternet(): Boolean {
        return hasInternetConnection()
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } else {
            val network = connectivityManager.activeNetworkInfo

            return network?.type?.equals(ConnectivityManager.TYPE_WIFI) == true
                    || network?.type?.equals(ConnectivityManager.TYPE_MOBILE) == true
                    || network?.type?.equals(ConnectivityManager.TYPE_VPN) == true
        }

    }
}