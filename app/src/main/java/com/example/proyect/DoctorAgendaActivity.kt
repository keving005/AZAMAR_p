package com.example.proyect // CAMBIA ESTO POR TU PAQUETE REAL (aparece en la primera línea de tus otros archivos)

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DoctorAgendaActivity : AppCompatActivity() {

    // Componentes de la interfaz
    private lateinit var calendarView: CalendarView
    private lateinit var tvDateSelected: TextView
    private lateinit var switchDayOff: Switch

    // Botones de hora
    private lateinit var btnAtencionInicio: Button
    private lateinit var btnAtencionFin: Button
    private lateinit var btnComidaInicio: Button
    private lateinit var btnComidaFin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_agenda)

        // 1. Vincular vistas
        calendarView = findViewById(R.id.calendarView)
        tvDateSelected = findViewById(R.id.tvDateSelected)
        switchDayOff = findViewById(R.id.switchDayOff)

        btnAtencionInicio = findViewById(R.id.btnAtencionInicio)
        btnAtencionFin = findViewById(R.id.btnAtencionFin)
        btnComidaInicio = findViewById(R.id.btnComidaInicio)
        btnComidaFin = findViewById(R.id.btnComidaFin)

        // 2. Establecer fecha inicial en el texto
        val calendar = Calendar.getInstance()
        updateDateText(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // 3. Listener del Calendario (Cuando cambias de día)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Nota: month es 0 para Enero, 11 para Diciembre
            updateDateText(year, month, dayOfMonth)

            // Aquí puedes agregar la lógica para cargar datos de la base de datos para ese día
            Toast.makeText(this, "Cargando agenda...", Toast.LENGTH_SHORT).show()
        }

        // 4. Listener del Switch (Día Libre)
        switchDayOff.setOnCheckedChangeListener { _, isChecked ->
            toggleTimeButtons(!isChecked) // Si es día libre (checked), desactivamos botones
            if (isChecked) {
                Toast.makeText(this, "Día marcado como NO LABORAL", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. Ejemplo de click en botón de hora
        btnAtencionInicio.setOnClickListener {
            Toast.makeText(this, "Aquí abrirías el selector de hora", Toast.LENGTH_SHORT).show()
            // Aquí implementarías el TimePickerDialog
        }
    }

    private fun updateDateText(year: Int, month: Int, day: Int) {
        // Sumamos 1 al mes porque Calendar.MONTH empieza en 0
        val mesReal = month + 1
        // Formateamos para que siempre tenga dos dígitos (ej. 05 en vez de 5) si quieres, o simple:
        val fecha = "$year-$mesReal-$day"
        tvDateSelected.text = "Programando para: $fecha"
    }

    private fun toggleTimeButtons(enabled: Boolean) {
        btnAtencionInicio.isEnabled = enabled
        btnAtencionFin.isEnabled = enabled
        btnComidaInicio.isEnabled = enabled
        btnComidaFin.isEnabled = enabled

        // Efecto visual opcional (cambiar opacidad)
        val alpha = if (enabled) 1.0f else 0.5f
        btnAtencionInicio.alpha = alpha
        btnAtencionFin.alpha = alpha
        btnComidaInicio.alpha = alpha
        btnComidaFin.alpha = alpha
    }
}