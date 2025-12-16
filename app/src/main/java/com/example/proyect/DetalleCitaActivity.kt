package com.example.proyect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DetalleCitaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idDocumento: String? = null

    // IDs clave para la lógica
    private var currentDoctorId: String = ""
    private var currentPacienteId: String = "" // ¡Necesario para la receta!

    // Vistas Detalle
    private lateinit var tvNombre: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvHora: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvNacimiento: TextView
    private lateinit var tvGenero: TextView
    private lateinit var tvAfiliacion: TextView
    private lateinit var tvTipoCita: TextView
    private lateinit var tvMotivo: TextView

    // Variables temporales para reagendar
    private var nuevaFechaVisual: String = ""
    private var nuevaFechaLogica: String = ""
    private var nuevaHoraSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cita)

        // 1. VINCULAR VISTAS
        tvNombre = findViewById(R.id.tvDetalleNombre)
        tvFecha = findViewById(R.id.tvDetalleFecha)
        tvHora = findViewById(R.id.tvDetalleHora)
        tvTelefono = findViewById(R.id.tvDetalleTelefono)
        tvCorreo = findViewById(R.id.tvDetalleCorreo)
        tvNacimiento = findViewById(R.id.tvDetalleNacimiento)
        tvGenero = findViewById(R.id.tvDetalleGenero)
        tvAfiliacion = findViewById(R.id.tvDetalleAfiliacion)
        tvTipoCita = findViewById(R.id.tvDetalleTipoCita)
        tvMotivo = findViewById(R.id.tvDetalleMotivo)

        val btnReagendar = findViewById<Button>(R.id.btnReagendar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarCita)

        // Botón Nuevo: Escribir Receta
        val btnEscribirReceta = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEscribirReceta)

        // 2. RECIBIR ID Y CARGAR
        val extras = intent.extras
        if (extras != null) {
            idDocumento = extras.getString("ID_DOC")
            cargarDatosCompletosDeFirebase()
        }

        // 3. LISTENERS

        // Cancelar
        btnCancelar.setOnClickListener { mostrarConfirmacionCancelar() }

        // Reagendar
        btnReagendar.setOnClickListener {
            if (currentDoctorId.isNotEmpty()) {
                mostrarBottomSheetCalendario()
            } else {
                Toast.makeText(this, "Cargando datos del doctor...", Toast.LENGTH_SHORT).show()
            }
        }

        // Escribir Receta (NUEVO)
        btnEscribirReceta.setOnClickListener {
            if (idDocumento.isNullOrEmpty() || currentPacienteId.isEmpty()) {
                Toast.makeText(this, "Faltan datos para crear la receta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CrearRecetaActivity::class.java)
            intent.putExtra("CITA_ID", idDocumento)
            intent.putExtra("PACIENTE_ID", currentPacienteId) // Enviamos el ID real
            intent.putExtra("PACIENTE_NOMBRE", tvNombre.text.toString())
            startActivity(intent)
        }
    }

    private fun cargarDatosCompletosDeFirebase() {
        if (idDocumento.isNullOrEmpty()) return

        db.collection("citas").document(idDocumento!!).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // Guardamos IDs importantes
                    currentDoctorId = doc.getString("doctorId") ?: ""
                    currentPacienteId = doc.getString("uidUsuario") ?: "" // ID del paciente

                    // Llenar UI
                    tvNombre.text = doc.getString("nombrePaciente") ?: "Sin nombre"
                    tvFecha.text = doc.getString("fecha") ?: "--"
                    tvHora.text = doc.getString("hora") ?: "--"

                    tvTelefono.text = doc.getString("telefono") ?: "No registrado"
                    tvCorreo.text = doc.getString("correo") ?: "No registrado"
                    tvNacimiento.text = doc.getString("fechaNacimiento") ?: "No registrada"
                    tvGenero.text = doc.getString("genero") ?: "No especificado"

                    val esAfiliado = doc.getBoolean("esAfiliado") ?: false
                    tvAfiliacion.text = if (esAfiliado) "SÍ - ${doc.getString("numeroAfiliado")}" else "NO Afiliado"

                    tvTipoCita.text = "Tipo: ${doc.getString("tipoCita")}"
                    tvMotivo.text = doc.getString("motivo") ?: ""
                }
            }
    }

    // ========================================================================
    // LÓGICA DE REAGENDAR (VALIDACIÓN DE HORARIOS)
    // ========================================================================

    private fun mostrarBottomSheetCalendario() {
        val dialogCalendario = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_calendar_reagendar, null)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarViewReagendar)

        calendarView.minDate = System.currentTimeMillis() - 1000

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            nuevaFechaVisual = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            nuevaFechaLogica = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth)

            dialogCalendario.dismiss()
            mostrarBottomSheetHorariosDisponibles()
        }
        dialogCalendario.setContentView(view)
        dialogCalendario.show()
    }

    private fun mostrarBottomSheetHorariosDisponibles() {
        val dialogHora = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_time_picker_reagendar, null)

        val tvFechaLabel = view.findViewById<TextView>(R.id.tvFechaSeleccionadaLabel)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarHorarios)
        val tvError = view.findViewById<TextView>(R.id.tvSinHorarios)
        val spHorarios = view.findViewById<Spinner>(R.id.spHorariosReagendar)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarReagendar)

        tvFechaLabel.text = "Disponibilidad: $nuevaFechaVisual"

        // Consultar Agenda
        val agendaId = "${currentDoctorId}_$nuevaFechaLogica"

        db.collection("agenda_doctores").document(agendaId).get()
            .addOnSuccessListener { docAgenda ->
                if (!docAgenda.exists() || docAgenda.getBoolean("esDiaLibre") == true) {
                    progressBar.visibility = View.GONE
                    tvError.text = "El doctor no trabaja este día."
                    tvError.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                val inicio = docAgenda.getString("horaInicio") ?: "09:00"
                val fin = docAgenda.getString("horaFin") ?: "17:00"
                val comidaIni = docAgenda.getString("comidaInicio") ?: "14:00"
                val comidaFin = docAgenda.getString("comidaFin") ?: "15:00"

                // Consultar Citas Ocupadas
                db.collection("citas")
                    .whereEqualTo("doctorId", currentDoctorId)
                    .whereEqualTo("fechaAgenda", nuevaFechaLogica)
                    .get()
                    .addOnSuccessListener { citasSnap ->
                        val horasOcupadas = mutableListOf<String>()
                        for (cita in citasSnap) {
                            if (cita.id != idDocumento) { // Ignorar mi propia cita actual
                                horasOcupadas.add(cita.getString("hora") ?: "")
                            }
                        }

                        val slots = generarSlots(inicio, fin, comidaIni, comidaFin, horasOcupadas)
                        progressBar.visibility = View.GONE

                        if (slots.isEmpty()) {
                            tvError.text = "Agenda llena."
                            tvError.visibility = View.VISIBLE
                        } else {
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, slots)
                            spHorarios.adapter = adapter
                            spHorarios.visibility = View.VISIBLE
                            btnConfirmar.isEnabled = true
                        }
                    }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                tvError.text = "Error de conexión."
                tvError.visibility = View.VISIBLE
            }

        btnConfirmar.setOnClickListener {
            if (spHorarios.selectedItem != null) {
                nuevaHoraSeleccionada = spHorarios.selectedItem.toString()
                dialogHora.dismiss()
                actualizarCitaEnFirebase(nuevaFechaVisual, nuevaFechaLogica, nuevaHoraSeleccionada)
            }
        }

        dialogHora.setContentView(view)
        dialogHora.show()
    }

    private fun generarSlots(inicio: String, fin: String, comIni: String, comFin: String, ocupadas: List<String>): List<String> {
        val slots = mutableListOf<String>()
        val startMin = timeToMinutes(inicio)
        val endMin = timeToMinutes(fin)
        val lunchStart = timeToMinutes(comIni)
        val lunchEnd = timeToMinutes(comFin)

        var current = startMin
        while (current < endMin) {
            if (current >= lunchStart && current < lunchEnd) {
                current += 30
                continue
            }
            val horaString = minutesToTime(current)
            if (!ocupadas.contains(horaString)) {
                slots.add(horaString)
            }
            current += 30
        }
        return slots
    }

    private fun timeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) { 0 }
    }

    private fun minutesToTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format("%02d:%02d", h, m)
    }

    private fun actualizarCitaEnFirebase(fechaVisual: String, fechaLogica: String, hora: String) {
        if (idDocumento != null) {
            Toast.makeText(this, "Guardando...", Toast.LENGTH_SHORT).show()
            db.collection("citas").document(idDocumento!!)
                .update(mapOf("fecha" to fechaVisual, "fechaAgenda" to fechaLogica, "hora" to hora))
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Reagendado!", Toast.LENGTH_LONG).show()
                    tvFecha.text = fechaVisual
                    tvHora.text = hora
                }
        }
    }

    // ========================================================================
    // CANCELAR CITA
    // ========================================================================
    private fun mostrarConfirmacionCancelar() {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Seguro que deseas cancelar?")
            .setPositiveButton("Sí, Cancelar") { _, _ -> eliminarCita() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun eliminarCita() {
        if (idDocumento != null) {
            db.collection("citas").document(idDocumento!!).delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }
}