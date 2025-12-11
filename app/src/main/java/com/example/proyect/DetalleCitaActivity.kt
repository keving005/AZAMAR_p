package com.example.proyect

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DetalleCitaActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var idDocumento: String? = null

    // Variables para guardar temporalmente la nueva selección
    private var nuevaFechaSeleccionada: String = ""
    private var nuevaHoraSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_cita)

        // 1. VINCULAR VISTAS
        val tvNombre = findViewById<TextView>(R.id.tvDetalleNombre)
        val tvMotivo = findViewById<TextView>(R.id.tvDetalleMotivo)
        val tvFecha = findViewById<TextView>(R.id.tvDetalleFecha)
        val tvHora = findViewById<TextView>(R.id.tvDetalleHora)
        val tvSexo = findViewById<TextView>(R.id.tvDetalleSexo)
        val tvSangre = findViewById<TextView>(R.id.tvDetalleSangre)
        val tvPeso = findViewById<TextView>(R.id.tvDetallePeso)
        val tvAltura = findViewById<TextView>(R.id.tvDetalleAltura)
        val tvAlergias = findViewById<TextView>(R.id.tvDetalleAlergias)

        val btnReagendar = findViewById<Button>(R.id.btnReagendar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelarCita)

        // 2. RECIBIR DATOS DEL INTENT (La ficha técnica)
        val extras = intent.extras
        if (extras != null) {
            idDocumento = extras.getString("ID_DOC")
            tvNombre.text = extras.getString("PACIENTE")
            tvMotivo.text = extras.getString("MOTIVO")
            tvFecha.text = "Fecha: ${extras.getString("FECHA")}"
            tvHora.text = "Hora: ${extras.getString("HORA")}"
            tvSexo.text = extras.getString("SEXO")
            tvSangre.text = extras.getString("SANGRE")
            tvPeso.text = "${extras.getString("PESO")} kg"
            tvAltura.text = "${extras.getString("ALTURA")} m"
            tvAlergias.text = extras.getString("ALERGIAS")
        }

        // 3. BOTÓN CANCELAR
        btnCancelar.setOnClickListener {
            mostrarConfirmacionCancelar()
        }

        // 4. BOTÓN REAGENDAR
        btnReagendar.setOnClickListener {
            mostrarBottomSheetCalendario()
        }
    }

    // ========================================================================
    // LÓGICA DE REAGENDAR (DISEÑO PREMIUM)
    // ========================================================================

    // PASO 1: Mostrar Calendario en BottomSheet
    private fun mostrarBottomSheetCalendario() {
        val dialogCalendario = BottomSheetDialog(this)

        // Inflamos el XML del calendario (Asegúrate de tener dialog_calendar_reagendar.xml)
        val view = layoutInflater.inflate(R.layout.dialog_calendar_reagendar, null)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarViewReagendar)

        // Restringir para no poder seleccionar fechas pasadas
        calendarView.minDate = System.currentTimeMillis() - 1000

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Guardamos la fecha en formato estándar
            nuevaFechaSeleccionada = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

            // Cerramos el calendario y abrimos el selector de hora
            dialogCalendario.dismiss()
            mostrarBottomSheetHoraSpinner()
        }

        dialogCalendario.setContentView(view)
        dialogCalendario.show()
    }

    // PASO 2: Mostrar Selector de Hora tipo Lista (Spinner)
    private fun mostrarBottomSheetHoraSpinner() {
        val dialogHora = BottomSheetDialog(this)

        // --- CORRECCIÓN CLAVE AQUÍ ---
        // Usamos el XML NUEVO que tiene los IDs correctos
        val view = layoutInflater.inflate(R.layout.dialog_time_picker_reagendar, null)

        // Ahora sí encontrará estos IDs porque existen en dialog_time_picker_reagendar.xml
        val timePicker = view.findViewById<TimePicker>(R.id.timePickerSpinnerReagendar)
        val btnConfirmar = view.findViewById<Button>(R.id.btnConfirmarReagendar)

        // Configurar para que se vea AM/PM (false = formato 12 horas)
        timePicker.setIs24HourView(false)

        btnConfirmar.setOnClickListener {
            // Obtener hora y minuto del spinner
            val hora = timePicker.hour
            val minuto = timePicker.minute

            // Formatear a "09:30 AM" usando zona horaria CDMX
            val timeZoneCDMX = TimeZone.getTimeZone("America/Mexico_City")
            val calendario = Calendar.getInstance(timeZoneCDMX)
            calendario.set(Calendar.HOUR_OF_DAY, hora)
            calendario.set(Calendar.MINUTE, minuto)

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.timeZone = timeZoneCDMX
            nuevaHoraSeleccionada = sdf.format(calendario.time).uppercase()

            // Confirmamos y actualizamos en Firebase
            dialogHora.dismiss()
            actualizarCitaEnFirebase(nuevaFechaSeleccionada, nuevaHoraSeleccionada)
        }

        dialogHora.setContentView(view)
        dialogHora.show()
    }

    private fun actualizarCitaEnFirebase(fecha: String, hora: String) {
        if (idDocumento != null) {
            Toast.makeText(this, "Reagendando...", Toast.LENGTH_SHORT).show()

            db.collection("citas").document(idDocumento!!)
                .update(
                    mapOf(
                        "fecha" to fecha,
                        "hora" to hora
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "¡Cita reagendada con éxito!", Toast.LENGTH_LONG).show()
                    // Actualizamos los textos en pantalla para que el doctor vea el cambio al instante
                    findViewById<TextView>(R.id.tvDetalleFecha).text = "Fecha: $fecha"
                    findViewById<TextView>(R.id.tvDetalleHora).text = "Hora: $hora"
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al intentar reagendar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ========================================================================
    // LÓGICA DE CANCELAR
    // ========================================================================
    private fun mostrarConfirmacionCancelar() {
        AlertDialog.Builder(this)
            .setTitle("Cancelar Cita")
            .setMessage("¿Estás seguro? Esta acción borrará la cita permanentemente.")
            .setPositiveButton("Sí, Cancelar") { _, _ ->
                eliminarCita()
            }
            .setNegativeButton("No, Regresar", null)
            .show()
    }

    private fun eliminarCita() {
        if (idDocumento != null) {
            db.collection("citas").document(idDocumento!!)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show()
                    finish() // Cierra la pantalla y vuelve a la lista
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cancelar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}