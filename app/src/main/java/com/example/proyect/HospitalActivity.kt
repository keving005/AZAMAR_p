package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // IMPORTANTE: Importar CardView
import com.google.firebase.auth.FirebaseAuth

class HospitalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)

        // Vinculación de vistas
        // NOTA: El título y subtítulo siguen siendo TextView
        val tvTitulo = findViewById<TextView>(R.id.tvTituloHospital)
        val tvSubtitulo = findViewById<TextView>(R.id.tvSubtitulo)

        // CAMBIO IMPORTANTE: Ahora son CardView, no Button
        val cardDoctores = findViewById<CardView>(R.id.btnGestionarDoctores)
        val cardPacientes = findViewById<CardView>(R.id.btnGestionarPacientes)
        val cardFarmacia = findViewById<CardView>(R.id.btnFarmacia)

        // El botón de cerrar sesión SÍ sigue siendo un Button en el nuevo diseño
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesionHosp)

        // Recibir nombre del admin si viene del Login
        val nombre = intent.getStringExtra("NOMBRE_USUARIO") ?: "Administrador"
        tvSubtitulo.text = "Bienvenido, $nombre"

        // --- LISTENERS (CLICS) ---

        // 1. Gestión de Doctores
        cardDoctores.setOnClickListener {
            // Abrimos la pantalla de Doctores
            val intent = Intent(this, DoctoresActivity::class.java)
            startActivity(intent)
        }

        // 2. Gestión de Pacientes
        cardPacientes.setOnClickListener {
            // Abrimos la pantalla de Pacientes
            val intent = Intent(this, PacientesActivity::class.java)
            startActivity(intent)
        }

        // 3. Farmacia (Aún sin pantalla)
        cardFarmacia.setOnClickListener {
            Toast.makeText(this, "Próximamente: Inventario de Farmacia", Toast.LENGTH_SHORT).show()
        }

        // 4. Cerrar Sesión
        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}