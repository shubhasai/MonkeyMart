package com.shubhasai.monkeymart

import androidx.lifecycle.SavedStateHandle
import com.shubhasai.monkeymart.ui.DetailViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NavigationStateTest {
    @Test
    fun restoresRouteFromSavedHandle() {
        val handle = SavedStateHandle(mapOf("route" to "detail:3"))
        assertEquals(Route.Detail(3), NavigationViewModel(handle).route)
    }

    @Test
    fun navigationIsSavedAndRestoredByNewViewModel() {
        val handle = SavedStateHandle(mapOf("route" to "detail:3"))
        val first = NavigationViewModel(handle)
        first.go(Route.Checkout(3))
        assertEquals(Route.Checkout(3), NavigationViewModel(handle).route)
        first.go(Route.Shop)
        assertEquals(Route.Shop, NavigationViewModel(handle).route)
    }

    @Test
    fun defaultsToShop() {
        assertEquals(Route.Shop, NavigationViewModel(SavedStateHandle()).route)
    }

    @Test
    fun restoredDetailShowsProductImmediatelyWithoutDeal() {
        val handle = SavedStateHandle(mapOf("loadedProductId" to 3))
        val vm = DetailViewModel(handle)
        vm.load(3)
        assertEquals(3, vm.product?.id)
        assertNull(vm.pendingDeal)
    }
}
