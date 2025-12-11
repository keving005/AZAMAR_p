package com.example.proyect
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.proyect.DB.DBHelper
import com.example.proyect.model.Cita
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.HashMap
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

    // NUEVOS CAMPOS (Que agregaron tus amigos)
    var spSexo: Spinner? = null
    var spTipoSangre: Spinner? = null
    var etPeso: EditText? = null
    var etAltura: EditText? = null
    var etAlergias: EditText? = null

    // VARIABLES PARA GUARDAR LA UBICACIÓN SELECCIONADA
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

        // Configuración del Menú Lateral
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        inicializarVistasPrincipales()

        // Botones extra de navegación
        val btnIrSegundaPantalla: Button = findViewById(R.id.btnSegundaPantalla)
        btnIrSegundaPantalla.setOnClickListener {
            val intent = Intent(this, SegundaFor::class.java)
            startActivity(intent)
        }

        val btnUbicacion = findViewById<Button>(R.id.btnUbicacion)
        btnUbicacion.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivityForResult(intent, REQUEST_HOSPITAL)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_HOSPITAL && resultCode == RESULT_OK) {
            val nombre = data?.getStringExtra("HOSPITAL_NOMBRE")
            val lat = data?.getDoubleExtra("HOSPITAL_LAT", 0.0)
            val lng = data?.getDoubleExtra("HOSPITAL_LNG", 0.0)
            val direccion = data?.getStringExtra("HOSPITAL_DIRECCION")
            val telefono = data?.getStringExtra("HOSPITAL_TELEFONO")
            val horario = data?.getStringExtra("HOSPITAL_HORARIO")

            // Guardamos en variables globales
            hospitalNombre = nombre
            hospitalLat = lat
            hospitalLng = lng
            hospitalDireccion = direccion
            hospitalTelefono = telefono
            hospitalHorario = horario

            // Mostrar en EditText
            etTipoCita?.setText("Hospital: $hospitalNombre")

            Toast.makeText(this, "Elegiste: $hospitalNombre", Toast.LENGTH_LONG).show()
        }
    }

    private fun inicializarVistasPrincipales() {
        // Inicializar Vistas
        etNombrePaciente = findViewById(R.id.etNombrePaciente)
        etTipoCita = findViewById(R.id.etTipoCita)
        spEspecialista = findViewById(R.id.spEspecialista)
        etFechaCita = findViewById(R.id.etFechaCita)
        etHoraCita = findViewById(R.id.etHoraCita)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnSalir = findViewById(R.id.btnSalir)

        // Inicializar Campos Nuevos
        spSexo = findViewById(R.id.spSexo)
        spTipoSangre = findViewById(R.id.spTipoSangre)
        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        etAlergias = findViewById(R.id.etAlergias)

        // Recuperar usuario
        val nombreUsuario = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"
        etNombrePaciente?.setText(nombreUsuario)

        // Configurar Spinners
        val especialistas = arrayOf("Médico General", "Pediatra", "Cardiólogo", "Dentista", "Ginecólogo")
        spEspecialista!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, especialistas)

        val sexos = arrayOf("Femenino", "Masculino", "Otro")
        spSexo!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sexos)

        val sangre = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        spTipoSangre!!.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sangre)

        // Listeners
        etFechaCita!!.setOnClickListener { mostrarDatePicker() }
        etHoraCita!!.setOnClickListener { mostrarTimePicker() }
        btnGuardar!!.setOnClickListener { guardarCitaEnFirebase() }
        btnHistorial!!.setOnClickListener { mostrarHistorialCitas() }

        btnSalir!!.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // --- AQUÍ ESTABA EL CAMBIO QUE HICIERON TUS AMIGOS ---
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_carnet -> {
                val intent = Intent(this, CarnetActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_tratamientos -> {
                mostrarMenuTratamientos()
            }
            R.id.nav_inicio -> Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
            R.id.nav_salir -> {
                // Opcional: Cerrar sesión
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // =========================================================================
    //   SECCIÓN DE TRATAMIENTOS (RESTAURADA)
    // =========================================================================

    // 1. DASHBOARD DE TRATAMIENTOS
    private fun mostrarMenuTratamientos() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_menu_tratamientos, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnVer = dialogView.findViewById<Button>(R.id.btnVerLista)
        val btnAgregar = dialogView.findViewById<Button>(R.id.btnAgregarNuevo)
        val btnCancelar = dialogView.findViewById<TextView>(R.id.btnCancelarMenu)

        btnVer.setOnClickListener {
            dialog.dismiss()
            cargarTratamientosDeFirebase()
        }

        btnAgregar.setOnClickListener {
            dialog.dismiss()
            mostrarDialogoAgregarTratamiento()
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // 2. FORMULARIO PARA AGREGAR
    private fun mostrarDialogoAgregarTratamiento() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_tratamiento, null)

        val etMed = dialogView.findViewById<EditText>(R.id.etDialogMedicamento)
        val etDosis = dialogView.findViewById<EditText>(R.id.etDialogDosis)
        val etFrecuencia = dialogView.findViewById<EditText>(R.id.etDialogFrecuencia)
        val etDuracion = dialogView.findViewById<EditText>(R.id.etDialogDuracion)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(null, null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        val botonGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        botonGuardar.text = "GUARDAR TRATAMIENTO"
        botonGuardar.setTextColor(resources.getColor(android.R.color.holo_purple))

        botonGuardar.setOnClickListener {
            val nombre = etMed.text.toString()
            val dosis = etDosis.text.toString()
            val horasStr = etFrecuencia.text.toString()
            val duracion = etDuracion.text.toString()

            if (nombre.isNotEmpty() && horasStr.isNotEmpty()) {
                guardarTratamientoFirebase(nombre, dosis, horasStr, duracion)
                dialog.dismiss()
            } else {
                if (nombre.isEmpty()) etMed.error = "Requerido"
                if (horasStr.isEmpty()) etFrecuencia.error = "Requerido"
                Toast.makeText(this, "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 3. GUARDAR TRATAMIENTO Y ALARMA
    private fun guardarTratamientoFirebase(nombre: String, dosis: String, horas: String, duracion: String) {
        val tratamiento = hashMapOf(
            "medicamento" to nombre,
            "dosis" to dosis,
            "frecuencia_horas" to horas,
            "duracion" to duracion
        )

        db.collection("tratamientos")
            .add(tratamiento)
            .addOnSuccessListener {
                Toast.makeText(this, "Tratamiento guardado", Toast.LENGTH_SHORT).show()
                programarAlarma(nombre, dosis, horas.toInt())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun programarAlarma(nombre: String, dosis: String, horas: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, RecordatorioReceiver::class.java).apply {
            putExtra("MEDICAMENTO", nombre)
            putExtra("DOSIS", dosis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            nombre.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervaloMillis = horas * 60 * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + intervaloMillis

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            intervaloMillis,
            pendingIntent
        )

        Toast.makeText(this, "¡Recordatorio activado cada $horas horas!", Toast.LENGTH_LONG).show()
    }

    // 4. VER TRATAMIENTOS (CARGA Y LISTA PREMIUM)
    private fun cargarTratamientosDeFirebase() {
        db.collection("tratamientos").get()
            .addOnSuccessListener { result ->
                val listaDatos = ArrayList<HashMap<String, String>>()

                for (doc in result) {
                    val mapa = hashMapOf(
                        "medicamento" to (doc.getString("medicamento") ?: "Sin nombre"),
                        "dosis" to (doc.getString("dosis") ?: "--"),
                        "frecuencia" to (doc.getString("frecuencia_horas") ?: "0"),
                        "duracion" to (doc.getString("duracion") ?: "--")
                    )
                    listaDatos.add(mapa)
                }

                if (listaDatos.isEmpty()) {
                    Toast.makeText(this, "No tienes tratamientos activos", Toast.LENGTH_SHORT).show()
                } else {
                    mostrarDialogoListaPremium(listaDatos)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoListaPremium(listaDatos: ArrayList<HashMap<String, String>>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_lista_tratamientos, null)
        val contenedorItems = dialogView.findViewById<LinearLayout>(R.id.contenedorTratamientos)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarLista)

        for (dato in listaDatos) {
            val itemView = layoutInflater.inflate(R.layout.item_tratamiento, null)

            val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreMed)
            val tvDosis = itemView.findViewById<TextView>(R.id.tvDosisMed)
            val tvFrecuencia = itemView.findViewById<TextView>(R.id.tvFrecuenciaMed)

            tvNombre.text = dato["medicamento"]
            tvDosis.text = "Dosis: ${dato["dosis"]}"
            tvFrecuencia.text = "Cada ${dato["frecuencia"]} horas (${dato["duracion"]})"

            contenedorItems.addView(itemView)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    // =========================================================================
    //   SECCIÓN DE CITAS (LÓGICA ACTUALIZADA CON SQLITE)
    // =========================================================================

    private fun guardarCitaEnFirebase() {
        val nombre = etNombrePaciente!!.text.toString()
        val tipo = etTipoCita!!.text.toString()
        val esp = spEspecialista!!.selectedItem.toString()
        val fecha = etFechaCita!!.text.toString()
        val hora = etHoraCita!!.text.toString()
        val sexo = spSexo!!.selectedItem.toString()
        val sangre = spTipoSangre!!.selectedItem.toString()
        val peso = etPeso!!.text.toString()
        val altura = etAltura!!.text.toString()
        val alergias = etAlergias!!.text.toString()

        if (tipo.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Guardar en Firebase
        val cita = hashMapOf(
            "nombrePaciente" to nombre,
            "especialista" to esp,
            "tipoCita" to tipo,
            "fecha" to fecha,
            "hora" to hora,
            "sexo" to sexo,
            "tipoSangre" to sangre,
            "peso" to peso,
            "altura" to altura,
            "alergias" to alergias,
            "hospitalNombre" to hospitalNombre,
            "hospitalDireccion" to hospitalDireccion,
            "hospitalTelefono" to hospitalTelefono,
            "hospitalHorario" to hospitalHorario
        )

        db.collection("citas").add(cita)
            .addOnSuccessListener {
                Toast.makeText(this, "Cita Agendada en Firebase", Toast.LENGTH_SHORT).show()
            }

        // 2. Guardar en SQLite (Base de datos local)
        val exito = dbHelper.agregarCita(
            nombre, esp, tipo, fecha, hora,
            sexo, sangre, peso, altura, alergias,
            hospitalNombre, hospitalDireccion, hospitalTelefono, hospitalHorario
        )
        if (exito) Toast.makeText(this, "Cita guardada en SQLite también", Toast.LENGTH_SHORT).show()

        // Limpiar campos
        etTipoCita!!.setText("")
        etFechaCita!!.setText("")
        etHoraCita!!.setText("")
        etPeso!!.setText("")
        etAltura!!.setText("")
        etAlergias!!.setText("")
    }

    private fun mostrarHistorialCitas() {
        db.collection("citas").get().addOnSuccessListener { res ->
            val lista = ArrayList<String>()
            for (doc in res) {
                val cita = doc.toObject(Cita::class.java)
                lista.add("Paciente: ${cita.nombrePaciente}\n${cita.especialista}\n${cita.fecha} - ${cita.hora}")
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

    // --- UTILIDADES ---
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

    private fun mostrarDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                etFechaCita?.setText(String.format("%02d/%02d/%d", d, m + 1, y))
            },
            c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun mostrarTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m ->
                etHoraCita?.setText(String.format("%02d:%02d", h, m))
            },
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
        ).show()
    }
}