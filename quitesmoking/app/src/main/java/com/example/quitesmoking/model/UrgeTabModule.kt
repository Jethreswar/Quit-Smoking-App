package com.example.quitesmoking.model

data class UrgeResponse(
    val urgeStrength: String = "",
    val urgeReason: String = "",
    val timestamp: Long = 0L
)