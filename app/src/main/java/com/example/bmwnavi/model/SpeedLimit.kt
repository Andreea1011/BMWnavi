package com.example.bmwnavi.model

data class SpeedLimit(
    val kph: Int?,                 // null if unknown
    val source: String,            // "google_roads" | "here" | ...
    val timestampMs: Long          // when we fetched it
)