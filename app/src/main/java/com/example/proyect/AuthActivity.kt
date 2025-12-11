package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp

class AuthActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnAceptar: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Asegúrate que tu XML se llama activity_auth o activity_main según corresponda

        // Verificación de conexión a Firebase (Lo que ya tenías)
        try {
            val idProyecto = FirebaseApp.getInstance().options.projectId
            // Comenté el Toast para que no moleste cada vez, pero funciona igual
            // Toast.makeText(this, "Conectado a: $idProyecto", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error config: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Vinculación de Vistas
        etEmail = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        btnAceptar = findViewById(R.id.btnAceptar)
        val tvIrARegistro = findViewById<TextView>(R.id.tvIrARegistro)

        btnAceptar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etContrasena.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- LÓGICA DE LOGIN ---
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) {
                        verificarRolYRedirigir(uid)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al entrar: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        tvIrARegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun verificarRolYRedirigir(uid: String) {
        val userRef = db.collection("usuarios").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: "Usuario"

                    // Verificamos si existe el campo "rol"
                    val rolExistente = document.getLong("rol")?.toInt()

                    if (rolExistente == null) {
                        // --- AUTOMATIZACIÓN PARA USUARIOS VIEJOS ---
                        // Si no tiene rol, le ponemos 1 (Paciente) y actualizamos la BD
                        userRef.update("rol", 1)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Perfil actualizado. Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                                irAPantalla(1, nombre) // Lo mandamos como paciente
                            }
                    } else {
                        // Si ya tiene rol, respetamos el que tenga
                        Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()
                        irAPantalla(rolExistente, nombre)
                    }
                } else {
                    Toast.makeText(this, "Usuario sin datos en base de datos", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
            }
    }

    private fun irAPantalla(rol: Int, nombre: String) {
        val intent = when (rol) {
            1 -> Intent(this, MenuActivity::class.java)       // Paciente
            2 -> Intent(this, DoctorActivity::class.java)     // Doctor
            3 -> Intent(this, HospitalActivity::class.java)   // Hospital
            else -> Intent(this, MenuActivity::class.java)    // Por defecto Paciente
        }
        intent.putExtra("NOMBRE_USUARIO", nombre)
        startActivity(intent)
        finish()
    }
}