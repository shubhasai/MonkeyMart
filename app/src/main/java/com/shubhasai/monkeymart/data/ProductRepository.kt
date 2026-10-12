package com.shubhasai.monkeymart.data

import kotlinx.coroutines.delay

data class Product(
    val id: Int,
    val name: String,
    val emoji: String,
    val pricePaise: Long,
    val description: String,
    val dealPercent: Int,
)

fun Product.priceLabel(): String = "₹%,d".format(pricePaise / 100)

/** Price after today's deal. */
fun Product.dealPricePaise(): Long = pricePaise * (100 - dealPercent) / 100

fun rupees(paise: Long): String = "₹%,d".format(paise / 100)

/** Dispatch time: big items come from the central warehouse, everything else from the nearest store. */
fun Product.dispatchLabel(): String =
    if (pricePaise > 50_000) "Ships in %d days".format(5) else "Ships in %d days"

/** In-memory catalogue that behaves like a slow backend. */
object ProductRepository {

    private val products = listOf(
        Product(1, "Banana Bunch", "🍌", 6_000, "Six ripe Robusta bananas, monkey approved.", 10),
        Product(2, "Coconut Water", "🥥", 4_500, "Tender coconut water, no added sugar.", 15),
        Product(3, "Mango Box", "🥭", 49_900, "A dozen Alphonso mangoes from Ratnagiri.", 20),
        Product(4, "Peanut Butter", "🥜", 29_900, "Crunchy, 1 kg jar.", 5),
        Product(5, "Jungle Hammock", "🏕️", 189_900, "Cotton hammock for two, holds 200 kg.", 25),
        Product(6, "Vine Swing", "🌿", 99_900, "Hand-braided swing for balconies.", 12),
        Product(7, "Monkey Plush", "🐒", 79_900, "Soft toy, 40 cm, machine washable.", 30),
        Product(8, "Leaf Umbrella", "🍃", 59_900, "Compact umbrella with a leaf print.", 8),
    )

    suspend fun all(): List<Product> {
        delay(800)
        return products
    }

    /** Product detail, including today's deal, from a slow endpoint. */
    suspend fun byId(id: Int): Product {
        delay(5_000)
        return products.first { it.id == id }
    }

    fun cached(id: Int): Product? = products.firstOrNull { it.id == id }
}
