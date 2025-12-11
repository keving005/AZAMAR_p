package com.example.proyect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DoctoresAdapter(
    private val lista: List<Usuario>,
    private val onDeleteClick: (Usuario) -> Unit
) : RecyclerView.Adapter<DoctoresAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreDoc)
        val tvEspecialidad: TextView = view.findViewById(R.id.tvEspecialidad)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoDoc)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = lista[position]
        holder.tvNombre.text = doc.nombre
        holder.tvEspecialidad.text = doc.especialidad
        holder.tvCorreo.text = doc.correo

        holder.btnDelete.setOnClickListener { onDeleteClick(doc) }
    }

    override fun getItemCount() = lista.size
}