package com.example.bmwnavi.repo

import com.example.bmwnavi.data.GoogleRoadsApi
import com.example.bmwnavi.model.SpeedLimit
import com.example.bmwnavi.repo.RoadsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class GoogleRoadsRepository(
    private val api: GoogleRoadsApi,
    private val apiKey: String
) : RoadsRepository {

    // keep last few GPS points to make a short path
    private val ring = ArrayDeque<Pair<Double, Double>>() // lat, lon
    private val maxPoints = 5

    override suspend fun getSpeedLimit(lat: Double, lon: Double): SpeedLimit = withContext(Dispatchers.IO) {
        ring.addLast(lat to lon)
        while (ring.size > maxPoints) ring.removeFirst()

        val path = ring.joinToString("|") { "${it.first},${it.second}" }

        return@withContext try {
            val snap = api.snapToRoads(path = path, interpolate = true, key = apiKey)
            val placeIds = snap.snappedPoints.mapNotNull { it.placeId }.distinct()
            if (placeIds.isEmpty()) return@withContext SpeedLimit(null, "google_roads", System.currentTimeMillis())

            val speed = api.speedLimits(placeIds.joinToString(","), apiKey)
            val first = speed.speedLimits.firstOrNull()
            val kph = when (first?.units) {
                "MPH" -> first.speedLimit?.times(1.60934)?.roundToInt()
                else  -> first?.speedLimit?.roundToInt()
            }
            SpeedLimit(kph = kph, source = "google_roads", timestampMs = System.currentTimeMillis())
        } catch (e: Exception) {
            SpeedLimit(null, "google_roads", System.currentTimeMillis())
        }
    }
}