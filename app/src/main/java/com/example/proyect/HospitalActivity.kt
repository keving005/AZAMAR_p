package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HospitalActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Variables para los textos de contadores
    private lateinit var tvCountDoctores: TextView
    private lateinit var tvCountFarmacia: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)

        // 1. Inicializar las vistas
        tvCountDoctores = findViewById(R.id.tvCountDoctores)
        tvCountFarmacia = findViewById(R.id.tvCountFarmacia)

        // 2. Configurar Botón: DOCTORES
        findViewById<CardView>(R.id.btnGestionarDoctores).setOnClickListener {
            // Asegúrate de que tu archivo se llame DoctoresActivity
            val intent = Intent(this, DoctoresActivity::class.java)
            startActivity(intent)
        }

        // 3. Configurar Botón: PACIENTES
        findViewById<CardView>(R.id.btnGestionarPacientes).setOnClickListener {
            // Asegúrate de que tu archivo se llame PacientesActivity
            val intent = Intent(this, PacientesActivity::class.java)
            startActivity(intent)
        }

        // 4. Configurar Botón: FARMACIA
        findViewById<CardView>(R.id.btnFarmacia).setOnClickListener {
            val intent = Intent(this, FarmaciaAdminActivity::class.java)
            startActivity(intent)
        }

        // 5. Configurar Botón: CERRAR SESIÓN
        findViewById<Button>(R.id.btnCerrarSesionHosp).setOnClickListener {
            auth.signOut()
            finish() // Cierra esta pantalla y regresa al Login
        }

        // Cargar los números iniciales
        cargarEstadisticas()
    }

    override fun onResume() {
        super.onResume()
        // Recargar las estadísticas cada vez que volvemos a esta pantalla
        // (Por si agregaste un doctor o medicamento nuevo y regresaste)
        cargarEstadisticas()
    }

    private fun cargarEstadisticas() {
        val hospitalId = auth.currentUser?.uid ?: return

        // --- Contar DOCTORES ---
        db.collection("doctores")
            .whereEqualTo("hospitalId", hospitalId)
            .get()
            .addOnSuccessListener { result ->
                // Actualizamos el número grande en la tarjeta
                tvCountDoctores.text = result.size().toString()
            }
            .addOnFailureListener {
                tvCountDoctores.text = "-"
            }

        // --- Contar MEDICAMENTOS (Farmacia) ---
        db.collection("medicamentos")
            .whereEqualTo("hospitalId", hospitalId)
            .get()
            .addOnSuccessListener { result ->
                // Actualizamos el número grande en la tarjeta
                tvCountFarmacia.text = result.size().toString()
            }
            .addOnFailureListener {
                tvCountFarmacia.text = "-"
            }
    }
}