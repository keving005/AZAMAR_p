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

class DoctorCitasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var rvCitas: RecyclerView
    private lateinit var tvSinCitas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_citas)

        rvCitas = findViewById(R.id.rvCitasDoctor)
        tvSinCitas = findViewById(R.id.tvSinCitas)
        rvCitas.layoutManager = LinearLayoutManager(this)
    }

    // Usamos onResume para que la lista se refresque al volver de "Detalles"
    override fun onResume() {
        super.onResume()
        cargarCitasDelDoctor()
    }

    private fun cargarCitasDelDoctor() {
        db.collection("citas")
            .get()
            .addOnSuccessListener { result ->
                val listaCitas = ArrayList<ModeloCita>()

                for (document in result) {
                    val cita = ModeloCita(
                        idDocumento = document.id, // ¡IMPORTANTE! El ID para borrar/editar
                        paciente = document.getString("nombrePaciente") ?: "Anónimo",
                        hora = document.getString("hora") ?: "--:--",
                        fecha = document.getString("fecha") ?: "",
                        motivo = document.getString("tipoCita") ?: "Consulta",
                        peso = document.getString("peso") ?: "N/A",
                        altura = document.getString("altura") ?: "N/A",
                        sexo = document.getString("sexo") ?: "N/A",
                        tipoSangre = document.getString("tipoSangre") ?: "N/A",
                        alergias = document.getString("alergias") ?: "Ninguna"
                    )
                    listaCitas.add(cita)
                }

                if (listaCitas.isEmpty()) {
                    tvSinCitas.visibility = View.VISIBLE
                    rvCitas.visibility = View.GONE
                } else {
                    tvSinCitas.visibility = View.GONE
                    rvCitas.visibility = View.VISIBLE
                    rvCitas.adapter = CitasAdapter(listaCitas)
                }
            }
    }

    // Modelo de datos COMPLETO
    data class ModeloCita(
        val idDocumento: String,
        val paciente: String,
        val hora: String,
        val fecha: String,
        val motivo: String,
        val peso: String,
        val altura: String,
        val sexo: String,
        val tipoSangre: String,
        val alergias: String
    )

    inner class CitasAdapter(private val lista: List<ModeloCita>) : RecyclerView.Adapter<CitasAdapter.CitaViewHolder>() {

        inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvHora: TextView = itemView.findViewById(R.id.tvItemHora)
            val tvPaciente: TextView = itemView.findViewById(R.id.tvItemPaciente)
            val tvMotivo: TextView = itemView.findViewById(R.id.tvItemMotivo)
            val tvFecha: TextView = itemView.findViewById(R.id.tvItemFecha)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita_doctor, parent, false)
            return CitaViewHolder(view)
        }

        override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
            val item = lista[position]
            holder.tvPaciente.text = item.paciente
            holder.tvHora.text = item.hora
            holder.tvMotivo.text = item.motivo
            holder.tvFecha.text = item.fecha

            // AL DAR CLIC EN LA TARJETA -> VAMOS A DETALLES
            holder.itemView.setOnClickListener {
                val intent = Intent(this@DoctorCitasActivity, DetalleCitaActivity::class.java)
                intent.putExtra("ID_DOC", item.idDocumento)
                intent.putExtra("PACIENTE", item.paciente)
                intent.putExtra("FECHA", item.fecha)
                intent.putExtra("HORA", item.hora)
                intent.putExtra("MOTIVO", item.motivo)
                intent.putExtra("PESO", item.peso)
                intent.putExtra("ALTURA", item.altura)
                intent.putExtra("SEXO", item.sexo)
                intent.putExtra("SANGRE", item.tipoSangre)
                intent.putExtra("ALERGIAS", item.alergias)
                startActivity(intent)
            }
        }

        override fun getItemCount(): Int = lista.size
    }
}