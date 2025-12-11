package com.example.proyect

import DB.DBHelper
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var dbHelper: DBHelper

    // 🔹 ROL DEL USUARIO (Fijo como pediste)
    private val rolUsuario = 2

    // UI
    lateinit var etNombrePaciente: EditText
    lateinit var etFechaNacimiento: EditText
    lateinit var etTelefono: EditText
    lateinit var etCorreo: EditText
    lateinit var spHospital: Spinner
    lateinit var spDoctor: Spinner
    lateinit var spHorario: Spinner
    lateinit var spSexo: Spinner
    lateinit var spTipoSangre: Spinner
    lateinit var etPeso: EditText
    lateinit var etAltura: EditText
    lateinit var etAlergias: EditText
    lateinit var btnGuardar: Button
    lateinit var btnHistorial: Button
    lateinit var btnSalir: Button
    lateinit var btnUbicacion: Button
    lateinit var rbNotiSi: RadioButton
    lateinit var rbNotiNo: RadioButton

    // 🔹 LISTAS DINÁMICAS
    private val listaHospitales = ArrayList<String>()
    private val listaDoctores = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        dbHelper = DBHelper(this)
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

        inicializarVistas()
        cargarHospitalesDesdeFirestore()
        configurarBotones()
    }

    // --------------------------------------------------------------------
    // 🔹 INICIALIZAR VISTAS
    // --------------------------------------------------------------------
    private fun inicializarVistas() {
        etNombrePaciente = findViewById(R.id.etNombrePaciente)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        etTelefono = findViewById(R.id.etTelefono)
        etCorreo = findViewById(R.id.etCorreo)
        spHospital = findViewById(R.id.spHospital)
        spDoctor = findViewById(R.id.spDoctor)
        spHorario = findViewById(R.id.spHorario)
        spSexo = findViewById(R.id.spSexo)
        spTipoSangre = findViewById(R.id.spTipoSangre)
        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        etAlergias = findViewById(R.id.etAlergias)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnSalir = findViewById(R.id.btnSalir)
        btnUbicacion = findViewById(R.id.btnUbicacion)
        rbNotiSi = findViewById(R.id.rbNotiSi)
        rbNotiNo = findViewById(R.id.rbNotiNo)

        spSexo.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            arrayOf("Femenino", "Masculino", "Otro"))

        spTipoSangre.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"))
    }

    // --------------------------------------------------------------------
    // 🔹 CARGAR HOSPITALES DESDE FIRESTORE (DEL ROL 2)
    // --------------------------------------------------------------------
    private fun cargarHospitalesDesdeFirestore() {
        db.collection("usuarios")
            .whereEqualTo("rol", 2)
            .whereEqualTo("tipo", "hospital")
            .get()
            .addOnSuccessListener { docs ->
                listaHospitales.clear()

                for (doc in docs) {
                    listaHospitales.add(doc.getString("nombre") ?: "Sin nombre")
                }

                spHospital.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaHospitales
                )

                spHospital.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                    ) {
                        val hospitalSeleccionado = listaHospitales[position]
                        cargarDoctoresDesdeFirestore(hospitalSeleccionado)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
    }

    // --------------------------------------------------------------------
    // 🔹 CARGAR DOCTORES DESDE FIRESTORE (DEL ROL 2)
    // --------------------------------------------------------------------
    private fun cargarDoctoresDesdeFirestore(hospital: String) {
        db.collection("usuarios")
            .whereEqualTo("rol", 2)
            .whereEqualTo("tipo", "doctor")
            .whereEqualTo("hospital", hospital)
            .get()
            .addOnSuccessListener { docs ->
                listaDoctores.clear()

                for (doc in docs) {
                    listaDoctores.add(doc.getString("nombre") ?: "Sin nombre")
                }

                spDoctor.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    listaDoctores
                )
            }
    }

    // --------------------------------------------------------------------
    // 🔹 BOTONES
    // --------------------------------------------------------------------
    private fun configurarBotones() {

        etFechaNacimiento.setOnClickListener { mostrarCalendarioNacimiento() }

        btnGuardar.setOnClickListener {
            Toast.makeText(this, "Cita guardada exitosamente", Toast.LENGTH_SHORT).show()
        }

        btnSalir.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun mostrarCalendarioNacimiento() {
        val calendario = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                etFechaNacimiento.setText(String.format("%02d/%02d/%d", day, month + 1, year))
                etFechaNacimiento.setTextColor(Color.parseColor("#6C3483"))
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
