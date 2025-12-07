package com.example.proyect

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

// ----------------------------------------------------------------------
// 1. RUTAS DE NAVEGACIÓN
// ----------------------------------------------------------------------

object Screen {
    const val SPLASH = "splash_screen"
    const val AUTH = "auth_screen" // Ruta que lanzará AuthActivity
    const val MAIN = "main_screen"
}

// ----------------------------------------------------------------------
// 2. FUNCIÓN PRINCIPAL DE NAVEGACIÓN
// ----------------------------------------------------------------------

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.SPLASH
    ) {
        // CORRECCIÓN: Se usa la sintaxis correcta sin 'route ='
        composable(Screen.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(Screen.AUTH) {
            AuthActivityStarter()
        }

        composable(Screen.MAIN) {
            // Este es un marcador de posición.
        }
    }
}

// ----------------------------------------------------------------------
// 3. COMPONENTE DE ANIMACIÓN (SPLASH SCREEN)
// ----------------------------------------------------------------------

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember {
        Animatable(0f)
    }

    LaunchedEffect(key1 = true) {
        // Animación de zoom: escala de 0 a 1.05 en 1 segundo
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(durationMillis = 1000)
        )
        // Espera 0.5 segundos (Usando el prefijo para evitar ambigüedad)
        kotlinx.coroutines.delay(500L)

        // Navega a la ruta de Autenticación después de la animación
        navController.popBackStack()
        navController.navigate(Screen.AUTH)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.ui.graphics.Color.White
    )
    {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // SINTAXIS CORREGIDA: Sin líneas vacías ni comentarios que rompan la DSL
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Logo de la Aplicación",
                modifier = Modifier
                    .size(250.dp)
                    .scale(scale.value)
            )
        }
    }
}

// ----------------------------------------------------------------------
// 4. LANZADOR DE LA ACTIVIDAD DE VISTAS (AuthActivity.kt)
// ----------------------------------------------------------------------

@Composable
fun AuthActivityStarter() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // Lanza tu Activity de login basada en XML (AuthActivity)
        val intent = Intent(context, AuthActivity::class.java)
        // Limpia la pila para evitar que el usuario regrese al Splash con el botón Atrás
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    // IMPORTANTE: Un Composable debe emitir algo, por lo que devolvemos un Surface vacío.
    Surface(modifier = Modifier.fillMaxSize()) {}
}
