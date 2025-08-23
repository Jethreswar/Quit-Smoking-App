package com.example.quitesmoking.model

/**
 * One “I almost bought” event that the user logs.
 *
 * @param timestamp  epoch-millis when the log happened (defaults to now)
 * @param brand      e-cig / vape brand or SKU the user skipped
 * @param units      number of units they would have bought
 * @param price      total retail price for this would-be purchase (USD)
 */
data class Purchase(
    val timestamp: Long = System.currentTimeMillis(),
    val brand: String = "",
    val units: Int = 0,
    val price: Double = 0.0
)
