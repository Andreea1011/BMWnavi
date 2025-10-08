package com.example.bmwnavi.repo

import com.example.bmwnavi.model.SpeedLimit

interface RoadsRepository {
    suspend fun getSpeedLimit(lat: Double, lon: Double): SpeedLimit
}