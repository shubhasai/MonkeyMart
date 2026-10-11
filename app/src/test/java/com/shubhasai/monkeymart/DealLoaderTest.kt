package com.shubhasai.monkeymart

import com.shubhasai.monkeymart.data.Product
import com.shubhasai.monkeymart.ui.DealLoader
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DealLoaderTest {
    private val banana = Product(1, "Banana Bunch", "🍌", 6_000, "desc", 10)

    @Test
    fun dealMessageDeliveredExactlyOnce() = runBlocking {
        val messages = mutableListOf<String>()
        DealLoader.load(1, loadProduct = { banana }, onLoaded = {}, onDeal = { messages += it })
        assertEquals(listOf("10% off Banana Bunch, today only."), messages)
    }

    @Test
    fun cancelledCallerNeverShowsDeal() = runBlocking {
        val gate = CompletableDeferred<Product>()
        val messages = mutableListOf<String>()
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            DealLoader.load(1, loadProduct = { gate.await() }, onLoaded = {}, onDeal = { messages += it })
        }
        job.cancel()
        gate.complete(banana)
        job.join()
        assertTrue(messages.isEmpty())
    }
}
