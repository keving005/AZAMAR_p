package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton // IMPORTANTE: Cambio aquí
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DoctorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor)

        // 1. VINCULAR LAS VISTAS
        val tvNombreDoctor = findViewById<TextView>(R.id.tvDoctorName)
        val tvFechaHoy = findViewById<TextView>(R.id.tvFechaHoy) // Nuevo campo opcional
        val cardCitas = findViewById<MaterialCardView>(R.id.cardCitasAgendadas)
        val cardAgenda = findViewById<MaterialCardView>(R.id.cardAgenda)
        // OJO: Ahora es ExtendedFloatingActionButton para que tenga texto "Cerrar sesión"
        val btnSalir = findViewById<ExtendedFloatingActionButton>(R.id.fabSalir)
        val imgAvatar = findViewById<ImageView>(R.id.ivDoctorAvatar)

        // 2. RECIBIR DATOS
        val nombreRecibido = intent.getStringExtra("NOMBRE_USUARIO") ?: "Colega"
        tvNombreDoctor.text = nombreRecibido

        // Poner fecha actual bonita debajo del nombre
        val fecha = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date())
        tvFechaHoy.text = fecha.capitalize()

        // 3. LISTENERS

        cardCitas.setOnClickListener {
            startActivity(Intent(this, DoctorCitasActivity::class.java))
        }

        cardAgenda.setOnClickListener {
            startActivity(Intent(this, DoctorAgendaActivity::class.java))
        }

        imgAvatar.setOnClickListener {
            Toast.makeText(this, "Perfil médico activo", Toast.LENGTH_SHORT).show()
        }

        btnSalir.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}