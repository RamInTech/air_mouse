package com.example.airmouse

import android.os.Bundle
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.airmouse.sensors.SensorHandler
import com.example.airmouse.ui.theme.AirMouseTheme
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    private lateinit var sensorHandler: SensorHandler

    // ✅ Laser mouse ON / OFF state
    private var isLaserActive = false

    // ✅ Firebase reference
    private val databaseRef =
        FirebaseDatabase.getInstance().getReference("airmouse")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorHandler = SensorHandler(this)

        setContent {
            AirMouseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LaserMouseUI(
                        isActive = isLaserActive,
                        onToggle = { active ->
                            isLaserActive = active
                            databaseRef.child("laser_mouse_active").setValue(active)
                        },
                        onClick = {
                            if (isLaserActive) {
                                sensorHandler.triggerClick()
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.start()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.stop()
        databaseRef.child("laser_mouse_active").setValue(false)
    }
}

@Composable
fun LaserMouseUI(
    isActive: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (isActive)
                    "🔴 Laser Mouse ACTIVE\nTap button to click"
                else
                    "⚪ Laser Mouse INACTIVE",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { onToggle(!isActive) }) {
                Text(
                    text = if (isActive)
                        "Deactivate Laser Mouse"
                    else
                        "Activate Laser Mouse"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isActive) {
                Button(onClick = onClick) {
                    Text("Click")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLaserMouseUI() {
    AirMouseTheme {
        LaserMouseUI(
            isActive = true,
            onToggle = {},
            onClick = {}
        )
    }
}
