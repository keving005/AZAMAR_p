package com.example.proyect

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class HistorialAdapter(private val listaCitas: List<Map<String, Any>>) :
    RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvHistorialFecha)
        val tvHora: TextView = view.findViewById(R.id.tvHistorialHora)
        val tvDoctor: TextView = view.findViewById(R.id.tvHistorialEspecialista)
        val tvHospital: TextView = view.findViewById(R.id.tvHistorialHospital)
        val tvEstado: TextView = view.findViewById(R.id.tvHistorialEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita_historial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = listaCitas[position]

        holder.tvFecha.text = cita["fecha"]?.toString() ?: "--/--/--"
        holder.tvHora.text = cita["hora"]?.toString() ?: "--:--"
        holder.tvDoctor.text = cita["especialista"]?.toString() ?: "Especialista"
        holder.tvHospital.text = cita["hospitalNombre"]?.toString() ?: "Hospital"

        val estado = cita["estado"]?.toString()?.uppercase(Locale.getDefault()) ?: "PENDIENTE"
        holder.tvEstado.text = estado

        // Color según estado
        when (estado) {
            "FINALIZADA", "COMPLETADA" -> holder.tvEstado.setTextColor(Color.parseColor("#4CAF50")) // Verde
            "CANCELADA" -> holder.tvEstado.setTextColor(Color.parseColor("#F44336")) // Rojo
            else -> holder.tvEstado.setTextColor(Color.parseColor("#FF9800")) // Naranja (Pendiente)
        }
    }

    override fun getItemCount() = listaCitas.size
}