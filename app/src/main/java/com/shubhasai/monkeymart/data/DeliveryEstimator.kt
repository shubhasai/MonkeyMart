package com.shubhasai.monkeymart.data

import android.util.Log

/** How long delivery takes to a pincode. */
object DeliveryEstimator {

    /** Metro pincodes (first digit 1 to 6) get delivery in 2 days, everywhere else in 5. */
    fun days(pincode: String): Int? = try {
        if (pincode.trim().toInt() / 100_000 in 1..6) 2 else 5
    } catch (e: NumberFormatException) {
        Log.e("Delivery", "estimate failed", e)
        null
    }
}
