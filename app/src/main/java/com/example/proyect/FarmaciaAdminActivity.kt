package com.example.proyect

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class FarmaciaAdminActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private lateinit var rvMedicamentos: RecyclerView
    private lateinit var searchView: SearchView

    // Listas para búsqueda
    private val listaCompleta = mutableListOf<Medicamento>()
    private val listaFiltrada = mutableListOf<Medicamento>()

    private lateinit var adapter: MedicamentosAdminAdapter

    // Variables de imagen
    private var imageUri: Uri? = null
    private lateinit var imgPreviewDialog: ImageView

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            try {
                imgPreviewDialog.imageTintList = null
                Glide.with(this).load(uri).centerCrop().into(imgPreviewDialog)
            } catch (e: Exception) { }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_farmacia)

        rvMedicamentos = findViewById(R.id.rvFarmacia)
        searchView = findViewById(R.id.searchView)

        rvMedicamentos.layoutManager = LinearLayoutManager(this)

        // Configurar Adapter
        adapter = MedicamentosAdminAdapter(listaFiltrada,
            onItemClick = { med -> mostrarDialogoMedicamento(med) }, // Click normal = Editar
            onDeleteClick = { med -> borrarMedicamento(med) }        // Click basura = Borrar
        )
        rvMedicamentos.adapter = adapter

        // Botón nuevo
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddMed).setOnClickListener {
            mostrarDialogoMedicamento(null)
        }

        setupBuscador()
        cargarInventario()
    }

    private fun setupBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarLista(newText)
                return true
            }
        })
    }

    private fun filtrarLista(texto: String?) {
        listaFiltrada.clear()
        if (texto.isNullOrEmpty()) {
            listaFiltrada.addAll(listaCompleta)
        } else {
            val busqueda = texto.lowercase()
            listaFiltrada.addAll(listaCompleta.filter {
                it.nombre.lowercase().contains(busqueda) ||
                        it.patente.lowercase().contains(busqueda)
            })
        }
        adapter.notifyDataSetChanged()
    }

    private fun cargarInventario() {
        val miHospitalId = auth.currentUser?.uid ?: return
        db.collection("medicamentos")
            .whereEqualTo("hospitalId", miHospitalId)
            .get()
            .addOnSuccessListener { result ->
                listaCompleta.clear()
                for (doc in result) {
                    val m = doc.toObject(Medicamento::class.java)
                    m.id = doc.id
                    listaCompleta.add(m)
                }
                filtrarLista(searchView.query.toString())
            }
    }

    private fun mostrarDialogoMedicamento(medicamentoEditar: Medicamento?) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_crear_medicamento, null)

        // Referencias
        val etNombre = view.findViewById<TextInputEditText>(R.id.etNombre)
        val etGramaje = view.findViewById<TextInputEditText>(R.id.etGramaje)
        val etPatente = view.findViewById<TextInputEditText>(R.id.etPatente)
        val etIngredientes = view.findViewById<TextInputEditText>(R.id.etIngredientes)
        val etPrecio = view.findViewById<TextInputEditText>(R.id.etPrecio)
        val etStock = view.findViewById<TextInputEditText>(R.id.etStock)

        imgPreviewDialog = view.findViewById(R.id.imgPreview)
        imageUri = null

        // Si editamos, llenamos los datos
        if (medicamentoEditar != null) {
            etNombre.setText(medicamentoEditar.nombre)
            etGramaje.setText(medicamentoEditar.gramaje)
            etPatente.setText(medicamentoEditar.patente)
            etIngredientes.setText(medicamentoEditar.ingredientes)
            etPrecio.setText(medicamentoEditar.precio.toString())
            etStock.setText(medicamentoEditar.stock.toString())

            if (medicamentoEditar.fotoUrl.isNotEmpty()) {
                imgPreviewDialog.imageTintList = null
                Glide.with(this).load(medicamentoEditar.fotoUrl).into(imgPreviewDialog)
            }
        }

        imgPreviewDialog.setOnClickListener { getContent.launch("image/*") }

        builder.setView(view)
        builder.setPositiveButton(if(medicamentoEditar == null) "Guardar" else "Actualizar", null)
        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if(etNombre.text.toString().isEmpty() || etPrecio.text.toString().isEmpty()){
                Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Procesando...", Toast.LENGTH_SHORT).show()

                val medFinal = Medicamento(
                    id = medicamentoEditar?.id ?: "",
                    nombre = etNombre.text.toString(),
                    gramaje = etGramaje.text.toString(),
                    patente = etPatente.text.toString(),
                    ingredientes = etIngredientes.text.toString(),
                    precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0,
                    stock = etStock.text.toString().toIntOrNull() ?: 0,
                    hospitalId = auth.currentUser?.uid ?: "",
                    fotoUrl = medicamentoEditar?.fotoUrl ?: ""
                )

                if (imageUri != null) {
                    subirImagenYGuardar(medFinal, dialog, esEdicion = (medicamentoEditar != null))
                } else {
                    guardarEnFirestore(medFinal, dialog, esEdicion = (medicamentoEditar != null))
                }
            }
        }
    }

    private fun subirImagenYGuardar(med: Medicamento, dialog: AlertDialog, esEdicion: Boolean) {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("medicamentos/$filename")

        try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            val baos = java.io.ByteArrayOutputStream()
            originalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()

            ref.putBytes(data).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    val medConFoto = med.copy(fotoUrl = uri.toString())
                    guardarEnFirestore(medConFoto, dialog, esEdicion)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarEnFirestore(med: Medicamento, dialog: AlertDialog, esEdicion: Boolean) {
        if (esEdicion) {
            db.collection("medicamentos").document(med.id).set(med)
                .addOnSuccessListener {
                    Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
                    cargarInventario()
                    dialog.dismiss()
                }
        } else {
            db.collection("medicamentos").add(med)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registrado", Toast.LENGTH_SHORT).show()
                    cargarInventario()
                    dialog.dismiss()
                }
        }
    }

    private fun borrarMedicamento(med: Medicamento) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Borrar ${med.nombre}?")
            .setPositiveButton("Sí") { _, _ ->
                db.collection("medicamentos").document(med.id).delete()
                    .addOnSuccessListener { cargarInventario() }
            }
            .show()
    }
}

// ADAPTER CORREGIDO
class MedicamentosAdminAdapter(
    private val lista: List<Medicamento>,
    private val onItemClick: (Medicamento) -> Unit,
    private val onDeleteClick: (Medicamento) -> Unit
) : RecyclerView.Adapter<MedicamentosAdminAdapter.Holder>() {

    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val img: ImageView = v.findViewById(R.id.imgProducto)
        val nombre: TextView = v.findViewById(R.id.tvNombreMed)
        val detalles: TextView = v.findViewById(R.id.tvDetallesMed)
        val precio: TextView = v.findViewById(R.id.tvPrecioMed)
        val stock: TextView = v.findViewById(R.id.tvStock)
        val btnDel: ImageButton = v.findViewById(R.id.btnDeleteMed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_medicamento_admin, parent, false)
        return Holder(v)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = lista[position]
        holder.nombre.text = item.nombre
        holder.detalles.text = if(item.gramaje.isNotEmpty()) "${item.gramaje} • ${item.patente}" else item.patente
        holder.precio.text = "$${item.precio}"
        holder.stock.text = "Stock: ${item.stock}"

        if (item.fotoUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context).load(item.fotoUrl).centerCrop().into(holder.img)
        } else {
            holder.img.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Click en la tarjeta completa -> EDITAR
        holder.itemView.setOnClickListener { onItemClick(item) }

        // Click en botón basura -> BORRAR
        holder.btnDel.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = lista.size
}