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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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

internal fun Route.encode(): String = when (this) {
    Route.Shop -> "shop"
    is Route.Detail -> "detail:$productId"
    is Route.Checkout -> "checkout:$productId"
}

internal fun decodeRoute(raw: String): Route = when {
    raw.startsWith("detail:") -> raw.removePrefix("detail:").toIntOrNull()?.let { Route.Detail(it) } ?: Route.Shop
    raw.startsWith("checkout:") -> raw.removePrefix("checkout:").toIntOrNull()?.let { Route.Checkout(it) } ?: Route.Shop
    else -> Route.Shop
}

/** Holds the current route in [SavedStateHandle] so it is restored after process death. */
class NavigationViewModel(private val handle: SavedStateHandle) : ViewModel() {
    var route by mutableStateOf(decodeRoute(handle.get<String>(ROUTE_KEY) ?: Route.Shop.encode()))
        private set

    fun go(to: Route) {
        route = to
        handle[ROUTE_KEY] = to.encode()
    }

    private companion object {
        const val ROUTE_KEY = "route"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MonkeyMartApp(nav: NavigationViewModel = viewModel()) {
    val route = nav.route
    fun go(to: Route) = nav.go(to)

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
