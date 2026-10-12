package com.shubhasai.monkeymart

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.fragment.app.FragmentActivity
import com.shubhasai.monkeymart.analytics.ScreenTracker
import com.shubhasai.monkeymart.ui.CheckoutScreen
import com.shubhasai.monkeymart.ui.DetailScreen
import com.shubhasai.monkeymart.ui.ShopScreen
import com.shubhasai.monkeymart.ui.theme.MonkeyMartTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenTracker.onScreenCreated(this)
        enableEdgeToEdge()
        setContent {
            MonkeyMartTheme {
                MonkeyMartApp()
            }
        }
    }
}

/** Screens are addressed by a small route string so the back stack survives recreation. */
sealed interface Route {
    data object Shop : Route
    data class Detail(val productId: Int) : Route
    data class Checkout(val productId: Int) : Route
}

private fun Route.encode(): String = when (this) {
    Route.Shop -> "shop"
    is Route.Detail -> "detail:$productId"
    is Route.Checkout -> "checkout:$productId"
}

private fun decodeRoute(raw: String): Route = when {
    raw.startsWith("detail:") -> Route.Detail(raw.removePrefix("detail:").toInt())
    raw.startsWith("checkout:") -> Route.Checkout(raw.removePrefix("checkout:").toInt())
    else -> Route.Shop
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MonkeyMartApp() {
    var rawRoute by rememberSaveable { mutableStateOf(Route.Shop.encode()) }
    val route = decodeRoute(rawRoute)
    fun go(to: Route) { rawRoute = to.encode() }

    BackHandler(enabled = route != Route.Shop) {
        go(if (route is Route.Checkout) Route.Detail(route.productId) else Route.Shop)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true },
        topBar = { TopAppBar(title = { Text("MonkeyMart") }) },
    ) { padding ->
        val content = Modifier.padding(padding)
        when (route) {
            Route.Shop -> ShopScreen(onProductClick = { go(Route.Detail(it)) }, modifier = content)
            is Route.Detail -> DetailScreen(route.productId, onBuyNow = { go(Route.Checkout(it)) }, modifier = content)
            is Route.Checkout -> CheckoutScreen(route.productId, modifier = content)
        }
    }
}
