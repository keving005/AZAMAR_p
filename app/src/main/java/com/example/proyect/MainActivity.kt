package com.example.proyect // Asegúrate que este sea tu paquete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// 1. IMPORTAR LIBRERÍAS DE VOLLEY
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText // Corresponde a tu etUsuario
    private lateinit var etContrasena: EditText
    private lateinit var btnAceptar: Button

    // --- 2. ¡TU IP YA ESTÁ CONFIGURADA! ---
    private val IP_SERVIDOR = "http://192.168.1.78:8000"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        etEmail = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        btnAceptar = findViewById(R.id.btnAceptar)

        // Lógica del botón de Login
        btnAceptar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Ingresa email y contraseña.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llamar a la función de login con Volley
            loginConVolley(email, contrasena)
        }

        // Lógica del botón de ir a Registro (Esta se queda igual)
        val tvIrARegistro = findViewById<TextView>(R.id.tvIrARegistro)
        tvIrARegistro.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginConVolley(email: String, contrasena: String) {

        // a. URL del endpoint de login de FastAPI
        val url = "$IP_SERVIDOR/login"

        // b. JSON que espera FastAPI (coincide con UserLogin en schemas.py)
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", contrasena)

        // c. Petición POST de Volley
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,

            // d. Listener de ÉXITO
            { response ->
                // Obtenemos el nombre que nos devuelve el servidor
                val nombreUsuario = response.getString("nombre")
                Toast.makeText(
                    this,
                    "¡Bienvenido de nuevo, $nombreUsuario!",
                    Toast.LENGTH_LONG
                ).show()

                // Navega a MenuActivity
                // ¡IMPORTANTE! Pasamos el nombre a la siguiente pantalla
                val intent = Intent(this@MainActivity, MenuActivity::class.java)
                intent.putExtra("NOMBRE_USUARIO", nombreUsuario) // Pasamos el nombre
                startActivity(intent)
                finish()
            },

            // e. Listener de ERROR
            { error ->
                val errorMsg = error.networkResponse?.let {
                    try {
                        val data = String(it.data)
                        JSONObject(data).getString("detail") // El error de FastAPI
                    } catch (e: Exception) {
                        error.message
                    }
                } ?: error.message

                Toast.makeText(
                    this,
                    "Error: $errorMsg",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        // f. Añadir a la cola
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}