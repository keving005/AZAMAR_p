package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MisRecetasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaRecetas = mutableListOf<Receta>()
    private lateinit var adapter: RecetasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_recetas)

        val rv = findViewById<RecyclerView>(R.id.rvMisRecetas)
        rv.layoutManager = LinearLayoutManager(this)

        adapter = RecetasAdapter(listaRecetas) { receta ->
            // Al hacer clic, vamos al detalle
            val intent = Intent(this, DetalleRecetaPacienteActivity::class.java)
            intent.putExtra("RECETA_ID", receta.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        cargarRecetas()
    }

    private fun cargarRecetas() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("recetas")
            .whereEqualTo("pacienteId", uid)
            // .orderBy("timestamp", Query.Direction.DESCENDING) // Requiere índice en Firebase, si falla quítalo
            .get()
            .addOnSuccessListener { result ->
                listaRecetas.clear()
                for (doc in result) {
                    val r = doc.toObject(Receta::class.java)
                    r.id = doc.id
                    listaRecetas.add(r)
                }
                adapter.notifyDataSetChanged()

                if (listaRecetas.isEmpty()) {
                    Toast.makeText(this, "No tienes recetas aún", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

class RecetasAdapter(private val lista: List<Receta>, private val onClick: (Receta) -> Unit) :
    RecyclerView.Adapter<RecetasAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val tvDoctor: TextView = v.findViewById(R.id.tvItemDocNombre)
        val tvFecha: TextView = v.findViewById(R.id.tvItemFecha)
        val tvEstado: TextView = v.findViewById(R.id.tvItemEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_receta_paciente, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = lista[position]
        holder.tvDoctor.text = "Dr. ${item.nombreDoctor}"
        holder.tvFecha.text = item.fecha
        holder.tvEstado.text = item.estado
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = lista.size
}