package com.example.airmouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.airmouse.sensors.SensorHandler
import com.example.airmouse.ui.theme.AirMouseTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorHandler: SensorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorHandler = SensorHandler(this)

        enableEdgeToEdge()

        setContent {
            AirMouseTheme {
                // The Scaffold is now clickable and triggers the click in the handler
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { sensorHandler.triggerClick() }
                ) { innerPadding ->
                    Greeting(
                        name = "🖱️ Air Mouse Active\nTap screen to click",
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
    }
}

// Updated the Greeting to center the text for a cleaner look
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AirMouseTheme {
        Greeting("🖱️ Air Mouse Active\nTap screen to click")
    }
}
