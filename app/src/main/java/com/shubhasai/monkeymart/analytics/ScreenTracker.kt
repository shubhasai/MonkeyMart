package com.shubhasai.monkeymart.analytics

import android.app.Activity

/** Counts screen views per session so we can see which screens people actually use. */
object ScreenTracker {

    private var screenViews = 0

    /** Call from every Activity's onCreate. Only counts; never retains the Activity. */
    @Suppress("UNUSED_PARAMETER")
    fun onScreenCreated(activity: Activity) {
        screenViews++
    }

    fun viewsThisSession(): Int = screenViews
}
