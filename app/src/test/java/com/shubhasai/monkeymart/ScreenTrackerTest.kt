package com.shubhasai.monkeymart

import com.shubhasai.monkeymart.analytics.ScreenTracker
import com.shubhasai.monkeymart.data.Product
import com.shubhasai.monkeymart.ui.DealLoader
import com.shubhasai.monkeymart.ui.LoadedDeal
import java.lang.ref.WeakReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTrackerTest {

    private fun createScreens(): List<WeakReference<Any>> =
        List(3) { WeakReference(Any().also { ScreenTracker.onScreenCreated(it) }) }

    @Test
    fun countsScreensWithoutRetainingThem() {
        val before = ScreenTracker.viewsThisSession()
        val refs = createScreens()
        assertEquals(before + 3, ScreenTracker.viewsThisSession())

        repeat(5) {
            System.gc()
            Thread.sleep(20)
        }
        refs.forEach { assertNull("tracker leaked a screen", it.get()) }
    }
}

class DealLoaderTest {

    private val dealProduct = Product(1, "Banana Bunch", "x", 6_000, "d", 10)

    @Test
    fun cancelledLoadDeliversNothing() = runBlocking {
        val gate = CompletableDeferred<Product>()
        var delivered: LoadedDeal? = null

        val job = launch(Dispatchers.Unconfined) {
            delivered = DealLoader.load(1) { gate.await() }
        }
        job.cancelAndJoin()
        gate.complete(dealProduct)

        assertTrue(job.isCancelled)
        assertNull(delivered)
    }

    @Test
    fun completedLoadBuildsDealMessage() = runBlocking {
        val result = DealLoader.load(1) { dealProduct }
        assertEquals(dealProduct, result.product)
        assertEquals("10% off Banana Bunch, today only.", result.dealMessage)
    }

    @Test
    fun productWithoutDealHasNoMessage() = runBlocking {
        val result = DealLoader.load(1) { dealProduct.copy(dealPercent = 0) }
        assertNull(result.dealMessage)
    }
}
