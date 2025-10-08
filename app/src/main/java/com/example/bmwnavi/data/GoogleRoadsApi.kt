package com.example.bmwnavi.data


import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleRoadsApi {
    // 1) Snap to Roads (returns snappedPoints with placeId)
    @GET("v1/snapToRoads")
    suspend fun snapToRoads(
        @Query("path") path: String,           // "lat,lon|lat,lon|..."
        @Query("interpolate") interpolate: Boolean = true,
        @Query("key") key: String
    ): SnapResponse

    // 2) Speed Limits (by placeId list)
    @GET("v1/speedLimits")
    suspend fun speedLimits(
        @Query("placeId") placeIdsCsv: String, // "ChIJ...,ChIJ..."
        @Query("key") key: String
    ): SpeedResponse
}

// --- DTOs (simplified) ---
data class SnapResponse(val snappedPoints: List<SnappedPoint>)
data class SnappedPoint(val placeId: String?)
data class SpeedResponse(val speedLimits: List<SpeedItem>)
data class SpeedItem(val placeId: String, val speedLimit: Double?, val units: String?) // units could be "KPH"|"MPH"