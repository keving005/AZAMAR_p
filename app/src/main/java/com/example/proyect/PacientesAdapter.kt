package com.example.proyect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton // Importante
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial // Importante
import kotlin.random.Random

class PacientesAdapter(
    private val listaUsuarios: List<Usuario>,
    private val onAfiliadoChanged: (Usuario, Boolean) -> Unit
) : RecyclerView.Adapter<PacientesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombrePac)
        val tvCorreo: TextView = view.findViewById(R.id.tvCorreoPac)
        // CAMBIO: Ahora es SwitchMaterial, no CheckBox
        val switchAfiliado: SwitchMaterial = view.findViewById(R.id.cbAfiliado)
        val tvNumAfiliacion: TextView = view.findViewById(R.id.tvNumAfiliacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.tvNombre.text = usuario.nombre
        holder.tvCorreo.text = usuario.correo

        // Quitamos el listener temporalmente
        holder.switchAfiliado.setOnCheckedChangeListener(null)

        // Asignamos estado
        holder.switchAfiliado.isChecked = usuario.esAfiliado

        // Logica visual del ID
        if (usuario.esAfiliado) {
            holder.tvNumAfiliacion.visibility = View.VISIBLE
            holder.tvNumAfiliacion.text = usuario.numeroAfiliacion
        } else {
            holder.tvNumAfiliacion.visibility = View.GONE
        }

        // Listener del Switch
        holder.switchAfiliado.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) { // Solo si el usuario lo tocó físicamente
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
    }

    override fun getItemCount() = listaUsuarios.size
}