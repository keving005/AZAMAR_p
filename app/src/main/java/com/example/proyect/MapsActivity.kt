package com.example.proyect

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.PolyUtil
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var txtNombre: TextView
    private lateinit var txtDireccion: TextView
    private lateinit var txtTelefono: TextView
    private lateinit var txtHorario: TextView
    private lateinit var txtDistancia: TextView
    private lateinit var txtTiempo: TextView
    private lateinit var infoPanel: LinearLayout
    private lateinit var btnIr: Button

    private lateinit var fusedLocation: FusedLocationProviderClient
    private var miUbicacion: LatLng? = null
    private var currentPolyline: Polyline? = null
    private var hospitalSeleccionado: Hospital? = null

    data class Hospital(
        val nombre: String,
        val lat: Double,
        val lng: Double,
        val direccion: String,
        val telefono: String,
        val horario: String
    )
        private val hospitales = listOf(
            // Hospitales privados en Chalco
            Hospital("Hospital SAME Chalco", 19.2687548, -98.8941361, "Av. Cuauhtémoc No. 15, San Miguel Jacalones, Chalco de Díaz Covarrubias, Méx.", "55-1734-0930", "24 hrs"),
    Hospital("Hospital de Maria", 19.2616433, -98.8903132, "Calle Centro, Chalco de Díaz Covarrubias, Méx.", "55-5975-5886", "Horario local"),
    Hospital("Clínica Altius Chalco", 19.2600, -98.8890, "Privada Cerrada s/n, Agostadero, 56615 Valle de Chalco Solidaridad, Méx.", "55-3091-5133", "Horario local"),
    Hospital("Clínica Santa Anita", 19.2500, -98.8660, "Avenida Alfredo del Mazo, Santa Cruz, 56617 Valle de Chalco Solidaridad, Méx.", "N/A", "Horario local"),
    Hospital("Central Médica Santa Cruz", 19.2510, -98.8665, "Calle Norte 9, Santa Cruz, 56617 Valle de Chalco Solidaridad, Méx.", "N/A", "Horario local"),
    Hospital("Sanatorio Providencia", 19.2520, -98.8670, "Calle Norte 1, Providencia, 56616 Valle de Chalco Solidaridad, Méx.", "N/A", "Horario local"),
    Hospital("Clínica Santa Ana Yareni", 19.2490, -98.8680, "Avenida Solidaridad (Tejones), El Triunfo, 56600 Chalco, Méx.", "N/A", "Horario local"),
    Hospital("Clínica Del Valle", 19.2550, -98.8655, "Avenida Emiliano Zapata, San Isidro, 56608 Chalco, Méx.", "N/A", "Horario local"),
    Hospital("Clínica Cambios Salud Integral", 19.2560, -98.8640, "Av. Moctezuma, San Miguel Xico II Sección, 56600 Chalco, Méx.", "N/A", "Horario local")
    )




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Vincular vistas
        txtNombre = findViewById(R.id.txtNombreHospital)
        txtDireccion = findViewById(R.id.txtDireccionHospital)
        txtTelefono = findViewById(R.id.txtTelefonoHospital)
        txtHorario = findViewById(R.id.txtHorarioHospital)
        txtDistancia = findViewById(R.id.txtDistancia)
        txtTiempo = findViewById(R.id.txtTiempo)
        infoPanel = findViewById(R.id.infoHospital)
        btnIr = findViewById(R.id.btnIrHospital)

        infoPanel.visibility = View.GONE

        // Inicializar cliente de ubicación
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Botón Ir a hospital
        btnIr.setOnClickListener {
            val hospital = hospitalSeleccionado
            val ubicacion = miUbicacion
            if (hospital != null && ubicacion != null) {
                trazarRuta(ubicacion, LatLng(hospital.lat, hospital.lng))
            } else {
                Toast.makeText(this, "Selecciona un hospital y asegúrate de que tu ubicación esté disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Agregar hospitales al mapa
        hospitales.forEach { hospital ->
            val marker = mMap.addMarker(
                MarkerOptions().position(LatLng(hospital.lat, hospital.lng)).title(hospital.nombre)
            )
            marker?.tag = hospital
        }

        // Centrar cámara en primer hospital
        val primerHospital = hospitales[0]
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(primerHospital.lat, primerHospital.lng), 12f))

        // Activar ubicación
        activarUbicacion()

        // Click en marcador
        mMap.setOnMarkerClickListener { marker: Marker ->
            val hospital = marker.tag as Hospital
            hospitalSeleccionado = hospital
            infoPanel.visibility = View.VISIBLE
            txtNombre.text = hospital.nombre
            txtDireccion.text = "Dirección: ${hospital.direccion}"
            txtTelefono.text = "Teléfono: ${hospital.telefono}"
            txtHorario.text = "Horario: ${hospital.horario}"
            txtDistancia.text = "Distancia: --"
            txtTiempo.text = "Tiempo: --"
            true
        }
    }

    private fun activarUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocation.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                miUbicacion = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion!!, 15f))
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun trazarRuta(origen: LatLng, destino: LatLng) {
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origen.latitude},${origen.longitude}" +
                "&destination=${destino.latitude},${destino.longitude}" +
                "&mode=driving" +
                "&key=AIzaSyDnXy-sp4qOEsmK2P4BSv3g6gkAfUhKGIg"

        val queue = Volley.newRequestQueue(this)
        val request = StringRequest(Request.Method.GET, url,
            { response -> dibujarRuta(response) },
            { Toast.makeText(this, "Error al obtener ruta", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }

    private fun dibujarRuta(json: String) {
        val jsonObj = JSONObject(json)
        val routes = jsonObj.getJSONArray("routes")
        if (routes.length() == 0) {
            Toast.makeText(this, "No se encontraron rutas", Toast.LENGTH_SHORT).show()
            return
        }

        val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
        val listaCoordenadas = PolyUtil.decode(points)

        currentPolyline?.remove()
        currentPolyline = mMap.addPolyline(
            PolylineOptions().addAll(listaCoordenadas).width(12f).color(Color.BLUE).geodesic(true)
        )

        val legs = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
        val distancia = legs.getJSONObject("distance").getString("text")
        val duracion = legs.getJSONObject("duration").getString("text")

        txtDistancia.text = "Distancia: $distancia"
        txtTiempo.text = "Tiempo: $duracion"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activarUbicacion()
        }
    }
}
