package com.aikukisna.app.data.local

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ConectividadHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hayConexion(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val red = cm.activeNetwork ?: return false
        val capacidades = cm.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}