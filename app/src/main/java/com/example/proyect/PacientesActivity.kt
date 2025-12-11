package com.example.proyect

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

        // Configuramos el adaptador solo con la acción de afiliación
        adapter = PacientesAdapter(listaPacientes) { usuario, isChecked ->
            actualizarAfiliacionFirebase(usuario)
        }
        rvPacientes.adapter = adapter

        cargarPacientes()
    }

    private fun cargarPacientes() {
        // Traer solo usuarios con rol 1 (Pacientes)
        db.collection("usuarios")
            .whereEqualTo("rol", 1)
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
                if(usuario.esAfiliado) {
                    Toast.makeText(this, "Afiliado generado: ${usuario.numeroAfiliacion}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}