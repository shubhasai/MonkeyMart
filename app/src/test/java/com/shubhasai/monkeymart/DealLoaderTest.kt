package com.shubhasai.monkeymart

import com.shubhasai.monkeymart.data.Product
import com.shubhasai.monkeymart.ui.DealLoader
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DealLoaderTest {
    private val product = Product(1, "Banana Bunch", "🍌", 6_000, "desc", 10)

    @Test
    fun cancelledLoad_neverShowsDealOrDeliversProduct() = runBlocking {
        val loaded = mutableListOf<Product>()
        val deals = mutableListOf<String>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            DealLoader.load(
                productId = 1,
                fetch = { delay(200); product },
                onLoaded = { loaded += it },
                showDeal = { deals += it },
            )
        }
        // Screen is torn down (e.g. rotation) while the fetch is in flight.
        job.cancel()
        job.join()
        delay(400)

        assertTrue(loaded.isEmpty())
        assertTrue(deals.isEmpty())
    }

    @Test
    fun completedLoad_showsDeal() = runBlocking {
        val deals = mutableListOf<String>()
        DealLoader.load(1, fetch = { product }, onLoaded = {}, showDeal = { deals += it })
        assertEquals(listOf("10% off Banana Bunch, today only."), deals)
    }
}
