package com.example.proyect

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.widget.NestedScrollView // IMPORTANTE: Agregado para arreglar el error
import androidx.drawerlayout.widget.DrawerLayout
import com.example.proyect.DB.DBHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var dbHelper: DBHelper

    // --- CORRECCIÓN AQUÍ: Ahora es NestedScrollView ---
    private lateinit var layoutDashboard: NestedScrollView
    private lateinit var layoutFormulario: View // Este también es un NestedScrollView en el XML, pero como View funciona bien
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // Dashboard UI
    private lateinit var tvUserNameDashboard: TextView
    private lateinit var tvDashEspecialista: TextView
    private lateinit var tvDashFecha: TextView

    // Formulario UI
    private lateinit var etNombrePaciente: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var etFechaNacimiento: EditText
    private lateinit var spGenero: Spinner
    private lateinit var cbAfiliado: CheckBox
    private lateinit var etNumeroAfiliado: EditText

    private lateinit var spHospital: Spinner
    private lateinit var spDoctor: Spinner
    private lateinit var spTipoCita: Spinner
    private lateinit var etMotivo: EditText

    private lateinit var etFechaCita: EditText
    private lateinit var spHorarios: Spinner
    private lateinit var btnGuardar: Button

    // Variables de Lógica
    private val listaHospitalesNombres = mutableListOf<String>()
    private val listaHospitalesIds = mutableListOf<String>()

    private val listaDoctoresNombres = mutableListOf<String>()
    private val listaDoctoresIds = mutableListOf<String>()

    private var selectedHospitalId: String = ""
    private var selectedDoctorId: String = ""
    private var fechaSeleccionadaFormato: String = "" // YYYY-MM-DD para la BD

    private val REQUEST_HOSPITAL = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        dbHelper = DBHelper(this)

        // 1. Inicializar Vistas Estructurales
        // Al hacer findViewById, ahora lo encontrará correctamente como NestedScrollView
        layoutDashboard = findViewById(R.id.layoutDashboard)
        layoutFormulario = findViewById(R.id.layoutFormulario)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        tvUserNameDashboard = findViewById(R.id.tvUserNameDashboard)
        tvDashEspecialista = findViewById(R.id.tvDashEspecialista)
        tvDashFecha = findViewById(R.id.tvDashFecha)

        // 2. Configurar Menú Lateral
        navigationView.setNavigationItemSelectedListener(this)
        findViewById<ImageView>(R.id.btnMenuHamburguesa).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 3. Inicializar Formulario y Cargar Datos
        inicializarVistasFormulario()
        cargarDatosUsuario()
        cargarHospitales()

        // 4. Botones del Dashboard

        // Botón Cita (Grande)
        findViewById<CardView>(R.id.btnAgendarCitaMain).setOnClickListener {
            mostrarFormulario(true)
        }

        // Botón Farmacia
        findViewById<CardView>(R.id.btnFarmaciaMain).setOnClickListener {
            startActivity(Intent(this, FarmaciaPacienteActivity::class.java))
        }

        // --- BOTÓN RECETAS (ACTUALIZADO) ---
        findViewById<CardView>(R.id.btnRecetasMain).setOnClickListener {
            startActivity(Intent(this, MisRecetasActivity::class.java))
        }

        // Botón Mapa
        findViewById<CardView>(R.id.btnBuscarEspecialista).setOnClickListener {
            abrirMapa()
        }

        // Botón Regresar (Flecha dentro del formulario)
        findViewById<ImageView>(R.id.btnRegresarDashboard).setOnClickListener {
            mostrarFormulario(false)
        }

        // Cargar datos del dashboard al iniciar
        actualizarDashboard()
    }

    private fun inicializarVistasFormulario() {
        etNombrePaciente = findViewById(R.id.etNombrePaciente)
        etTelefono = findViewById(R.id.etTelefono)
        etCorreo = findViewById(R.id.etCorreo)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        spGenero = findViewById(R.id.spGenero)
        cbAfiliado = findViewById(R.id.cbAfiliado)
        etNumeroAfiliado = findViewById(R.id.etNumeroAfiliado)

        spHospital = findViewById(R.id.spHospital)
        spDoctor = findViewById(R.id.spDoctor)
        spTipoCita = findViewById(R.id.spTipoCita)
        etMotivo = findViewById(R.id.etMotivo)

        etFechaCita = findViewById(R.id.etFechaCita)
        spHorarios = findViewById(R.id.spHorarios)
        btnGuardar = findViewById(R.id.btnGuardar)

        val generos = arrayOf("Masculino", "Femenino", "Otro")
        spGenero.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, generos)

        val tiposCita = arrayOf("Primera Vez", "Seguimiento", "Consulta General", "Urgencia Menor")
        spTipoCita.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposCita)

        cbAfiliado.setOnCheckedChangeListener { _, isChecked ->
            etNumeroAfiliado.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        etFechaNacimiento.setOnClickListener { mostrarSelectorNacimientoPremium() }
        etFechaCita.setOnClickListener { mostrarCalendarioCitaPremium() }

        btnGuardar.setOnClickListener { validarYGuardarCita() }
    }

    // --- CARGA DE DATOS ---

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("usuarios").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombre = doc.getString("nombre") ?: ""
                val apellidos = doc.getString("apellidos") ?: ""
                val nombreCompleto = "$nombre $apellidos".trim()

                etNombrePaciente.setText(nombreCompleto)
                tvUserNameDashboard.text = nombreCompleto

                if (doc.getBoolean("esAfiliado") == true) {
                    cbAfiliado.isChecked = true
                    etNumeroAfiliado.setText(doc.getString("numeroAfiliacion") ?: "")
                }
            }
        }
    }

    private fun cargarHospitales() {
        db.collection("usuarios").whereEqualTo("rol", 3).get()
            .addOnSuccessListener { result ->
                listaHospitalesNombres.clear()
                listaHospitalesIds.clear()

                listaHospitalesNombres.add("Seleccione Hospital")
                listaHospitalesIds.add("")

                for (doc in result) {
                    val nombre = doc.getString("nombre") ?: "Hospital"
                    listaHospitalesNombres.add(nombre)
                    listaHospitalesIds.add(doc.id)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaHospitalesNombres)
                spHospital.adapter = adapter

                spHospital.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position > 0) {
                            selectedHospitalId = listaHospitalesIds[position]
                            cargarDoctores(selectedHospitalId)
                        } else {
                            selectedHospitalId = ""
                            limpiarDoctores()
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }

    private fun cargarDoctores(hospitalId: String) {
        db.collection("usuarios")
            .whereEqualTo("rol", 2)
            .whereEqualTo("hospitalId", hospitalId)
            .get()
            .addOnSuccessListener { result ->
                listaDoctoresNombres.clear()
                listaDoctoresIds.clear()

                listaDoctoresNombres.add("Seleccione Doctor")
                listaDoctoresIds.add("")

                for (doc in result) {
                    val nombre = doc.getString("nombre") ?: ""
                    val apellidos = doc.getString("apellidos") ?: ""
                    val nombreDoc = "$nombre $apellidos".trim()
                    val nombreFinal = if (nombreDoc.isNotEmpty()) nombreDoc else "Doctor"

                    val especialidad = doc.getString("especialidad") ?: "General"

                    listaDoctoresNombres.add("$nombreFinal ($especialidad)")
                    listaDoctoresIds.add(doc.id)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaDoctoresNombres)
                spDoctor.adapter = adapter

                spDoctor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedDoctorId = if (position > 0) listaDoctoresIds[position] else ""
                        spHorarios.adapter = null
                        if (fechaSeleccionadaFormato.isNotEmpty() && selectedDoctorId.isNotEmpty()) {
                            cargarHorariosDisponibles()
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
    }

    private fun limpiarDoctores() {
        listaDoctoresNombres.clear()
        listaDoctoresNombres.add("Seleccione primero un hospital")
        spDoctor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaDoctoresNombres)
    }

    // --- LÓGICA DE HORARIOS ---

    private fun cargarHorariosDisponibles() {
        Toast.makeText(this, "Consultando agenda...", Toast.LENGTH_SHORT).show()
        val agendaId = "${selectedDoctorId}_$fechaSeleccionadaFormato"

        db.collection("agenda_doctores").document(agendaId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists() || doc.getBoolean("esDiaLibre") == true) {
                    mostrarSinHorarios("El doctor no labora este día")
                    return@addOnSuccessListener
                }

                val inicio = doc.getString("horaInicio") ?: "09:00"
                val fin = doc.getString("horaFin") ?: "17:00"
                val comidaIni = doc.getString("comidaInicio") ?: "14:00"
                val comidaFin = doc.getString("comidaFin") ?: "15:00"

                db.collection("citas")
                    .whereEqualTo("doctorId", selectedDoctorId)
                    .whereEqualTo("fechaAgenda", fechaSeleccionadaFormato)
                    .get()
                    .addOnSuccessListener { citasSnap ->
                        val horasOcupadas = mutableListOf<String>()
                        for (cita in citasSnap) {
                            horasOcupadas.add(cita.getString("hora") ?: "")
                        }
                        generarListaHoras(inicio, fin, comidaIni, comidaFin, horasOcupadas)
                    }
            }
            .addOnFailureListener {
                mostrarSinHorarios("Error de conexión")
            }
    }

    private fun generarListaHoras(inicio: String, fin: String, comIni: String, comFin: String, ocupadas: List<String>) {
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

        if (slots.isEmpty()) {
            mostrarSinHorarios("Agenda llena hoy")
        } else {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, slots)
            spHorarios.adapter = adapter
            spHorarios.isEnabled = true
        }
    }

    private fun mostrarSinHorarios(mensaje: String) {
        val lista = listOf(mensaje)
        spHorarios.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lista)
        spHorarios.isEnabled = false
    }

    private fun timeToMinutes(time: String): Int {
        try {
            val parts = time.split(":")
            return parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) { return 0 }
    }

    private fun minutesToTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return String.format("%02d:%02d", h, m)
    }

    // --- GUARDAR CITA ---

    private fun validarYGuardarCita() {
        if (etTelefono.text.isEmpty() || etCorreo.text.isEmpty() ||
            selectedDoctorId.isEmpty() || selectedHospitalId.isEmpty() ||
            !spHorarios.isEnabled || spHorarios.selectedItem == null) {
            Toast.makeText(this, "Completa todos los datos y selecciona horario", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardar.isEnabled = false
        btnGuardar.text = "Agendando..."

        val citaMap = hashMapOf(
            "uidUsuario" to (auth.currentUser?.uid ?: ""),
            "nombrePaciente" to etNombrePaciente.text.toString(),
            "telefono" to etTelefono.text.toString(),
            "correo" to etCorreo.text.toString(),
            "fechaNacimiento" to etFechaNacimiento.text.toString(),
            "genero" to spGenero.selectedItem.toString(),
            "esAfiliado" to cbAfiliado.isChecked,
            "numeroAfiliado" to if (cbAfiliado.isChecked) etNumeroAfiliado.text.toString() else "",
            "hospitalId" to selectedHospitalId,
            "hospitalNombre" to spHospital.selectedItem.toString(),
            "doctorId" to selectedDoctorId,
            "especialista" to spDoctor.selectedItem.toString(),
            "tipoCita" to spTipoCita.selectedItem.toString(),
            "motivo" to etMotivo.text.toString(),
            "fecha" to etFechaCita.text.toString(),
            "fechaAgenda" to fechaSeleccionadaFormato,
            "hora" to spHorarios.selectedItem.toString(),
            "estado" to "pendiente"
        )

        db.collection("citas").add(citaMap)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Cita Confirmada!", Toast.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
                btnGuardar.text = "Agendar Cita"
                limpiarFormulario()
                mostrarFormulario(false)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                btnGuardar.isEnabled = true
                btnGuardar.text = "Agendar Cita"
            }
    }

    // --- DIÁLOGOS PREMIUM ---

    private fun mostrarSelectorNacimientoPremium() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_fecha_nacimiento, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val npDia = dialogView.findViewById<NumberPicker>(R.id.npDia)
        val npMes = dialogView.findViewById<NumberPicker>(R.id.npMes)
        val npAnio = dialogView.findViewById<NumberPicker>(R.id.npAnio)
        val btnAceptar = dialogView.findViewById<Button>(R.id.btnAceptarFecha)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarFecha)

        val yearActual = Calendar.getInstance().get(Calendar.YEAR)
        npAnio.minValue = 1920
        npAnio.maxValue = yearActual
        npAnio.value = 2000
        npAnio.wrapSelectorWheel = false

        val meses = arrayOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        npMes.minValue = 0
        npMes.maxValue = 11
        npMes.displayedValues = meses
        npMes.value = 0

        npDia.minValue = 1
        npDia.maxValue = 31
        npDia.value = 1

        btnAceptar.setOnClickListener {
            val dia = npDia.value
            val mes = npMes.value + 1
            val anio = npAnio.value
            etFechaNacimiento.setText(String.format("%02d/%02d/%d", dia, mes, anio))
            dialog.dismiss()
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun mostrarCalendarioCitaPremium() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_calendario_premium, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val calendarView = dialogView.findViewById<CalendarView>(R.id.calendarViewPremium)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarCal)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarCal)

        calendarView.minDate = System.currentTimeMillis() - 1000

        var anioSel = Calendar.getInstance().get(Calendar.YEAR)
        var mesSel = Calendar.getInstance().get(Calendar.MONTH)
        var diaSel = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            anioSel = year
            mesSel = month
            diaSel = dayOfMonth
        }

        btnConfirmar.setOnClickListener {
            val fechaVisual = String.format("%02d/%02d/%d", diaSel, mesSel + 1, anioSel)
            etFechaCita.setText(fechaVisual)
            fechaSeleccionadaFormato = String.format(Locale.getDefault(), "%04d-%02d-%02d", anioSel, mesSel + 1, diaSel)

            if (selectedDoctorId.isNotEmpty()) {
                cargarHorariosDisponibles()
            } else {
                Toast.makeText(this, "Fecha lista. Selecciona un doctor.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        btnCancelar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // --- UTILS ---

    private fun mostrarFormulario(mostrar: Boolean) {
        layoutDashboard.visibility = if (mostrar) View.GONE else View.VISIBLE
        layoutFormulario.visibility = if (mostrar) View.VISIBLE else View.GONE
        if (!mostrar) actualizarDashboard()
    }

    private fun limpiarFormulario() {
        etMotivo.setText("")
        etFechaCita.setText("")
        spHorarios.adapter = null
        etFechaNacimiento.setText("")
    }

    private fun abrirMapa() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivityForResult(intent, REQUEST_HOSPITAL)
    }

    private fun actualizarDashboard() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("citas")
            .whereEqualTo("uidUsuario", uid)
            .limit(1)
            .get()
            .addOnSuccessListener { res ->
                if (!res.isEmpty) {
                    val doc = res.documents[0]
                    var especialista = doc.getString("especialista") ?: "Consulta"
                    especialista = especialista.replace("null", "", ignoreCase = true).trim()

                    val fecha = doc.getString("fecha") ?: "--"
                    val hora = doc.getString("hora") ?: ""

                    tvDashEspecialista.text = "$especialista $hora"
                    tvDashFecha.text = fecha
                } else {
                    tvDashEspecialista.text = "Sin citas programadas"
                    tvDashFecha.text = "--/--"
                }
            }
    }

    // --- NAVEGACIÓN Y MENU ---

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_carnet -> startActivity(Intent(this, ExpedientePacienteActivity::class.java))
            R.id.nav_historial -> startActivity(Intent(this, HistorialCitasActivity::class.java))
            R.id.nav_tratamientos -> startActivity(Intent(this, TratamientosActivity::class.java))
            R.id.nav_inicio -> mostrarFormulario(false)
            R.id.nav_salir -> {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (layoutFormulario.visibility == View.VISIBLE) mostrarFormulario(false)
        else if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }
}