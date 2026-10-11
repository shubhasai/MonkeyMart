package com.shubhasai.monkeymart

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteTest {

    @Test
    fun routesRoundTripThroughSavedState() {
        listOf(Route.Shop, Route.Detail(3), Route.Checkout(7)).forEach {
            assertEquals(it, decodeRoute(it.encode()))
        }
    }

    @Test
    fun corruptSavedRouteFallsBackToShopInsteadOfCrashing() {
        assertEquals(Route.Shop, decodeRoute("detail:abc"))
        assertEquals(Route.Shop, decodeRoute("checkout:"))
        assertEquals(Route.Shop, decodeRoute("garbage"))
    }
}
