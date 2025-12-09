package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SegundaFor : AppCompatActivity() {

    // Declaración de los elementos
    private lateinit var etTipoCita: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnHistorial: Button
    private lateinit var btnUbicacion: Button
    private lateinit var btnSalir: Button
    private lateinit var btnSiguiente: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_segunda_for) // Asegúrate de que este sea el nombre correcto de tu layout

        // Vincular los elementos con el XML
        etTipoCita = findViewById(R.id.etTipoCita)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnUbicacion = findViewById(R.id.btnUbicacion)
        btnSalir = findViewById(R.id.btnSalir)
        btnSiguiente = findViewById(R.id.btnSiguiente)

        // Configuración de los listeners para los botones
        btnGuardar.setOnClickListener {
            val tipoCita = etTipoCita.text.toString()
            if (tipoCita.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese el tipo de cita", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Cita guardada", Toast.LENGTH_SHORT).show()
            }
        }

        btnHistorial.setOnClickListener {
            Toast.makeText(this, "Mostrando historial", Toast.LENGTH_SHORT).show()
        }

        btnUbicacion.setOnClickListener {
            Toast.makeText(this, "Mostrando ubicación", Toast.LENGTH_SHORT).show()
        }

        btnSalir.setOnClickListener {

            finish()
        }

        btnSiguiente.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Cambia 'OtraActividad' por la clase de tu siguiente pantalla
            startActivity(intent)
        }
    }
}
