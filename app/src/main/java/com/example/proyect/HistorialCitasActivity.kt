package com.example.proyect

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistorialCitasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaCitas = mutableListOf<Map<String, Any>>()
    private lateinit var adapter: HistorialAdapter
    private lateinit var tvVacio: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_citas)

        tvVacio = findViewById(R.id.tvVacioHistorial)
        val rv = findViewById<RecyclerView>(R.id.rvHistorial)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = HistorialAdapter(listaCitas)
        rv.adapter = adapter

        findViewById<ImageView>(R.id.btnBackHistorial).setOnClickListener { finish() }

        cargarHistorial()
    }

    private fun cargarHistorial() {
        val uid = auth.currentUser?.uid ?: return

        // Buscamos citas donde 'uidUsuario' sea el del usuario actual
        // Opcional: .orderBy("fechaAgenda", Query.Direction.DESCENDING) si creaste índices en Firebase
        db.collection("citas")
            .whereEqualTo("uidUsuario", uid)
            .get()
            .addOnSuccessListener { result ->
                listaCitas.clear()
                for (doc in result) {
                    listaCitas.add(doc.data)
                }

                // Ordenar manualmente por si Firebase falla en orden sin índice
                // Asumiendo formato YYYY-MM-DD en 'fechaAgenda'
                listaCitas.sortByDescending { it["fechaAgenda"].toString() }

                adapter.notifyDataSetChanged()

                if (listaCitas.isEmpty()) {
                    tvVacio.visibility = View.VISIBLE
                } else {
                    tvVacio.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
    }
}