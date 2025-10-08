package com.example.bmwnavi.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.sin

class GeneralStatsViewModel : ViewModel() {

    data class UiState(
        val speedKmh: Double = 0.0,        // 0..260
        val rpm: Double = 800.0,           // 0..7000
        val fuelPercent: Double = 55.0,    // 0..100
        val coolantC: Double = 90.0,       // 0..120
        val remainingKm: Int? = null,
        val dateStr: String = "",

        // Fuel tab extras (okay if unused for now)
        val fuelHistory: List<Pair<Float, Float>> = emptyList(),
        val kmSinceFull: Double = 0.0,
        val avgTripLPer100: Double = 0.0,

        // Speed limit for the pill (null hides it)
        val speedLimitKph: Int? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val df = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault())

    // Simple mock “engine” so gauges keep moving. Replace later with OBD + GPS.
    private var t = 0.0
    private var kmSinceFull = 0.0
    private var litersSinceFull = 0.0
    private val tankLiters = 45.0

    init {
        viewModelScope.launch {
            while (true) {
                t += 0.15

                // Smooth oscillations (replace with real signals later)
                val speed = (110 + 90 * sin(t)).coerceIn(0.0, 260.0)
                val rpm   = (2100 + 1800 * sin(t + 0.7)).coerceIn(600.0, 7000.0)
                val coolant = (88 + 6 * sin(t / 4)).coerceIn(70.0, 110.0)

                // distance in this tick
                val dtHours = 0.12 / 3600.0
                val dKm = speed * dtHours
                kmSinceFull += dKm

                // mock instant consumption (related to rpm a bit)
                val instantL100 = (4.8 + (rpm / 7000.0) * 3.2 + 0.8 * sin(t + 1.1))
                    .coerceIn(3.5, 11.5)

                val dLiters = instantL100 * (dKm / 100.0)
                litersSinceFull += dLiters

                // fuel %
                val prevFuelPct = _state.value.fuelPercent
                val currentLiters = (prevFuelPct / 100.0) * tankLiters - dLiters
                val fuelPct = (currentLiters / tankLiters * 100.0).coerceIn(0.0, 100.0)

                val avgTrip = if (kmSinceFull > 0.001) (litersSinceFull / kmSinceFull) * 100.0 else 0.0
                val remaining = (((fuelPct/100.0) * tankLiters) / max(avgTrip, 0.1) * 100.0).toInt()

                _state.update {
                    it.copy(
                        speedKmh = speed,
                        rpm = rpm,
                        fuelPercent = fuelPct,
                        coolantC = coolant,
                        remainingKm = remaining,
                        dateStr = df.format(Date()),
                        avgTripLPer100 = avgTrip,
                        kmSinceFull = kmSinceFull,
                        // keep showing a test speed limit so you see the pill; set to null when wired
                        speedLimitKph = it.speedLimitKph ?: 50
                    )
                }

                delay(120) // ~8 fps
            }
        }
    }
}
