package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    var etNombre: EditText? = null
    var etMatricula: EditText? = null
    var etContrasena: EditText? = null
    var btnAceptar: Button? = null

    private val USERNAME = "kevin"
    private val NUMERO_PACIENTE = "12345"
    private val PASSWORD = "123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etNombre = findViewById(R.id.etUsuario)
        etMatricula = findViewById(R.id.etMatricula)
        etContrasena = findViewById(R.id.etContrasena)
        btnAceptar = findViewById(R.id.btnAceptar)

        btnAceptar!!.setOnClickListener {
            val nombre = etNombre!!.getText().toString().trim { it <= ' ' }
            val matricula = etMatricula!!.getText().toString().trim { it <= ' ' }
            val contrasena = etContrasena!!.getText().toString().trim { it <= ' ' }

            if (nombre == "kevin" && matricula == "12345" && contrasena == "123") {
                val intent: Intent = Intent(this@MainActivity, MenuActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@MainActivity, "Datos incorrectos. Intenta de nuevo.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}