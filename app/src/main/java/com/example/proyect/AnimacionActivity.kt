package com.example.proyect

// --- 1. ESTOS IMPORTS SON OBLIGATORIOS ---
import android.content.Intent                 // Para poder cambiar de pantalla
import android.graphics.drawable.Animatable   // Para detectar la animación
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AnimacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animacion)

        // --- PARTE A: LA ANIMACIÓN ---
        val imageView = findViewById<ImageView>(R.id.ivAnimacion)
        val drawable = imageView.drawable

        // Verificamos si la imagen es animada (AnimatedVectorDrawable)
        if (drawable is Animatable) {
            drawable.start()
        }

        // --- PARTE B: EL TEMPORIZADOR ---
        // Usamos Handler con Looper.getMainLooper() para asegurar que corra en la UI
        Handler(Looper.getMainLooper()).postDelayed({

            // Intentamos ir al Login
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Cerramos la animación
            finish()

        }, 3000) // 3 segundos
    }
}