package com.example.proyect

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appproy.dao.citaDAO
import com.example.proyect.db.CitaDBHelper
import com.example.appproy.model.Cita
import java.util.Calendar
import java.util.Locale

class MenuActivity : AppCompatActivity() {
    private lateinit var citaDAO: citaDAO
    var etNombrePaciente: EditText? = null
    var etTipoCita: EditText? = null
    var spEspecialista: Spinner? = null
    var cbNotificaciones: RadioGroup? = null
    var rbNotiSi: RadioButton? = null
    var rbNotiNo: RadioButton? = null
    var btnGuardar: Button? = null
    var btnSalir: Button? = null
    var btnHistorial: Button? = null
    var tvHistorial: TextView? = null
    var tvCitas: TextView? = null

    var etFechaCita: EditText? = null
    var etHoraCita: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val dbHelper = CitaDBHelper(this)
        citaDAO = citaDAO(dbHelper)

        etNombrePaciente = findViewById(R.id.etNombrePaciente)
        etTipoCita = findViewById(R.id.etTipoCita)
        spEspecialista = findViewById(R.id.spEspecialista)
        cbNotificaciones = findViewById(R.id.cbNotificaciones)
        rbNotiSi = findViewById(R.id.rbNotiSi)
        rbNotiNo = findViewById(R.id.rbNotiNo)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnSalir = findViewById(R.id.btnSalir)
        btnHistorial = findViewById(R.id.btnHistorial)
        tvHistorial = findViewById(R.id.tvHistorial)
        tvCitas = findViewById(R.id.tvCitas)

        // FECHA Y HORA
        etFechaCita = findViewById(R.id.etFechaCita)
        etHoraCita = findViewById(R.id.etHoraCita)

        // diálogos de fecha y hora
        etFechaCita!!.setOnClickListener { mostrarDatePicker() }
        etHoraCita!!.setOnClickListener { mostrarTimePicker() }

        // Spinner doctores
        val especialistas =
            arrayOf<String?>("Médico General", "Pediatra", "Cardiólogo", "Dentista", "Ginecólogo")
        val adapter =
            ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item, especialistas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEspecialista!!.setAdapter(adapter)

        btnGuardar!!.setOnClickListener(View.OnClickListener{ v: View? ->
            guardarCita()
        })

        // Botón Historial (CRUD)
        btnHistorial!!.setOnClickListener(View.OnClickListener { v: View? ->
            mostrarHistorialConAcciones()
        })

        // Botón Salir
        btnSalir!!.setOnClickListener(View.OnClickListener { v: View? ->
            val intent = Intent(this@MenuActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
    }

    private fun guardarCita() {
        val nombre = etNombrePaciente!!.getText().toString()
        val tipoCita = etTipoCita!!.getText().toString()
        val especialista = spEspecialista!!.getSelectedItem().toString()
        val fecha = etFechaCita!!.getText().toString()
        val hora = etHoraCita!!.getText().toString()

        // Validación de campos
        if (tipoCita.isEmpty() || fecha.isEmpty() || hora.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "Debe completar todos los campos.", Toast.LENGTH_LONG).show()
            return
        }

        val nuevaCita = Cita(
            nombrePaciente = nombre,
            especialista = especialista,
            fecha = fecha,
            hora = hora,
            tipoCita = tipoCita
        )

        val idInsertado = citaDAO.insertar(nuevaCita)

        if (idInsertado > 0) {
            Toast.makeText(this, "Cita guardada con ID: $idInsertado", Toast.LENGTH_SHORT).show()
            // Limpiar campos
            etTipoCita!!.setText("")
            etFechaCita!!.setText("")
            etHoraCita!!.setText("")
        } else {
            Toast.makeText(this, "Error al guardar la cita.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarHistorialConAcciones() {
        val listaCitas = citaDAO.listar()

        // Ocultamos los TextViews estáticos
        tvHistorial!!.setVisibility(TextView.GONE)
        tvCitas!!.setVisibility(TextView.GONE)

        if (listaCitas.isEmpty()) {
            Toast.makeText(this, "No hay citas registradas en el historial.", Toast.LENGTH_SHORT).show()
            return
        }

        // AlertDialog
        val citasDisplay = listaCitas.map { cita ->
            "ID ${cita.id}: ${cita.nombrePaciente} - ${cita.especialista} (${cita.fecha})"
        }.toTypedArray()

        // AlertDialog interactivo
        AlertDialog.Builder(this)
            .setTitle("Historial de Citas Médicas")
            .setItems(citasDisplay) { _, which ->
                val citaSeleccionada = listaCitas[which]
                mostrarOpcionesCita(citaSeleccionada)
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun mostrarOpcionesCita(cita: Cita) {
        val opciones = arrayOf("Ver Detalles/Consultar", "Editar/Actualizar", "Eliminar Cita")

        AlertDialog.Builder(this)
            .setTitle("Opciones para Cita ID ${cita.id}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDetalles(cita)
                    1 -> mostrarDialogoEdicion(cita) // Llama a la edición completa (U)
                    2 -> confirmarYEliminarCita(cita) // Llama a la eliminación (D)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDetalles(cita: Cita) {
        AlertDialog.Builder(this)
            .setTitle("Detalles de Cita")
            .setMessage(
                "ID: ${cita.id}\n" +
                        "Paciente: ${cita.nombrePaciente}\n" +
                        "Especialista: ${cita.especialista}\n" +
                        "Tipo: ${cita.tipoCita}\n" +
                        "Fecha: ${cita.fecha}\n" +
                        "Hora: ${cita.hora}"
            )
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun mostrarDialogoEdicion(citaOriginal: Cita) {
        // layout personalizado
        val dialogView = layoutInflater.inflate(R.layout.dialog_editar_cita, null)

        // nuevos campos del diálogo
        val spEspecialistaEdit = dialogView.findViewById<Spinner>(R.id.spEspecialista_edit)
        val etTipoCitaEdit = dialogView.findViewById<EditText>(R.id.etTipoCita_edit)
        val etFechaCitaEdit = dialogView.findViewById<EditText>(R.id.etFechaCita_edit)
        val etHoraCitaEdit = dialogView.findViewById<EditText>(R.id.etHoraCita_edit)

        // nuevo Especialista
        val especialistas =
            arrayOf<String?>("Médico General", "Pediatra", "Cardiólogo", "Dentista", "Ginecólogo")
        val adapter = ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item, especialistas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEspecialistaEdit.adapter = adapter

        //  Precargar datos de la cita original
        etTipoCitaEdit.setText(citaOriginal.tipoCita)
        etFechaCitaEdit.setText(citaOriginal.fecha)
        etHoraCitaEdit.setText(citaOriginal.hora)

        // Seleccionar el especialista correcto
        val especialistaIndex = especialistas.indexOf(citaOriginal.especialista)
        if (especialistaIndex >= 0) {
            spEspecialistaEdit.setSelection(especialistaIndex)
        }

        etFechaCitaEdit.setOnClickListener { mostrarDatePickerGeneric(etFechaCitaEdit) }
        etHoraCitaEdit.setOnClickListener { mostrarTimePickerGeneric(etHoraCitaEdit) }

        //  Construir y mostrar el diálogo
        AlertDialog.Builder(this)
            .setTitle("Editar Cita (ID ${citaOriginal.id})")
            .setMessage("Paciente: ${citaOriginal.nombrePaciente}")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                // Capturar los nuevos valores
                val nuevoTipoCita = etTipoCitaEdit.text.toString()
                val nuevoEspecialista = spEspecialistaEdit.selectedItem.toString()
                val nuevaFecha = etFechaCitaEdit.text.toString()
                val nuevaHora = etHoraCitaEdit.text.toString()

                if (nuevoTipoCita.isNotEmpty() && nuevaFecha.isNotEmpty() && nuevaHora.isNotEmpty()) {
                    // Crear una copia de la cita con los datos editados
                    val citaActualizada = citaOriginal.copy(
                        tipoCita = nuevoTipoCita,
                        especialista = nuevoEspecialista,
                        fecha = nuevaFecha,
                        hora = nuevaHora
                    )

                    // Ejecutar la acción de actualización
                    val filasActualizadas = citaDAO.actualizar(citaActualizada)

                    if (filasActualizadas > 0) {
                        Toast.makeText(this, "Cita ID ${citaOriginal.id} actualizada completamente.", Toast.LENGTH_LONG).show()
                        mostrarHistorialConAcciones()
                    } else {
                        Toast.makeText(this, "Error al actualizar la cita.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Todos los campos de edición son obligatorios.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarYEliminarCita(cita: Cita) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar la cita de ${cita.nombrePaciente}?")
            .setPositiveButton("Sí, Eliminar") { _, _ ->
                val id = cita.id
                // El operador ?: 0 es una verificación de seguridad
                val filasAfectadas = citaDAO.eliminar(id ?: 0)

                if (filasAfectadas > 0) {
                    Toast.makeText(this, "Cita de ID $id eliminada correctamente.", Toast.LENGTH_SHORT).show()
                    // Recargar la lista después de la eliminación
                    mostrarHistorialConAcciones()
                } else {
                    Toast.makeText(this, "Error: No se pudo eliminar la cita.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDatePicker() {
        etFechaCita?.let { mostrarDatePickerGeneric(it) }
    }

    // Función para el formulario principal
    private fun mostrarTimePicker() {
        etHoraCita?.let { mostrarTimePickerGeneric(it) }
    }

    private fun mostrarDatePickerGeneric(editText: EditText) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(fechaSeleccionada)
        }, year, month, day)

        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    private fun mostrarTimePickerGeneric(editText: EditText) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            editText.setText(horaSeleccionada)
        }, hour, minute, true)

        tpd.show()
    }
}
