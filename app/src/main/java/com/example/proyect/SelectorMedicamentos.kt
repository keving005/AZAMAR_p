package com.example.proyect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

// CLASE DE DATOS SIMPLE PARA MEDICAMENTO
data class MedicamentoSeleccion(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val stockMax: Int,
    var cantidadA_Recetar: Int = 0
)

// ADAPTER PARA LA LISTA CON BOTONES +/-
class SelectorAdapter(
    private val lista: List<MedicamentoSeleccion>
) : RecyclerView.Adapter<SelectorAdapter.VisorHolder>() {

    class VisorHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvNombre: TextView = v.findViewById(R.id.tvNombreMedSel)
        val tvStock: TextView = v.findViewById(R.id.tvStockMedSel)
        val tvCant: TextView = v.findViewById(R.id.tvCantidadSel)
        val btnMas: Button = v.findViewById(R.id.btnMas)
        val btnMenos: Button = v.findViewById(R.id.btnMenos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisorHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento_selector, parent, false)
        return VisorHolder(view)
    }

    override fun onBindViewHolder(holder: VisorHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombre
        holder.tvStock.text = "Disponible: ${item.stockMax}"
        holder.tvCant.text = item.cantidadA_Recetar.toString()

        holder.btnMas.setOnClickListener {
            if (item.cantidadA_Recetar < item.stockMax) {
                item.cantidadA_Recetar++
                holder.tvCant.text = item.cantidadA_Recetar.toString()
            }
        }

        holder.btnMenos.setOnClickListener {
            if (item.cantidadA_Recetar > 0) {
                item.cantidadA_Recetar--
                holder.tvCant.text = item.cantidadA_Recetar.toString()
            }
        }
    }

    override fun getItemCount() = lista.size
}

// EL DIÁLOGO FLOTANTE (BOTTOM SHEET)
class SelectorMedicamentosDialog(
    private val alConfirmar: (List<MedicamentoSeleccion>) -> Unit
) : BottomSheetDialogFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val listaMedicamentos = mutableListOf<MedicamentoSeleccion>()
    private lateinit var adapter: SelectorAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_buscador_farmacia, container, false)

        val rv = view.findViewById<RecyclerView>(R.id.rvMedicamentosDialog)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarSeleccion)

        rv.layoutManager = LinearLayoutManager(context)
        adapter = SelectorAdapter(listaMedicamentos)
        rv.adapter = adapter

        cargarFarmacia()

        btnConfirmar.setOnClickListener {
            // Filtramos solo los que tienen cantidad > 0
            val seleccionados = listaMedicamentos.filter { it.cantidadA_Recetar > 0 }
            if (seleccionados.isEmpty()) {
                Toast.makeText(context, "Selecciona al menos uno", Toast.LENGTH_SHORT).show()
            } else {
                alConfirmar(seleccionados)
                dismiss()
            }
        }

        return view
    }

    private fun cargarFarmacia() {
        // Asegúrate que tu colección en Firebase se llame "farmacia" o "medicamentos"
        db.collection("farmacia").get().addOnSuccessListener { result ->
            listaMedicamentos.clear()
            for (doc in result) {
                val stock = doc.getLong("stock")?.toInt() ?: 0
                // Solo mostramos si hay stock
                if (stock > 0) {
                    listaMedicamentos.add(
                        MedicamentoSeleccion(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "Medicamento",
                            descripcion = doc.getString("descripcion") ?: "",
                            stockMax = stock
                        )
                    )
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}