package com.example.bmwnavi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bmwnavi.ui.component.AppScaffold
import com.example.bmwnavi.ui.component.PrimaryButton

@Composable
fun HomeScreen(
    onGeneralStats: () -> Unit
) {
    AppScaffold(title = "OBD Dashboard") { p ->
        Column(
            Modifier      // ✅ start with Modifier
                .padding(p)   // ✅ apply Scaffold-provided padding
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton("General stats", onGeneralStats)
            // later: DPF / Errors buttons here
        }
    }
}


