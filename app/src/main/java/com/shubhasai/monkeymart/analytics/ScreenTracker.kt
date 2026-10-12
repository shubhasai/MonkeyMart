package com.shubhasai.monkeymart.analytics

import java.util.concurrent.atomic.AtomicInteger

/** Counts screen views per session so we can see which screens people actually use. */
object ScreenTracker {

    private val screenViews = AtomicInteger()

    /** Call from every Activity's onCreate. Only counts; never retains [screen]. */
    fun onScreenCreated(@Suppress("UNUSED_PARAMETER") screen: Any) {
        screenViews.incrementAndGet()
    }

    fun viewsThisSession(): Int = screenViews.get()
}
