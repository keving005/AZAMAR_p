package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.proyect.dao.carnetDAO
import com.example.proyect.model.Carnet
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 💡 IMPLEMENTACIÓN CRÍTICA: Implementa la interfaz para manejar los clics del menú
class CarnetActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // ➡️ VARIABLES DEL MENÚ DESPLEGABLE
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    // DAOs y Auth
    private val auth = FirebaseAuth.getInstance()
    private val carnetDAO = carnetDAO()

    // Referencias a los TextViews del layout (Cuerpo del Carnet)
    private lateinit var tvNombre: TextView
    private lateinit var tvTipoSangre: TextView
    private lateinit var tvFechaNacimiento: TextView
    private lateinit var tvAfiliacion: TextView
    private lateinit var tvVencimiento: TextView
    private lateinit var tvAlergias: TextView
    private lateinit var tvCondiciones: TextView
    private lateinit var tvContactoEmergencia: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carnet)

        // --- 1. CONFIGURACIÓN DEL MENÚ DESPLEGABLE ---
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)

        // Reemplazamos el supportActionBar simple por el toolbar para el Drawer
        setSupportActionBar(toolbar)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // El título de la Toolbar se establece aquí
        supportActionBar?.title = "Carnet Médico Digital"
        // ----------------------------------------------------

        // 2. Inicializar vistas (Cuerpo del Carnet)
        tvNombre = findViewById(R.id.tv_nombre)
        tvTipoSangre = findViewById(R.id.tv_tipo_sangre)
        tvFechaNacimiento = findViewById(R.id.tv_fecha_nacimiento)
        tvAfiliacion = findViewById(R.id.tv_afiliacion)
        tvVencimiento = findViewById(R.id.tv_vencimiento)
        tvAlergias = findViewById(R.id.tv_alergias)
        tvCondiciones = findViewById(R.id.tv_condiciones)
        tvContactoEmergencia = findViewById(R.id.tv_contacto_emergencia)

        // 3. Cargar los datos del carnet
        cargarDatosCarnet()
    }

    // --- LÓGICA DE NAVEGACIÓN DEL MENÚ ---
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            // ✅ NAVEGACIÓN PARA REGRESAR AL MENÚ PRINCIPAL
            R.id.nav_inicio -> {
                // Usamos flags para asegurar que MenuActivity sea la cima y cerrar esta
                val intent = Intent(this, MenuActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            R.id.nav_carnet -> {
                Toast.makeText(this, "Ya estás en tu Carnet Médico", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_tratamientos -> {
                Toast.makeText(this, "Sección de Tratamientos en desarrollo", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_salir -> {
                // Cerrar sesión y regresar a la pantalla de login (MainActivity)
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Cerrar el menú desplegable al presionar 'Atrás'
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    // ----------------------------------------------------


    // --- FUNCIONES EXISTENTES DE CARGA DE DATOS ---
    private fun cargarDatosCarnet() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val carnet = carnetDAO.obtenerCarnetPorID(userId)

                    if (carnet != null) {
                        mostrarDatosEnVistas(carnet as Carnet)
                    } else {
                        Toast.makeText(this@CarnetActivity, "Carnet no encontrado. Por favor, regístrelo.", Toast.LENGTH_LONG).show()
                        limpiarVistas()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CarnetActivity, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
                    limpiarVistas()
                }
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado. Inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun mostrarDatosEnVistas(carnet: Carnet) {
        tvNombre.text = carnet.nombreCompleto ?: "N/D"
        tvTipoSangre.text = carnet.tipoSangre ?: "N/D"
        tvFechaNacimiento.text = carnet.fechaNacimiento ?: "N/D"

        tvAfiliacion.text = carnet.numeroAfiliacion ?: "N/D"
        tvVencimiento.text = carnet.fechaVencimiento ?: "N/D"

        tvAlergias.text = carnet.alergias?.joinToString(", ") ?: "Ninguna conocida"
        tvCondiciones.text = carnet.condicionesCronicas?.joinToString(", ") ?: "Ninguna"

        val contacto = carnet.contactoEmergenciaNombre?.let { nombre ->
            "${nombre} (${carnet.contactoEmergenciaTelefono ?: "N/D"})"
        } ?: "N/D"
        tvContactoEmergencia.text = contacto
    }

    private fun limpiarVistas() {
        tvNombre.text = "SIN DATOS"
        tvTipoSangre.text = "N/D"
        tvFechaNacimiento.text = "N/D"
        tvAfiliacion.text = "N/D"
        tvVencimiento.text = "N/D"
        tvAlergias.text = "Sin datos"
        tvCondiciones.text = "Sin datos"
        tvContactoEmergencia.text = "Sin contacto"
    }

    // Función simple de regreso (solo necesaria si no usáramos el Drawer, pero se mantiene)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}