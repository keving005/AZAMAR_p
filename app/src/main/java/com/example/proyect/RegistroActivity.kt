package com.example.proyect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnRegistrar: Button

    // Instancias de Firebase
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        etNombre = findViewById(R.id.etNombreRegistro)
        etEmail = findViewById(R.id.etEmailRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        btnRegistrar = findViewById(R.id.btnRegistrarUsuario)

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etContrasena.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Crear usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid

                    // 2. Guardar el nombre en Firestore
                    val usuarioData = hashMapOf(
                        "uid" to uid,
                        "nombre" to nombre,
                        "email" to email
                    )

                    if (uid != null) {
                        db.collection("usuarios").document(uid).set(usuarioData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}