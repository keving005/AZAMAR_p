package com.example.proyect

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class FarmaciaPacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Para saber quién es el paciente
    private lateinit var rvFarmacia: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var fabConfirmar: ExtendedFloatingActionButton
    private lateinit var tvTitulo: TextView

    // Listas de datos
    private val listaCompleta = mutableListOf<Medicamento>()
    private val listaFiltrada = mutableListOf<Medicamento>()

    // Carrito de compras (sirve para Doctor y Paciente)
    private val listaSeleccionados = mutableListOf<Medicamento>()

    private lateinit var adapter: FarmaciaPacienteAdapter
    private var esModoSelector = false // true = Doctor, false = Paciente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmacia_paciente)

        // 1. RECUPERAR EL MODO
        esModoSelector = intent.getBooleanExtra("MODO_SELECTOR", false)

        // 2. VINCULAR VISTAS
        rvFarmacia = findViewById(R.id.rvFarmaciaPaciente)
        searchView = findViewById(R.id.svFarmaciaPaciente)
        fabConfirmar = findViewById(R.id.fabConfirmarSeleccion)
        tvTitulo = findViewById(R.id.tvTituloFarmacia)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        // 3. CONFIGURAR SEGÚN EL MODO
        // AHORA EL BOTÓN FLOTANTE SIEMPRE ES VISIBLE, PERO CAMBIA SU FUNCIÓN
        fabConfirmar.visibility = View.VISIBLE

        if (esModoSelector) {
            tvTitulo.text = "Seleccionar Medicamentos"
            fabConfirmar.text = "Confirmar (0)"
            fabConfirmar.setIconResource(android.R.drawable.ic_input_add)
            fabConfirmar.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_green_dark)
        } else {
            tvTitulo.text = "Farmacia Digital"
            fabConfirmar.text = "Ver Pedido (0)"
            fabConfirmar.setIconResource(android.R.drawable.ic_menu_camera) // Icono de QR o similar
            fabConfirmar.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark)
        }

        // Listener del botón flotante (Diferente acción según quién sea)
        fabConfirmar.setOnClickListener {
            if (esModoSelector) {
                confirmarSeleccionDoctor()
            } else {
                confirmarPedidoPaciente()
            }
        }

        // 4. CONFIGURAR RECYCLER
        rvFarmacia.layoutManager = LinearLayoutManager(this)

        // Pasamos la lista de seleccionados al adapter para que pinte los botones correctamente
        adapter = FarmaciaPacienteAdapter(listaFiltrada, listaSeleccionados) { med ->
            toggleSeleccion(med)
        }
        rvFarmacia.adapter = adapter

        // 5. CARGAR DATOS
        setupBuscador()
        cargarMedicamentos()
    }

    // --- LÓGICA DE SELECCIÓN (COMÚN) ---
    private fun toggleSeleccion(med: Medicamento) {
        val existente = listaSeleccionados.find { it.id == med.id }

        if (existente != null) {
            listaSeleccionados.remove(existente)
            med.cantidadSeleccionada = 0
            // Mensaje pequeño solo si es paciente
            if (!esModoSelector) Toast.makeText(this, "Eliminado del carrito", Toast.LENGTH_SHORT).show()
        } else {
            med.cantidadSeleccionada = 1
            listaSeleccionados.add(med)
            if (!esModoSelector) Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
        }

        actualizarBotonFlotante()
        adapter.notifyDataSetChanged()
    }

    private fun actualizarBotonFlotante() {
        val count = listaSeleccionados.size
        if (esModoSelector) {
            fabConfirmar.text = "Confirmar ($count)"
        } else {
            fabConfirmar.text = if (count > 0) "Generar QR ($count)" else "Ver Pedido (0)"
        }
    }

    // --- ACCIÓN 1: DOCTOR (Devuelve datos a la receta) ---
    private fun confirmarSeleccionDoctor() {
        if (listaSeleccionados.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un medicamento", Toast.LENGTH_SHORT).show()
            return
        }
        val intentResult = Intent()
        intentResult.putParcelableArrayListExtra("MEDICAMENTOS_SELECCIONADOS", ArrayList(listaSeleccionados))
        setResult(Activity.RESULT_OK, intentResult)
        finish()
    }

    // --- ACCIÓN 2: PACIENTE (Genera QR) ---
    private fun confirmarPedidoPaciente() {
        if (listaSeleccionados.isEmpty()) {
            Toast.makeText(this, "Tu carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Construir String para el QR (Resumen del pedido)
        val sb = StringBuilder()
        sb.append("PEDIDO FARMACIA\n")
        sb.append("Paciente ID: ${auth.currentUser?.uid}\n")
        sb.append("----------------\n")
        for (med in listaSeleccionados) {
            sb.append("- ${med.nombre} (${med.gramaje})\n")
        }
        sb.append("----------------\n")
        sb.append("Total Ítems: ${listaSeleccionados.size}")

        // 2. Mostrar QR
        mostrarDialogoQR(sb.toString())
    }

    private fun mostrarDialogoQR(contenido: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 600, 600)

            // Reutilizamos tu diseño de diálogo o creamos uno simple
            val view = layoutInflater.inflate(R.layout.dialog_qr_exito, null)
            val ivQR = view.findViewById<ImageView>(R.id.ivQrGenerado)
            val btnCerrar = view.findViewById<Button>(R.id.btnCerrarReceta)

            // Si el texto del layout dice "Receta", lo cambiamos (opcional si quieres hacerlo dinámico)
            // val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialogo)
            // tvTitulo.text = "Pedido de Farmacia"

            ivQR.setImageBitmap(bitmap)
            btnCerrar.text = "Cerrar Pedido"

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()

            btnCerrar.setOnClickListener {
                dialog.dismiss()
                // Opcional: Limpiar carrito al cerrar
                listaSeleccionados.clear()
                actualizarBotonFlotante()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Pedido listo. Muestra el QR en mostrador.", Toast.LENGTH_LONG).show()
            }

            dialog.show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FIREBASE & FILTROS ---
    private fun cargarMedicamentos() {
        db.collection("medicamentos").get()
            .addOnSuccessListener { result ->
                listaCompleta.clear()
                for (doc in result) {
                    val m = doc.toObject(Medicamento::class.java)
                    m.id = doc.id
                    m.cantidadSeleccionada = 0
                    listaCompleta.add(m)
                }
                filtrarLista("")
            }
    }

    private fun setupBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText ?: "")
                return true
            }
        })
    }

    private fun filtrarLista(texto: String) {
        listaFiltrada.clear()
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaCompleta)
        } else {
            val busqueda = texto.lowercase()
            listaFiltrada.addAll(listaCompleta.filter {
                it.nombre.lowercase().contains(busqueda) || it.patente.lowercase().contains(busqueda)
            })
        }
        adapter.notifyDataSetChanged()
    }
}

// --- ADAPTER MODIFICADO ---
class FarmaciaPacienteAdapter(
    private val lista: List<Medicamento>,
    private val seleccionados: List<Medicamento>, // Recibimos la lista de seleccionados para verificar estado
    private val onActionClick: (Medicamento) -> Unit
) : RecyclerView.Adapter<FarmaciaPacienteAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgMedItem)
        val nombre: TextView = v.findViewById(R.id.tvNombreMedItem)
        val dosis: TextView = v.findViewById(R.id.tvDosisItem)
        val precio: TextView = v.findViewById(R.id.tvPrecioItem)
        val btnAdd: MaterialButton = v.findViewById(R.id.btnAddCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento_paciente, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = lista[position]

        holder.nombre.text = item.nombre
        holder.dosis.text = if (item.gramaje.isNotEmpty()) "${item.gramaje} - ${item.patente}" else item.patente
        holder.precio.text = "$${item.precio}"

        if (item.fotoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(item.fotoUrl).placeholder(android.R.drawable.ic_menu_gallery).into(holder.img)
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // VERIFICAR SI ESTÁ EN EL CARRITO
        val estaSeleccionado = seleccionados.any { it.id == item.id }

        if (estaSeleccionado) {
            // ESTADO: EN CARRITO (Verde)
            holder.btnAdd.text = "En Carrito"
            holder.btnAdd.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
            holder.btnAdd.setIconResource(android.R.drawable.checkbox_on_background)
        } else {
            // ESTADO: NO AGREGADO (Azul)
            holder.btnAdd.text = "Agregar"
            holder.btnAdd.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_dark))
            holder.btnAdd.setIconResource(android.R.drawable.ic_input_add)
        }

        holder.btnAdd.setOnClickListener { onActionClick(item) }
    }

    override fun getItemCount() = lista.size
}