@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bmwnavi.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bmwnavi.ui.component.AppScaffold

private enum class GeneralTab { Speed, Fuel }

@Composable
fun GeneralStatsScreen(onBack: () -> Unit) {
    val vm: GeneralStatsViewModel = viewModel()
    val state by vm.state.collectAsState()

    var tab by rememberSaveable { mutableStateOf(GeneralTab.Speed) }

    val speedA by animateFloatAsState(state.speedKmh.toFloat(), tween(250), label = "speed")
    val rpmA by animateFloatAsState(state.rpm.toFloat(), tween(250), label = "rpm")
    val fuelA by animateFloatAsState(state.fuelPercent.toFloat(), tween(400), label = "fuel")
    val coolantA by animateFloatAsState(state.coolantC.toFloat(), tween(400), label = "coolant")

    AppScaffold(title = "General stats", onBack = onBack) { p ->
        Row(Modifier.padding(p).fillMaxSize()) {

            NavigationRail {
                NavigationRailItem(
                    selected = tab == GeneralTab.Speed,
                    onClick = { tab = GeneralTab.Speed },
                    icon = { Icon(Icons.Filled.Speed, null) },
                    label = { Text("Speed") }
                )
                NavigationRailItem(
                    selected = tab == GeneralTab.Fuel,
                    onClick = { tab = GeneralTab.Fuel },
                    icon = { Icon(Icons.Filled.LocalGasStation, null) },
                    label = { Text("Fuel") }
                )
            }

            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    GeneralTab.Speed -> {
                        SpeedTabContent(
                            speedKmh = speedA.toDouble(),
                            rpm = rpmA.toDouble(),
                            fuelPercent = fuelA.toDouble(),
                            coolantC = coolantA.toDouble(),
                            remainingKm = state.remainingKm,
                            dateStr = state.dateStr,
                            modifier = Modifier.fillMaxSize(),
                            speedLimitKph = 50
                        )
                    }
                    GeneralTab.Fuel -> {
                        FuelTabContent(
                            history = state.fuelHistory,
                            remainingKm = state.remainingKm ?: 0,
                            avgTripLPer100 = state.avgTripLPer100,
                            kmSinceFull = state.kmSinceFull,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
