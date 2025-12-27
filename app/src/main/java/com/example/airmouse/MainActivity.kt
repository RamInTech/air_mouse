package com.example.airmouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.airmouse.ui.theme.AirMouseTheme
import com.example.airmouse.sensors.SensorHandler

class MainActivity : ComponentActivity() {

    private lateinit var sensorHandler: SensorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize SensorHandler (NO lambda)
        sensorHandler = SensorHandler(this)

        enableEdgeToEdge()

        setContent {
            AirMouseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "🖱 Air Mouse Active",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.start()   // ✅ Start sensors
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.stop()    // ✅ Stop sensors
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AirMouseTheme {
        Greeting("🖱 Air Mouse Active")
    }
}
