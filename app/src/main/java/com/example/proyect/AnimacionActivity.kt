package com.example.proyect

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AnimacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animacion)

        // 1. Iniciar animación de la imagen (si es animada)
        val imageView = findViewById<ImageView>(R.id.ivAnimacion)
        val drawable = imageView.drawable
        if (drawable is Animatable) {
            drawable.start()
        }

        // 2. TEMPORIZADOR: Espera 3 segundos (3000ms) y cambia de pantalla
        Handler(Looper.getMainLooper()).postDelayed({

            // Ir al Login (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Cerrar esta pantalla para no volver
            finish()

        }, 3000)
    }
}