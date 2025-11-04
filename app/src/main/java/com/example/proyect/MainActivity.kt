package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// 1. IMPORTAR FIREBASE AUTH
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// --- AÑADIDO: IMPORTAR TEXTVIEW ---
// Necesitas esto para encontrar el 'tvIrARegistro'
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    // Es mejor usar 'lateinit var' que tipos nulos '?'
    private lateinit var etEmail: EditText // Corresponde a tu etUsuario
    private lateinit var etContrasena: EditText
    private lateinit var btnAceptar: Button

    // 2. DECLARAR FIREBASE AUTH
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 3. INICIALIZAR VISTAS
        // Asegúrate de que los IDs coincidan con tu XML
        etEmail = findViewById(R.id.etUsuario) // Usamos etUsuario para el email
        etContrasena = findViewById(R.id.etContrasena)
        btnAceptar = findViewById(R.id.btnAceptar)

        // 4. INICIALIZAR FIREBASE AUTH
        auth = Firebase.auth

        // 5. LÓGICA DE LOGIN (Esto ya lo tenías)
        btnAceptar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            // Validación simple
            if (email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, ingresa email y contraseña.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener // Detiene la ejecución si está vacío
            }

            // --- INICIAR SESIÓN CON FIREBASE ---
            auth.signInWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // ¡Login exitoso!
                        Toast.makeText(
                            this@MainActivity,
                            "Inicio de sesión exitoso.",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navega a MenuActivity
                        val intent = Intent(this@MainActivity, MenuActivity::class.java)
                        startActivity(intent)
                        finish() // Cierra MainActivity para que no pueda volver atrás

                    } else {
                        // Si el login falla, muestra el error de Firebase
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // --- 6. AÑADIDO: CÓDIGO PARA IR AL REGISTRO ---
        // (Asegúrate de tener el TextView con id 'tvIrARegistro' en tu activity_main.xml)

        val tvIrARegistro = findViewById<TextView>(R.id.tvIrARegistro)
        tvIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
        // --- FIN DE LA SECCIÓN AÑADIDA ---
    }
}