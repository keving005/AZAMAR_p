package com.example.proyect.model

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri // Import para la extensión toUri()
import com.example.proyect.R

class UbicacionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val btnComoLlegar = findViewById<Button>(R.id.btnComoLlegar)
        btnComoLlegar.setOnClickListener {
            abrirUbicacionEnMapa()
        }

        val btnRegresar = findViewById<Button>(R.id.btnRegresar)
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun abrirUbicacionEnMapa() {
        // Usamos la extensión KTX `toUri()` para un código más limpio
        val uri = "$URI_GEO_BASE$DIRECCION_HOSPITAL".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "No se encontró una aplicación de mapas para abrir la ubicación.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val DIRECCION_HOSPITAL = "Hospital General de México, Ciudad de México"
        private const val URI_GEO_BASE = "geo:0,0?q="
    }
}

