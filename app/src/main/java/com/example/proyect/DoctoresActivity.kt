package com.example.proyect

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DoctoresActivity : AppCompatActivity() {

    private lateinit var rvDoctores: RecyclerView
    private val listaDoctores = mutableListOf<Usuario>()
    private lateinit var adapter: DoctoresAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Auth del Hospital (Admin actual)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctores_lista) // Crear este XML en el paso 5

        rvDoctores = findViewById(R.id.rvDoctores)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddDoctor)

        rvDoctores.layoutManager = LinearLayoutManager(this)
        adapter = DoctoresAdapter(listaDoctores) { doctor ->
            confirmarBorrado(doctor)
        }
        rvDoctores.adapter = adapter

        fabAdd.setOnClickListener {
            mostrarDialogoCrearDoctor()
        }

        cargarDoctores()
    }

    private fun cargarDoctores() {
        val miHospitalId = auth.currentUser?.uid ?: return

        // CONSULTA CLAVE: Trae usuarios Rol 2 (Doc) que tengan MI hospitalId
        db.collection("usuarios")
            .whereEqualTo("rol", 2)
            .whereEqualTo("hospitalId", miHospitalId)
            .get()
            .addOnSuccessListener { documents ->
                listaDoctores.clear()
                for (doc in documents) {
                    val usuario = doc.toObject(Usuario::class.java)
                    usuario.uid = doc.id
                    listaDoctores.add(usuario)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun mostrarDialogoCrearDoctor() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null) // Crear XML simple

        val etNombre = view.findViewById<EditText>(R.id.etDocNombre)
        val etEmail = view.findViewById<EditText>(R.id.etDocEmail)
        val etPass = view.findViewById<EditText>(R.id.etDocPass)
        val etEsp = view.findViewById<EditText>(R.id.etDocEspecialidad)

        builder.setView(view)
        builder.setTitle("Nuevo Doctor")
        builder.setPositiveButton("Crear") { _, _ ->
            val nombre = etNombre.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val especialidad = etEsp.text.toString()

            if (nombre.isNotEmpty() && email.isNotEmpty() && pass.length >= 6) {
                crearDoctorEnFirebase(nombre, email, pass, especialidad)
            } else {
                Toast.makeText(this, "Datos inválidos (Pass min 6 chars)", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    // --- AQUÍ ESTÁ LA MAGIA PARA NO CERRAR SESIÓN ---
    private fun crearDoctorEnFirebase(nombre: String, email: String, pass: String, esp: String) {
        val miHospitalId = auth.currentUser?.uid ?: return

        // 1. Crear una instancia secundaria de Firebase para registrar al otro usuario
        val firebaseOptions = FirebaseOptions.Builder()
            .setApiKey(FirebaseApp.getInstance().options.apiKey)
            .setApplicationId(FirebaseApp.getInstance().options.applicationId)
            .setProjectId(FirebaseApp.getInstance().options.projectId)
            .build()

        val nombreAppSecundaria = "AppRegistradora"
        var appSecundaria: FirebaseApp? = null

        try {
            appSecundaria = FirebaseApp.initializeApp(this, firebaseOptions, nombreAppSecundaria)
            val authSecundaria = FirebaseAuth.getInstance(appSecundaria)

            authSecundaria.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val nuevoUid = result.user?.uid ?: ""

                    // 2. Guardar datos en Firestore (usando la instancia normal)
                    val nuevoDoctor = Usuario(
                        uid = nuevoUid,
                        nombre = nombre,
                        correo = email,
                        rol = 2, // 2 es Doctor
                        especialidad = esp,
                        hospitalId = miHospitalId // VINCULAMOS AL HOSPITAL AQUÍ
                    )

                    db.collection("usuarios").document(nuevoUid).set(nuevoDoctor)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Doctor creado exitosamente", Toast.LENGTH_SHORT).show()
                            authSecundaria.signOut() // Cerramos la sesión secundaria
                            cargarDoctores() // Refrescamos lista
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }

        } catch (e: Exception) {
            // Si la app secundaria ya existía, intentamos recuperarla (raro que pase aquí)
            Toast.makeText(this, "Error de instancia: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarBorrado(doctor: Usuario) {
        // Opcional: Implementar borrado de Firestore
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Eliminar al Dr. ${doctor.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("usuarios").document(doctor.uid).delete()
                    .addOnSuccessListener { cargarDoctores() }
            }
            .show()
    }
}