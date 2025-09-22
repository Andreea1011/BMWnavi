@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.bmwnavi.ui.screen

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.animateFloatAsState
import com.example.bmwnavi.ui.component.AppScaffold

@Composable
fun GeneralStatsScreen(onBack: () -> Unit) {
    val vm: GeneralStatsViewModel = viewModel()
    val state by vm.state.collectAsState()

    // smooth value animations
    val speedA by animateFloatAsState(
        targetValue = state.speedKmh.toFloat(),
        animationSpec = tween(250),
        label = "speed"
    )
    val rpmA by animateFloatAsState(
        targetValue = state.rpm.toFloat(),
        animationSpec = tween(250),
        label = "rpm"
    )
    val fuelA by animateFloatAsState(
        targetValue = state.fuelPercent.toFloat(),
        animationSpec = tween(400),
        label = "fuel"
    )
    val coolantA by animateFloatAsState(
        targetValue = state.coolantC.toFloat(),
        animationSpec = tween(400),
        label = "coolant"
    )

    AppScaffold(title = "General stats", onBack = onBack) { p ->
        Row(
            Modifier
                .padding(p)
                .fillMaxSize()
        ) {
            NavigationRail {
                NavigationRailItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                    label = { Text("Speed") }
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedTabContent(
                    speedKmh = speedA.toDouble(),
                    rpm = rpmA.toDouble(),
                    fuelPercent = fuelA.toDouble(),
                    coolantC = coolantA.toDouble(),
                    remainingKm = state.remainingKm,
                    dateStr = state.dateStr,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
