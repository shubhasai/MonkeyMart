package com.shubhasai.monkeymart.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.random.Random

data class OrderRequest(
    val productId: Int,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val city: String,
    val pincode: String,
)

/** Talks to the (fake) orders backend. Needs a working network connection, like the real one. */
class OrderRepository(context: Context) {

    private val connectivity = context.applicationContext.getSystemService(ConnectivityManager::class.java)

    /** Places the order and returns its id. */
    suspend fun submit(order: OrderRequest): String {
        if (!isOnline()) throw IOException("Unable to reach orders.monkeymart.example: no network")
        delay(1_500)
        return "MM-%06d".format(Random.nextInt(1_000_000))
    }

    private fun isOnline(): Boolean {
        val caps = connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
