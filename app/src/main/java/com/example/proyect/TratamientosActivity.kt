package com.example.proyect

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TratamientosActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val listaTratamientos = mutableListOf<Tratamiento>()
    private lateinit var adapter: TratamientosAdapter
    private lateinit var rvTratamientos: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tratamientos)

        // Configurar Recycler
        rvTratamientos = findViewById(R.id.rvTratamientos)
        rvTratamientos.layoutManager = LinearLayoutManager(this)
        adapter = TratamientosAdapter(listaTratamientos)
        rvTratamientos.adapter = adapter

        // Botón Regresar
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Botón Agregar (+)
        findViewById<FloatingActionButton>(R.id.fabAgregarTratamiento).setOnClickListener {
            mostrarDialogoAgregar()
        }

        // Cargar datos
        cargarTratamientos()
    }

    private fun cargarTratamientos() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("tratamientos")
            .whereEqualTo("uidUsuario", uid)
            .get()
            .addOnSuccessListener { result ->
                listaTratamientos.clear()
                for (doc in result) {
                    val t = doc.toObject(Tratamiento::class.java)
                    t.id = doc.id
                    listaTratamientos.add(t)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun mostrarDialogoAgregar() {
        // Inflamos TU diseño: dialog_agregar_tratamiento.xml
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_agregar_tratamiento, null)

        val etMed = view.findViewById<EditText>(R.id.etDialogMedicamento)
        val etDosis = view.findViewById<EditText>(R.id.etDialogDosis)
        val etFrec = view.findViewById<EditText>(R.id.etDialogFrecuencia)
        val etDur = view.findViewById<EditText>(R.id.etDialogDuracion)

        // Como tu XML no tiene botones de Guardar/Cancelar, los agregamos con el AlertDialog
        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Guardar") { dialog, _ ->
                val nombre = etMed.text.toString()
                val dosis = etDosis.text.toString()
                val frec = etFrec.text.toString()
                val dur = etDur.text.toString()

                if (nombre.isNotEmpty() && frec.isNotEmpty()) {
                    guardarEnFirebase(nombre, dosis, frec, dur)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Falta nombre o frecuencia", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun guardarEnFirebase(nombre: String, dosis: String, frec: String, dur: String) {
        val uid = auth.currentUser?.uid ?: return

        val nuevoTratamiento = Tratamiento(
            uidUsuario = uid,
            nombreMedicamento = nombre,
            dosis = dosis,
            frecuenciaHoras = frec,
            duracionDias = dur
        )

        db.collection("tratamientos").add(nuevoTratamiento)
            .addOnSuccessListener {
                Toast.makeText(this, "Tratamiento agregado", Toast.LENGTH_SHORT).show()
                cargarTratamientos() // Recargar lista
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }
}