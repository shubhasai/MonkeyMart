package com.shubhasai.monkeymart.ui

import androidx.lifecycle.SavedStateHandle
import com.shubhasai.monkeymart.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val banana = Product(1, "Banana Bunch", "🍌", 6_000, "Ripe.", dealPercent = 10)

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsProductWithoutNeedingAnActivity() = runTest(dispatcher) {
        val vm = DetailViewModel(1, SavedStateHandle()) { banana }
        assertNull(vm.product)

        advanceUntilIdle()

        assertEquals(banana, vm.product)
        assertFalse(vm.dealDismissed)
    }

    @Test
    fun dismissedDealStaysDismissedAfterProcessDeath() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val vm = DetailViewModel(1, handle) { banana }
        advanceUntilIdle()
        vm.dismissDeal()

        val restored = DetailViewModel(1, SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })) { banana }

        assertTrue(restored.dealDismissed)
    }

    @Test
    fun failedLoadIsReportedAndRetryRecovers() = runTest(dispatcher) {
        var online = false
        val vm = DetailViewModel(1, SavedStateHandle()) { if (online) banana else throw IOException("no network") }
        advanceUntilIdle()

        assertTrue(vm.loadFailed)
        assertNull(vm.product)

        online = true
        vm.load()
        advanceUntilIdle()

        assertFalse(vm.loadFailed)
        assertNotNull(vm.product)
    }
}
