package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PacientesActivity : AppCompatActivity() {

    private lateinit var rvPacientes: RecyclerView
    private val listaPacientes = mutableListOf<Usuario>()
    private lateinit var adapter: PacientesAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacientes_lista)

        title = "Gestión de Pacientes"

        rvPacientes = findViewById(R.id.rvPacientes)
        rvPacientes.layoutManager = LinearLayoutManager(this)

        // Inicializamos el Adapter
        adapter = PacientesAdapter(
            listaUsuarios = listaPacientes,

            // ACCIÓN 1: CAMBIAR SWITCH DE AFILIADO
            onAfiliadoChanged = { usuario, isChecked ->
                actualizarAfiliacionFirebase(usuario)
            },

            // ACCIÓN 2: CLIC EN BOTÓN EXPEDIENTE
            onExpedienteClick = { usuario ->
                val intent = Intent(this, CrearExpedienteActivity::class.java)
                intent.putExtra("PACIENTE_ID", usuario.uid)
                // CORRECCIÓN: Solo mandamos el nombre, borramos apellidos
                intent.putExtra("PACIENTE_NOMBRE", usuario.nombre)
                startActivity(intent)
            }
        )

        rvPacientes.adapter = adapter

        cargarPacientes()
    }

    private fun cargarPacientes() {
        db.collection("usuarios")
            .whereEqualTo("rol", 1) // Solo pacientes
            .get()
            .addOnSuccessListener { documents ->
                listaPacientes.clear()
                for (document in documents) {
                    val user = document.toObject(Usuario::class.java)
                    user.uid = document.id
                    listaPacientes.add(user)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarAfiliacionFirebase(usuario: Usuario) {
        if (usuario.uid.isEmpty()) return

        db.collection("usuarios").document(usuario.uid)
            .update(
                mapOf(
                    "esAfiliado" to usuario.esAfiliado,
                    "numeroAfiliacion" to usuario.numeroAfiliacion
                )
            )
            .addOnSuccessListener {
                if (usuario.esAfiliado) {
                    Toast.makeText(this, "Afiliado generado: ${usuario.numeroAfiliacion}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Afiliación cancelada", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar cambio", Toast.LENGTH_SHORT).show()
            }
    }
}