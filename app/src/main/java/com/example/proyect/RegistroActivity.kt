package com.example.proyect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Importaciones de Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegistroActivity : AppCompatActivity() {

    // Declarar vistas
    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnRegistrar: Button

    // Declarar Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro) // Usa el nuevo layout

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRegistro)
        etEmail = findViewById(R.id.etEmailRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        btnRegistrar = findViewById(R.id.btnRegistrarUsuario)

        // Inicializar Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        // Listener del botón
        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (contrasena.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llamar a la función de registro
            crearUsuarioConFirebase(nombre, email, contrasena)
        }
    }

    private fun crearUsuarioConFirebase(nombre: String, email: String, contrasena: String) {

        // 1. CREAR USUARIO EN AUTHENTICATION
        auth.createUserWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Usuario creado exitosamente.", Toast.LENGTH_SHORT).show()

                    val userId = auth.currentUser?.uid // Obtener el ID del nuevo usuario

                    // 2. GUARDAR DATOS EXTRA EN FIRESTORE
                    if (userId != null) {
                        guardarDatosDeUsuarioEnFirestore(userId, nombre, email)
                    }

                } else {
                    // Falló el registro en Authentication
                    Toast.makeText(
                        baseContext,
                        "Falló el registro: ${task.exception?.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }

    private fun guardarDatosDeUsuarioEnFirestore(uid: String, nombre: String, email: String) {

        // Crear un "mapa" de datos
        val userData = hashMapOf(
            "nombre" to nombre,
            "email" to email
            // Aquí puedes añadir tu "numero_paciente" (etMatricula) si lo pides en este formulario
        )

        // Guardar en la colección "usuarios" con el ID (uid) del usuario
        db.collection("usuarios").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(baseContext, "Datos guardados. ¡Bienvenido!", Toast.LENGTH_SHORT).show()

                // Opcional: Cierra esta actividad y vuelve al Login
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(baseContext, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}