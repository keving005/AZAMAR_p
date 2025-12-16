package com.example.proyect

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrearRecetaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Datos
    private var citaId: String = ""
    private var pacienteId: String = ""
    private var pacienteNombre: String = ""
    private var doctorId: String = ""
    private var doctorNombre: String = "" // Guardamos el nombre del doctor también

    // Lista de medicamentos seleccionados
    private val listaFinalReceta = mutableListOf<Medicamento>()

    // Vistas
    private lateinit var contenedorMedicamentos: LinearLayout
    private lateinit var tvPlaceholder: TextView
    private lateinit var btnGenerarQR: Button

    // --- LANZADOR PARA ABRIR LA FARMACIA ---
    private val farmaciaLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val seleccionados = data?.getParcelableArrayListExtra<Medicamento>("MEDICAMENTOS_SELECCIONADOS")

            if (seleccionados != null) {
                for (nuevo in seleccionados) {
                    // Evitar duplicados visuales (si ya está, actualizamos cantidad o lo ignoramos)
                    val existente = listaFinalReceta.find { it.id == nuevo.id }
                    if (existente == null) {
                        listaFinalReceta.add(nuevo)
                    } else {
                        // Opcional: Actualizar cantidad si cambia
                        existente.cantidadSeleccionada = nuevo.cantidadSeleccionada
                    }
                }
                actualizarVistaMedicamentos()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_receta)

        // Vincular Vistas
        val tvHospital = findViewById<TextView>(R.id.tvRecetaHospital)
        val tvDoctor = findViewById<TextView>(R.id.tvRecetaDoctor)
        val tvFecha = findViewById<TextView>(R.id.tvRecetaFecha)
        val tvPaciente = findViewById<TextView>(R.id.tvRecetaPacienteNombre)
        val etInstrucciones = findViewById<TextInputEditText>(R.id.etRecetaInstrucciones)

        val btnAgregarMeds = findViewById<Button>(R.id.btnAgregarMeds)
        btnGenerarQR = findViewById<Button>(R.id.btnGenerarQR)

        contenedorMedicamentos = findViewById(R.id.llListaMedicamentos)
        tvPlaceholder = findViewById(R.id.tvListaMedsPlaceholder)

        // Datos del Intent
        citaId = intent.getStringExtra("CITA_ID") ?: ""
        pacienteId = intent.getStringExtra("PACIENTE_ID") ?: ""
        pacienteNombre = intent.getStringExtra("PACIENTE_NOMBRE") ?: "Paciente"
        doctorId = auth.currentUser?.uid ?: ""

        tvPaciente.text = pacienteNombre
        val fechaHoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvFecha.text = "Fecha: $fechaHoy"

        cargarDatosDoctorMembrete(tvDoctor, tvHospital)

        // --- LISTENERS ---

        btnAgregarMeds.setOnClickListener {
            val intent = Intent(this, FarmaciaPacienteActivity::class.java)
            intent.putExtra("MODO_SELECTOR", true)
            farmaciaLauncher.launch(intent)
        }

        btnGenerarQR.setOnClickListener {
            val instrucciones = etInstrucciones.text.toString()

            if (instrucciones.isEmpty()) {
                etInstrucciones.error = "Escribe las indicaciones"
                return@setOnClickListener
            }

            if (listaFinalReceta.isEmpty()) {
                Toast.makeText(this, "⚠️ Agrega medicamentos primero", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // AHORA SÍ: Guardar de verdad
            guardarRecetaEnFirebase(instrucciones)
        }
    }

    private fun guardarRecetaEnFirebase(instrucciones: String) {
        btnGenerarQR.isEnabled = false // Evitar doble clic
        btnGenerarQR.text = "Guardando..."

        // 1. Preparar la lista de medicamentos en formato Mapa para Firebase
        val medicamentosMap = listaFinalReceta.map { med ->
            mapOf(
                "id" to med.id,
                "nombre" to med.nombre,
                "gramaje" to med.gramaje,
                "cantidad" to med.cantidadSeleccionada
            )
        }

        // 2. Crear el objeto receta
        val nuevaReceta = hashMapOf(
            "citaId" to citaId,
            "doctorId" to doctorId,
            "pacienteId" to pacienteId,
            "nombrePaciente" to pacienteNombre,
            "nombreDoctor" to doctorNombre,
            "fecha" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            "timestamp" to System.currentTimeMillis(), // Para ordenar
            "instrucciones" to instrucciones,
            "medicamentos" to medicamentosMap,
            "estado" to "ACTIVA" // Activa, Surtida, Cancelada
        )

        // 3. Subir a Firebase
        db.collection("recetas")
            .add(nuevaReceta)
            .addOnSuccessListener { documento ->
                // ¡Éxito! El documento tiene ID. Generamos el QR con ese ID.
                val idReceta = documento.id
                mostrarDialogoQR(idReceta)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar receta", Toast.LENGTH_SHORT).show()
                btnGenerarQR.isEnabled = true
                btnGenerarQR.text = "Firmar y Generar QR"
            }
    }

    private fun mostrarDialogoQR(contenidoQR: String) {
        try {
            // Generar Bitmap del QR
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(contenidoQR, BarcodeFormat.QR_CODE, 600, 600)

            // Crear vista personalizada para el Alert
            val view = layoutInflater.inflate(R.layout.dialog_qr_exito, null) // Crearemos este XML abajo rápido
            val ivQR = view.findViewById<ImageView>(R.id.ivQrGenerado)
            val btnFinalizar = view.findViewById<Button>(R.id.btnCerrarReceta)

            ivQR.setImageBitmap(bitmap)

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()

            btnFinalizar.setOnClickListener {
                dialog.dismiss()
                finish() // Cerramos la pantalla de receta y volvemos al detalle
            }

            dialog.show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Receta guardada, pero error al mostrar QR", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun actualizarVistaMedicamentos() {
        contenedorMedicamentos.removeAllViews()

        if (listaFinalReceta.isEmpty()) {
            contenedorMedicamentos.addView(tvPlaceholder)
            tvPlaceholder.visibility = View.VISIBLE
            return
        }

        tvPlaceholder.visibility = View.GONE

        for (med in listaFinalReceta) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_medicamento_simple, contenedorMedicamentos, false)

            val tvNombre = view.findViewById<TextView>(R.id.tvNombreMedItem)
            val tvDetalle = view.findViewById<TextView>(R.id.tvDetalleMedItem)
            val btnDelete = view.findViewById<ImageView>(R.id.btnAgregarItem)

            tvNombre.text = med.nombre
            tvDetalle.text = "${med.gramaje} - Cant: ${med.cantidadSeleccionada}"

            btnDelete.setImageResource(android.R.drawable.ic_menu_delete)
            btnDelete.setColorFilter(android.graphics.Color.RED)

            btnDelete.setOnClickListener {
                listaFinalReceta.remove(med)
                actualizarVistaMedicamentos()
            }

            contenedorMedicamentos.addView(view)
        }
    }

    private fun cargarDatosDoctorMembrete(tvDoc: TextView, tvHosp: TextView) {
        if (doctorId.isEmpty()) return
        db.collection("usuarios").document(doctorId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombre = "${doc.getString("nombre")} ${doc.getString("apellidos")}"
                doctorNombre = nombre // Guardamos en variable global para la BD
                tvDoc.text = "Dr. $nombre"
                tvHosp.text = doc.getString("hospitalNombre") ?: "Hospital General"
            }
        }
    }
}