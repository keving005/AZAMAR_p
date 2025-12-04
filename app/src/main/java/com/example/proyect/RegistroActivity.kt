package com.example.proyect // Asegúrate que este sea tu paquete

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// 1. IMPORTAR LIBRERÍAS DE VOLLEY
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnRegistrar: Button


    private val IP_SERVIDOR = "http://192.168.1.78:8000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombreRegistro) // Asegúrate que los IDs coincidan
        etEmail = findViewById(R.id.etEmailRegistro)
        etContrasena = findViewById(R.id.etContrasenaRegistro)
        btnRegistrar = findViewById(R.id.btnRegistrarUsuario)

        // Listener del botón
        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val contrasena = etContrasena.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || contrasena.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrarUsuarioConVolley(nombre, email, contrasena)
        }
    }

    private fun registrarUsuarioConVolley(nombre: String, email: String, contrasena: String) {

        val url = "$IP_SERVIDOR/registrar"

        // Creamos el objeto JSON que FastAPI espera (coincide con UserCreate)
        val jsonBody = JSONObject()
        jsonBody.put("nombre", nombre)
        jsonBody.put("email", email)
        jsonBody.put("password", contrasena)

        // Creamos la petición POST de Volley
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,

            // Listener de ÉXITO (El servidor respondió bien - código 200)
            { response ->
                val nombreUsuario = response.getString("nombre")
                Toast.makeText(
                    this,
                    "¡Bienvenido, $nombreUsuario! Registro exitoso.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Cierra esta pantalla y vuelve al Login
            },

            // Listener de ERROR (El servidor respondió mal - código 400, 500, etc.)
            { error ->
                // Intentamos leer el mensaje de error de FastAPI
                val errorMsg = error.networkResponse?.let {
                    try {
                        val data = String(it.data)
                        JSONObject(data).getString("detail") // Capturamos el "detail"
                    } catch (e: Exception) {
                        error.message // Si falla, usamos el error genérico
                    }
                } ?: error.message

                Toast.makeText(
                    this,
                    "Error en el registro: $errorMsg",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        // Añadimos la petición a la cola de Volley para que se ejecute
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}