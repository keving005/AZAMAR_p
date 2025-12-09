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

// Esta clase contiene tu lógica de login de Vistas (XML) y Firebase
class AuthActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnAceptar: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Verificación de conexión a Firebase
        try {
            val idProyecto = FirebaseApp.getInstance().options.projectId
            Toast.makeText(this, "CONECTADO A PROYECTO: $idProyecto", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error leyendo configuración: ${e.message}", Toast.LENGTH_LONG).show()
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

            // LÓGICA DE LOGIN CON FIREBASE
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid != null) {
                        db.collection("usuarios").document(uid).get()
                            .addOnSuccessListener { document ->
                                val nombre = document.getString("nombre") ?: "Usuario"
                                Toast.makeText(this, "Bienvenido $nombre", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, MenuActivity::class.java)
                                intent.putExtra("NOMBRE_USUARIO", nombre)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                val intent = Intent(this, MenuActivity::class.java)
                                startActivity(intent)
                            }
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
}