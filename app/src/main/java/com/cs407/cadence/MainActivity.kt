package com.cs407.cadence

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
import androidx.compose.ui.unit.dp
import com.cs407.cadence.ui.navigation.BottomNav
import com.cs407.cadence.ui.screens.HomeScreen
import com.cs407.cadence.ui.theme.CadenceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CadenceTheme {
                Scaffold(modifier =
                    Modifier
                        .fillMaxSize(),
                    bottomBar = { BottomNav() }
                ) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CadenceTheme {
        Scaffold(
            modifier =
                Modifier.fillMaxSize(),
            bottomBar = { BottomNav() }
        ) { innerPadding ->
            HomeScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}