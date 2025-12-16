package com.example.proyect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.random.Random

class PacientesAdapter(
    private val listaUsuarios: List<Usuario>,
    private val onAfiliadoChanged: (Usuario, Boolean) -> Unit,
    private val onExpedienteClick: (Usuario) -> Unit // NUEVO: Callback para el botón expediente
) : RecyclerView.Adapter<PacientesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombrePac)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoPac)
        val switchAfiliado: SwitchMaterial = view.findViewById(R.id.cbAfiliado)
        val tvNumAfiliacion: TextView = view.findViewById(R.id.tvNumAfiliacion)
        // NUEVO: El botón de expediente
        val btnExpediente: LinearLayout = view.findViewById(R.id.btnExpediente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.tvNombre.text = usuario.nombre
        holder.tvCorreo.text = usuario.correo

        // Lógica Afiliado (Igual que antes)
        holder.switchAfiliado.setOnCheckedChangeListener(null)
        holder.switchAfiliado.isChecked = usuario.esAfiliado

        if (usuario.esAfiliado) {
            holder.tvNumAfiliacion.visibility = View.VISIBLE
            holder.tvNumAfiliacion.text = usuario.numeroAfiliacion
        } else {
            holder.tvNumAfiliacion.visibility = View.GONE
        }

        holder.switchAfiliado.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                usuario.esAfiliado = isChecked
                if (isChecked) {
                    if (usuario.numeroAfiliacion.isEmpty()) {
                        val randomId = Random.nextInt(10000, 99999)
                        usuario.numeroAfiliacion = "AF-$randomId"
                    }
                    holder.tvNumAfiliacion.text = usuario.numeroAfiliacion
                    holder.tvNumAfiliacion.visibility = View.VISIBLE
                } else {
                    holder.tvNumAfiliacion.visibility = View.GONE
                }
                onAfiliadoChanged(usuario, isChecked)
            }
        }

        // NUEVO: Listener para el botón de Expediente
        holder.btnExpediente.setOnClickListener {
            onExpedienteClick(usuario)
        }
    }

    override fun getItemCount() = listaUsuarios.size
}