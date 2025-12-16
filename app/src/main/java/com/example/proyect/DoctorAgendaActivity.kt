package com.example.proyect

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class DoctorAgendaActivity : AppCompatActivity() {

    // Componentes UI
    private lateinit var calendarView: CalendarView
    private lateinit var tvDateSelected: TextView
    private lateinit var switchDayOff: Switch

    private lateinit var btnAtencionInicio: Button
    private lateinit var btnAtencionFin: Button
    private lateinit var btnComidaInicio: Button
    private lateinit var btnComidaFin: Button

    // Botón para guardar (Debes agregarlo a tu XML o usar un FAB)
    // Si no tienes botón en el XML, puedes agregar un FAB flotante programáticamente o en el layout
    // Aquí asumo que agregaste un botón o usaremos una lógica automática.
    // Para ser práctico, guardaremos cada vez que cambien algo o agregaremos un botón flotante.

    // Variables de datos
    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var fechaFormateada = ""

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_agenda)

        // Inicializar fecha hoy
        val c = Calendar.getInstance()
        selectedYear = c.get(Calendar.YEAR)
        selectedMonth = c.get(Calendar.MONTH)
        selectedDay = c.get(Calendar.DAY_OF_MONTH)
        actualizarFechaString()

        // Vincular vistas
        calendarView = findViewById(R.id.calendarView)
        tvDateSelected = findViewById(R.id.tvDateSelected)
        switchDayOff = findViewById(R.id.switchDayOff)

        btnAtencionInicio = findViewById(R.id.btnAtencionInicio)
        btnAtencionFin = findViewById(R.id.btnAtencionFin)
        btnComidaInicio = findViewById(R.id.btnComidaInicio)
        btnComidaFin = findViewById(R.id.btnComidaFin)

        // Actualizar texto inicial
        tvDateSelected.text = "Programando para: $fechaFormateada"
        cargarAgendaDelDia() // Ver si ya existe configuración guardada

        // --- LISTENERS ---

        // 1. Calendario
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth
            actualizarFechaString()
            tvDateSelected.text = "Programando para: $fechaFormateada"
            cargarAgendaDelDia() // Cargar datos de ese nuevo día
        }

        // 2. Switch Día Libre
        switchDayOff.setOnCheckedChangeListener { _, isChecked ->
            toggleTimeButtons(!isChecked)
            guardarConfiguracion() // Guardado automático al cambiar switch
        }

        // 3. Botones de Hora (Abren TimePicker)
        btnAtencionInicio.setOnClickListener { abrirReloj(btnAtencionInicio) }
        btnAtencionFin.setOnClickListener { abrirReloj(btnAtencionFin) }
        btnComidaInicio.setOnClickListener { abrirReloj(btnComidaInicio) }
        btnComidaFin.setOnClickListener { abrirReloj(btnComidaFin) }
    }

    private fun actualizarFechaString() {
        // Formato estricto: YYYY-MM-DD (ej: 2025-05-09)
        fechaFormateada = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
    }

    private fun abrirReloj(boton: Button) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val horaTexto = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
            boton.text = horaTexto
            guardarConfiguracion() // Guardado automático al cambiar hora
        }
        TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun toggleTimeButtons(enabled: Boolean) {
        btnAtencionInicio.isEnabled = enabled
        btnAtencionFin.isEnabled = enabled
        btnComidaInicio.isEnabled = enabled
        btnComidaFin.isEnabled = enabled
        val alpha = if (enabled) 1.0f else 0.5f
        btnAtencionInicio.alpha = alpha
        btnAtencionFin.alpha = alpha
        btnComidaInicio.alpha = alpha
        btnComidaFin.alpha = alpha
    }

    // --- LÓGICA FIREBASE ---

    private fun guardarConfiguracion() {
        val uidDoctor = auth.currentUser?.uid ?: return

        // Creamos un ID único para ese doctor en ese día
        val docId = "${uidDoctor}_$fechaFormateada"

        val agendaMap = hashMapOf(
            "doctorId" to uidDoctor,
            "fecha" to fechaFormateada,
            "esDiaLibre" to switchDayOff.isChecked,
            "horaInicio" to btnAtencionInicio.text.toString(),
            "horaFin" to btnAtencionFin.text.toString(),
            "comidaInicio" to btnComidaInicio.text.toString(),
            "comidaFin" to btnComidaFin.text.toString()
        )

        db.collection("agenda_doctores").document(docId)
            .set(agendaMap)
            .addOnSuccessListener {
                // Opcional: Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarAgendaDelDia() {
        val uidDoctor = auth.currentUser?.uid ?: return
        val docId = "${uidDoctor}_$fechaFormateada"

        db.collection("agenda_doctores").document(docId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Si ya configuró este día, cargamos sus datos
                    val esLibre = document.getBoolean("esDiaLibre") ?: false
                    switchDayOff.isChecked = esLibre

                    btnAtencionInicio.text = document.getString("horaInicio") ?: "08:00"
                    btnAtencionFin.text = document.getString("horaFin") ?: "16:00"
                    btnComidaInicio.text = document.getString("comidaInicio") ?: "14:00"
                    btnComidaFin.text = document.getString("comidaFin") ?: "15:00"
                } else {
                    // Si no ha configurado, ponemos valores por defecto
                    switchDayOff.isChecked = false
                    btnAtencionInicio.text = "08:00"
                    btnAtencionFin.text = "16:00"
                    btnComidaInicio.text = "14:00"
                    btnComidaFin.text = "15:00"
                }
            }
    }
}