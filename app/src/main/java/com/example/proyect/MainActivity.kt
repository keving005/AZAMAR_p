package com.example.proyect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Llama a la función principal de navegación (definida en Navigation.kt)
        setContent {
            AppNavigation()
        }
    }
}