package com.shubhasai.monkeymart.ui

import android.app.Activity
import com.shubhasai.monkeymart.analytics.ScreenTracker
import com.shubhasai.monkeymart.data.Product
import java.lang.ref.WeakReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DealLoaderTest {

    private val deal = Product(1, "Banana", "x", 6_000, "d", 10)

    @Test
    fun cancelledBeforeSourceCompletes_neverCallsBackOrShowsDialog() = runBlocking {
        val gate = CompletableDeferred<Product>()
        var loaded: Product? = null
        val messages = mutableListOf<String>()

        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            DealLoader.load(1, presenter = { messages += it }, source = { gate.await() }) { loaded = it }
        }
        job.cancelAndJoin()
        gate.complete(deal)

        assertNull(loaded)
        assertTrue(messages.isEmpty())
    }

    @Test
    fun completedLoad_reportsProductAndRequestsDialog() = runBlocking {
        var loaded: Product? = null
        val messages = mutableListOf<String>()

        DealLoader.load(1, presenter = { messages += it }, source = { deal }) { loaded = it }

        assertEquals(deal, loaded)
        assertEquals(listOf("10% off Banana, today only."), messages)
    }

    @Test
    fun screenTracker_doesNotRetainActivity() {
        var activity: Activity? = Activity()
        val ref = WeakReference(activity)
        val before = ScreenTracker.viewsThisSession()

        ScreenTracker.onScreenCreated(activity!!)
        assertEquals(before + 1, ScreenTracker.viewsThisSession())

        activity = null
        repeat(10) {
            System.gc()
            if (ref.get() == null) return@repeat
            Thread.sleep(50)
        }
        assertNull(ref.get())
    }
}
