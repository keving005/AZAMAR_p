package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class DoctorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        // 1. VINCULAR LAS VISTAS (IDs del XML Premium)
        val tvNombreDoctor = findViewById<TextView>(R.id.tvDoctorName)
        val cardCitas = findViewById<MaterialCardView>(R.id.cardCitasAgendadas)
        val cardAgenda = findViewById<MaterialCardView>(R.id.cardAgenda)
        val btnSalir = findViewById<FloatingActionButton>(R.id.fabSalir)
        val imgAvatar = findViewById<ImageView>(R.id.ivDoctorAvatar)

        // 2. RECIBIR DATOS DEL LOGIN
        // Si no llega el nombre, ponemos "Doctor" por defecto.
        val nombreRecibido = intent.getStringExtra("NOMBRE_USUARIO") ?: "Colega"

        // Asignamos el nombre al TextView grande
        tvNombreDoctor.text = nombreRecibido

        // 3. FUNCIONALIDAD DE LOS BOTONES

        // --- Botón Rosa: Ver Pacientes ---
        cardCitas.setOnClickListener {
            startActivity(Intent(this, DoctorCitasActivity::class.java))
        }

        // --- Botón Turquesa: Agenda ---
        cardAgenda.setOnClickListener {
            // Abrir la pantalla de configuración de agenda
            val intent = Intent(this, DoctorAgendaActivity::class.java)
            startActivity(intent)
        }

        // --- Clic en la foto (Opcional) ---
        imgAvatar.setOnClickListener {
            Toast.makeText(this, "Perfil del Dr. $nombreRecibido", Toast.LENGTH_SHORT).show()
        }

        // --- Botón Flotante: Salir ---
        btnSalir.setOnClickListener {
            // 1. Cerrar sesión en Firebase
            FirebaseAuth.getInstance().signOut()

            // 2. Regresar al Login y borrar historial para que no puedan volver atrás
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}