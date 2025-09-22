package com.example.bmwnavi.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import kotlin.math.abs
import kotlin.math.sin

class GeneralStatsViewModel : ViewModel() {

    data class UiState(
        val speedKmh: Double = 0.0,     // 0..260
        val rpm: Double = 800.0,        // 0..7000
        val fuelPercent: Double = 55.0, // 0..100
        val coolantC: Double = 90.0,    // 0..120
        val remainingKm: Int? = null,
        val dateStr: String = ""
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val df = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault())

    init {
        // Mock animation loop
        viewModelScope.launch {
            var t = 0.0
            while (true) {
                t += 0.15
                val speed = (130 + 120 * sin(t)).coerceIn(0.0, 260.0)
                val rpm   = (2500 + 2200 * sin(t + 0.7)).coerceIn(0.0, 7000.0)
                val fuel  = (55 + 10 * sin(t/3)).coerceIn(0.0, 100.0)
                val coolant = (90 + 5 * sin(t/4)).coerceIn(0.0, 120.0)

                // Example remaining range calc (optional placeholder)
                val tankLiters = 45.0
                val avgLPer100 = 5.5
                val remaining = ((fuel/100 * tankLiters) / avgLPer100 * 100).toInt()

                _state.value = UiState(
                    speedKmh = speed,
                    rpm = rpm,
                    fuelPercent = fuel,
                    coolantC = coolant,
                    remainingKm = remaining,
                    dateStr = df.format(Date())
                )
                delay(120)
            }
        }
    }
}
