package com.example.quitesmoking

/**
 * One “I almost bought” event that the user logs.
 */
data class Purchase(
    val timestamp: Long = System.currentTimeMillis(),
    val brand: String = "",
    val units: Int = 0,
    val price: Double = 0.0
)