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

    // CAMPOS NORMALES
    var etNombrePaciente: EditText? = null
    var etTipoCita: EditText? = null
    var spEspecialista: Spinner? = null
    var etFechaCita: EditText? = null
    var etHoraCita: EditText? = null
    var btnGuardar: Button? = null
    var btnHistorial: Button? = null
    var btnSalir: Button? = null
    var etTelefonoPaciente: EditText? = null

    // NUEVOS CAMPOS
    var spSexo: Spinner? = null
    var spTipoSangre: Spinner? = null
    var etPeso: EditText? = null
    var etAltura: EditText? = null
    var etAlergias: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

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

        val btnIrSegundaPantalla: Button = findViewById(R.id.btnSegundaPantalla)

        btnIrSegundaPantalla.setOnClickListener {
            val intent = Intent(this, SegundaFor::class.java)
            startActivity(intent)
        }
    }

    private fun inicializarVistasPrincipales() {

        // CAMPOS ORIGINALES
        etNombrePaciente = findViewById(R.id.etNombrePaciente)
        etTipoCita = findViewById(R.id.etTipoCita)
        spEspecialista = findViewById(R.id.spEspecialista)
        etFechaCita = findViewById(R.id.etFechaCita)
        etHoraCita = findViewById(R.id.etHoraCita)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnHistorial = findViewById(R.id.btnHistorial)
        btnSalir = findViewById(R.id.btnSalir)

        // 🚀 NUEVOS CAMPOS
        spSexo = findViewById(R.id.spSexo)
        spTipoSangre = findViewById(R.id.spTipoSangre)
        etPeso = findViewById(R.id.etPeso)
        etAltura = findViewById(R.id.etAltura)
        etAlergias = findViewById(R.id.etAlergias)

        // Usuario
        val nombreUsuario = intent.getStringExtra("NOMBRE_USUARIO") ?: "Usuario"
        etNombrePaciente?.setText(nombreUsuario)

        // Spinner de Especialistas
        val especialistas = arrayOf("Médico General", "Pediatra", "Cardiólogo", "Dentista", "Ginecólogo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, especialistas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spEspecialista!!.adapter = adapter

        // Spinner de Sexo
        val sexos = arrayOf("Femenino", "Masculino", "Otro")
        spSexo!!.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sexos)

        // Spinner de Tipo de Sangre
        val sangre = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        spTipoSangre!!.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, sangre)

        etFechaCita!!.setOnClickListener { mostrarDatePicker() }
        etHoraCita!!.setOnClickListener { mostrarTimePicker() }
        btnGuardar!!.setOnClickListener { guardarCitaEnFirebase() }
        btnHistorial!!.setOnClickListener { mostrarHistorialCitas() }

        btnSalir!!.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_tratamientos -> {
            Toast.makeText(this, "Sección en desarrollo", Toast.LENGTH_SHORT).show()
        }
            R.id.nav_inicio -> Toast.makeText(this, "Ya estás en Inicio", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

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
            Toast.makeText(this, "Faltan datos", Toast.LENGTH_SHORT).show()
            return
        }


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
            "alergias" to alergias
        )

        db.collection("citas").add(cita)
            .addOnSuccessListener {
                Toast.makeText(this, "Cita Agendada", Toast.LENGTH_SHORT).show()

                etTipoCita!!.setText("")
                etFechaCita!!.setText("")
                etHoraCita!!.setText("")
                etPeso!!.setText("")
                etAltura!!.setText("")
                etAlergias!!.setText("")
            }
    }

    private fun mostrarHistorialCitas() {
        db.collection("citas").get().addOnSuccessListener { res ->
            val lista = ArrayList<String>()
            for (doc in res) {
                val cita = doc.toObject(Cita::class.java)
                lista.add(
                    "Paciente: ${cita.nombrePaciente}\n${cita.especialista}\n${cita.fecha} - ${cita.hora}"
                )
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
