package com.example.proyect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TratamientosAdapter(
    private val lista: List<Tratamiento>
) : RecyclerView.Adapter<TratamientosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreMed)
        val tvDosis: TextView = view.findViewById(R.id.tvDosisMed)
        val tvFrecuencia: TextView = view.findViewById(R.id.tvFrecuenciaMed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Aquí usamos TU archivo item_tratamiento.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tratamiento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombreMedicamento
        holder.tvDosis.text = item.dosis
        holder.tvFrecuencia.text = "Cada ${item.frecuenciaHoras} horas por ${item.duracionDias} días"
    }

    override fun getItemCount() = lista.size
}