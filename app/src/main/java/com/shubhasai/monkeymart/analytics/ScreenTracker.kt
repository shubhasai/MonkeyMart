package com.shubhasai.monkeymart.analytics

import android.app.Activity
import android.util.Log

/** Counts screen views per session so we can see which screens people actually use. */
object ScreenTracker {

    private val sessionScreens = mutableListOf<Activity>()

    /** Call from every Activity's onCreate. */
    fun onScreenCreated(activity: Activity) {
        sessionScreens += activity
        Log.d("ScreenTracker", "screen views this session: ${sessionScreens.size} (${activity.javaClass.simpleName})")
    }

    fun viewsThisSession(): Int = sessionScreens.size
}
