package com.example.proyect

import DB.DBHelper
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyect.model.Cita
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.HashMap
import java.util.ArrayList
import java.util.Locale

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private val REQUEST_HOSPITAL = 2001

    // CAMPOS DE CITA
    var etNombrePaciente: EditText? = null
    var etTipoCita: EditText? = null
    var spEspecialista: Spinner? = null
    var etFechaCita: EditText? = null
    var etHoraCita: EditText? = null
    var btnGuardar: Button? = null
    var btnHistorial: Button? = null
    var btnSalir: Button? = null

    // CAMPO NUEVO PARA MOSTRAR DOCTOR ELEGIDO
    var etDoctorSeleccionado: EditText? = null
    var btnBuscarEspecialista: Button? = null

    // NUEVOS CAMPOS MÉDICOS
    var spSexo: Spinner? = null
    var spTipoSangre: Spinner? = null
    var etPeso: EditText? = null
    var etAltura: EditText? = null
    var etAlergias: EditText? = null

    // 🔥 CAMPOS NUEVOS PEDIDOS
    var etFechaNacimiento: EditText? = null
    var etTelefono: EditText? = null

    // VARIABLES SELECCIONADAS (IDs de Firebase)
    var hospitalIdSeleccionado: String = ""
    var doctorIdSeleccionado: String = ""

    // Variables de ubicación/Info Hospital
    var hospitalNombre: String? = null
    var hospitalLat: Double? = null
    var hospitalLng: Double? = null
    var hospitalDireccion: String? = null
    var hospitalTelefono: String? = null
    var hospitalHorario: String? = null

    lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(this)
        setContentView(R.layout.activity_menu)

        // Configuración Menú Lateral (Drawer)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        inicializarVistasPrincipales()

        findViewById<Button>(R.id.btnSegundaPantalla).setOnClickListener {
            startActivity(Intent(this, SegundaFor::class.java))
        }
        findViewById<Button>(R.id.btnUbicacion).setOnClickListener {
            startActivityForResult(Intent(this, MapsActivity::class.java), REQUEST_HOSPITAL)
        }
    }

    private fun inicializarVistasPrincipales() {
        etNombrePaciente = findViewById(R.id.etNombrePaciente)
        etTipoCita = findViewById(R.id.etTipoCita)
        spEspecialista = findViewById(R.id.spEspecialista)
        etFechaCita = findViewById(R.id.etFechaCita)
        etHoraCita = findViewById(R.id.etHoraCita)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnSalir = findViewById(R.id.btnSalir)

        etDoctorSeleccionado = findViewById(R.id.etDoctorSeleccionado)
        btnBuscarEspecialista = findViewById(R.id.btnBuscarEspecialista)

        spSexo = findViewById(R.id.spSexo)
        spTipoSangre = findViewById(R.id.spTipoSangre)
        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        etAlergias = findViewById(R.id.etAlergias)

        // 🔥 NUEVO
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        etTelefono = findViewById(R.id.etTelefono)

        val nombreUsuario = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"
        etNombrePaciente?.setText(nombreUsuario)

        val especialistas = arrayOf("Sin asignar", "Médico General", "Pediatra", "Cardiólogo")
        spEspecialista!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, especialistas)

        val sexos = arrayOf("Femenino", "Masculino", "Otro")
        spSexo!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sexos)

        val sangre = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-","S/D")
        spTipoSangre!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sangre)

        etFechaCita!!.setOnClickListener { mostrarDatePicker() }

        // 🔥 DATEPICKER DE FECHA DE NACIMIENTO
        etFechaNacimiento!!.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                etFechaNacimiento!!.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y))
            },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        etHoraCita!!.setOnClickListener {
            if(doctorIdSeleccionado.isNotEmpty() && etFechaCita!!.text.isNotEmpty()){
                cargarHorariosDisponibles(doctorIdSeleccionado, etFechaCita!!.text.toString())
            } else {
                Toast.makeText(this, "Primero selecciona Doctor y Fecha", Toast.LENGTH_SHORT).show()
            }
        }

        btnGuardar!!.setOnClickListener { guardarCitaEnFirebase() }
        btnHistorial!!.setOnClickListener { mostrarHistorialCitas() }
        btnSalir!!.setOnClickListener { startActivity(Intent(this, MainActivity::class.java)); finish() }

        btnBuscarEspecialista?.setOnClickListener { mostrarDialogoSeleccionHospital() }
    }

    // =======================================================================================
    //   BÚSQUEDA DE HOSPITAL Y DOCTOR
    // =======================================================================================

    private var dialogoHospitales: AlertDialog? = null
    private var dialogoDoctores: AlertDialog? = null

    private fun mostrarDialogoSeleccionHospital() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_buscador, null)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloBuscador)
        tvTitulo.text = "Selecciona un Hospital"

        val recycler = dialogView.findViewById<RecyclerView>(R.id.rvBuscador)
        recycler.layoutManager = LinearLayoutManager(this)

        val listaHosp = mutableListOf<Usuario>()
        val adapterHosp = HospitalInternalAdapter(listaHosp) { hospital ->
            hospitalIdSeleccionado = hospital.uid
            hospitalNombre = hospital.nombre
            dialogoHospitales?.dismiss()
            mostrarDialogoSeleccionDoctores(hospital.uid, hospital.nombre)
        }
        recycler.adapter = adapterHosp

        db.collection("usuarios").whereEqualTo("rol", 3).get()
            .addOnSuccessListener { res ->
                listaHosp.clear()
                for (doc in res) {
                    val u = doc.toObject(Usuario::class.java)
                    u.uid = doc.id
                    listaHosp.add(u)
                }
                adapterHosp.notifyDataSetChanged()
            }

        dialogoHospitales = AlertDialog.Builder(this).setView(dialogView).create()
        dialogoHospitales?.show()
    }

    private fun mostrarDialogoSeleccionDoctores(hospitalId: String, nombreHospital: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_buscador, null)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvTituloBuscador)
        tvTitulo.text = "Selecciona Especialista en $nombreHospital"

        val recycler = dialogView.findViewById<RecyclerView>(R.id.rvBuscador)
        recycler.layoutManager = LinearLayoutManager(this)

        val listaDocs = mutableListOf<Usuario>()
        val adapterDoc = DoctorInternalAdapter(listaDocs) { doctor ->
            doctorIdSeleccionado = doctor.uid
            etDoctorSeleccionado?.setText("${doctor.nombre} (${doctor.especialidad})")
            etTipoCita?.setText("Cita en $nombreHospital")
            Toast.makeText(this, "Doctor Asignado", Toast.LENGTH_SHORT).show()
            dialogoDoctores?.dismiss()
        }
        recycler.adapter = adapterDoc

        db.collection("usuarios")
            .whereEqualTo("rol", 2)
            .whereEqualTo("hospitalId", hospitalId)
            .get()
            .addOnSuccessListener { res ->
                listaDocs.clear()
                for (doc in res) {
                    val u = doc.toObject(Usuario::class.java)
                    u.uid = doc.id
                    listaDocs.add(u)
                }
                adapterDoc.notifyDataSetChanged()
            }

        dialogoDoctores = AlertDialog.Builder(this).setView(dialogView).create()
        dialogoDoctores?.show()
    }

    // =======================================================================================
    //   HORARIOS INTELIGENTES
    // =======================================================================================

    private fun cargarHorariosDisponibles(doctorId: String, fecha: String) {
        val partes = fecha.split("/")
        val fechaFirebase = if(partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else fecha

        val docAgendaId = "${doctorId}_$fechaFirebase"

        db.collection("agenda_doctores").document(docAgendaId).get()
            .addOnSuccessListener { docSnapshot ->

                var horaInicio = "08:00"
                var horaFin = "16:00"
                var comidaInicio = "14:00"
                var comidaFin = "15:00"
                var esDiaLibre = false

                if (docSnapshot.exists()) {
                    esDiaLibre = docSnapshot.getBoolean("esDiaLibre") ?: false
                    horaInicio = docSnapshot.getString("horaInicio") ?: "08:00"
                    horaFin = docSnapshot.getString("horaFin") ?: "16:00"
                    comidaInicio = docSnapshot.getString("comidaInicio") ?: "14:00"
                    comidaFin = docSnapshot.getString("comidaFin") ?: "15:00"
                }

                if (esDiaLibre) {
                    Toast.makeText(this, "El doctor no labora este día", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                db.collection("citas")
                    .whereEqualTo("doctorId", doctorId)
                    .whereEqualTo("fecha", fecha)
                    .get()
                    .addOnSuccessListener { citasSnapshot ->
                        val horasOcupadas = ArrayList<String>()
                        for (cita in citasSnapshot) {
                            val h = cita.getString("hora")
                            if (h != null) horasOcupadas.add(h)
                        }

                        generarListaDeHoras(horaInicio, horaFin, comidaInicio, comidaFin, horasOcupadas)
                    }
            }
    }

    private fun generarListaDeHoras(inicio: String, fin: String, comIni: String, comFin: String, ocupadas: List<String>) {
        val listaDisponibles = ArrayList<String>()
        val minInicio = convertirAMinutos(inicio)
        val minFin = convertirAMinutos(fin)
        val minComidaIni = convertirAMinutos(comIni)
        val minComidaFin = convertirAMinutos(comFin)
        val duracionCita = 30

        var actual = minInicio
        while (actual + duracionCita <= minFin) {
            val finSlot = actual + duracionCita
            val chocaComida = (actual < minComidaFin && finSlot > minComidaIni)

            if (!chocaComida) {
                val horaTexto = convertirAHoraTexto(actual)
                if (!ocupadas.contains(horaTexto)) {
                    listaDisponibles.add(horaTexto)
                }
            }
            actual += duracionCita
        }
        mostrarDialogoHorarios(listaDisponibles)
    }

    private fun mostrarDialogoHorarios(horas: ArrayList<String>) {
        if (horas.isEmpty()) {
            Toast.makeText(this, "Agenda llena para este día", Toast.LENGTH_LONG).show()
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Horarios Disponibles")
        val arrayHoras = horas.toTypedArray()
        builder.setItems(arrayHoras) { _, which ->
            etHoraCita!!.setText(arrayHoras[which])
        }
        builder.show()
    }

    private fun convertirAMinutos(hora: String): Int {
        val partes = hora.split(":")
        return (partes[0].toInt() * 60) + partes[1].toInt()
    }

    private fun convertirAHoraTexto(minutos: Int): String {
        val h = minutos / 60
        val m = minutos % 60
        return String.format(Locale.getDefault(), "%02d:%02d", h, m)
    }

    // =======================================================================================
    //   GUARDAR CITA EN FIREBASE + SQLITE (CON NUEVOS CAMPOS)
    // =======================================================================================

    private fun guardarCitaEnFirebase() {
        val nombre = etNombrePaciente!!.text.toString()
        val tipo = etTipoCita!!.text.toString()

        val doctorFinal = if (etDoctorSeleccionado?.text.toString().isNotEmpty())
            etDoctorSeleccionado?.text.toString()
        else spEspecialista!!.selectedItem.toString()

        val fecha = etFechaCita!!.text.toString()
        val hora = etHoraCita!!.text.toString()

        if (fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show()
            return
        }

        val cita = hashMapOf(
            "nombrePaciente" to nombre,
            "especialista" to doctorFinal,
            "tipoCita" to tipo,
            "fecha" to fecha,
            "hora" to hora,
            "hospitalNombre" to (hospitalNombre ?: "No especificado"),
            "doctorId" to doctorIdSeleccionado,
            "hospitalId" to hospitalIdSeleccionado,
            "sexo" to spSexo!!.selectedItem.toString(),
            "tipoSangre" to spTipoSangre!!.selectedItem.toString(),
            "peso" to etPeso!!.text.toString(),
            "altura" to etAltura!!.text.toString(),
            "alergias" to etAlergias!!.text.toString(),

            // 🔥 NUEVOS CAMPOS
            "fechaNacimiento" to etFechaNacimiento!!.text.toString(),
            "telefono" to etTelefono!!.text.toString()
        )



        db.collection("citas").add(cita)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Cita Agendada!", Toast.LENGTH_LONG).show()
                etTipoCita!!.setText("")
                etFechaCita!!.setText("")
                etHoraCita!!.setText("")
                etDoctorSeleccionado!!.setText("")
            }

        dbHelper.agregarCita(
            nombre, doctorFinal, tipo, fecha, hora,
            spSexo!!.selectedItem.toString(), spTipoSangre!!.selectedItem.toString(),
            etPeso!!.text.toString(), etAltura!!.text.toString(), etAlergias!!.text.toString(),
            hospitalNombre, hospitalDireccion, hospitalTelefono, hospitalHorario
        )
    }

    private fun mostrarHistorialCitas() {
        db.collection("citas").get().addOnSuccessListener { res ->
            val lista = ArrayList<String>()
            for (doc in res) {
                try {
                    val cita = doc.toObject(Cita::class.java)
                    lista.add("Paciente: ${cita.nombrePaciente}\n${cita.especialista}\n${cita.fecha} - ${cita.hora}")
                } catch (e: Exception) {
                    lista.add("Cita ID: ${doc.id}")
                }
            }
            if (lista.isEmpty()) Toast.makeText(this, "Sin historial", Toast.LENGTH_SHORT).show()
            else mostrarListaSimple(lista)
        }
    }

    private fun mostrarListaSimple(lista: ArrayList<String>) {
        AlertDialog.Builder(this)
            .setTitle("Historial de Citas")
            .setItems(lista.toTypedArray(), null)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_carnet -> startActivity(Intent(this, CarnetActivity::class.java))
            R.id.nav_tratamientos -> mostrarMenuTratamientos()
            R.id.nav_salir -> { startActivity(Intent(this, MainActivity::class.java)); finish() }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun mostrarMenuTratamientos() {
        Toast.makeText(this, "Gestión de Tratamientos", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d -> etFechaCita?.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", d, m + 1, y)) },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_HOSPITAL && resultCode == RESULT_OK) {
            hospitalNombre = data?.getStringExtra("HOSPITAL_NOMBRE")
            etTipoCita?.setText("Hospital: $hospitalNombre")
        }
    }
}

// =======================================================================================
//   ADAPTERS
// =======================================================================================

class HospitalInternalAdapter(
    private val lista: List<com.example.proyect.Usuario>,
    private val onClick: (com.example.proyect.Usuario) -> Unit
) : RecyclerView.Adapter<HospitalInternalAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nombre: TextView = v.findViewById(R.id.tvNombreItem)
        val subtitulo: TextView = v.findViewById(R.id.tvSubtituloItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital_selector, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nombre.text = item.nombre
        holder.subtitulo.text = "Ver especialistas"
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = lista.size
}

class DoctorInternalAdapter(
    private val lista: List<com.example.proyect.Usuario>,
    private val onClick: (com.example.proyect.Usuario) -> Unit
) : RecyclerView.Adapter<DoctorInternalAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val nombre: TextView = v.findViewById(R.id.tvDocNombre)
        val especialidad: TextView = v.findViewById(R.id.tvDocEspecialidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_selector, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.nombre.text = item.nombre
        holder.especialidad.text = item.especialidad
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = lista.size
}


