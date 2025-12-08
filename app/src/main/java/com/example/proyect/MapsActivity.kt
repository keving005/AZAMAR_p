package com.example.proyect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.proyect.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    // PASO 1: Lista de hospitales estáticos
    data class Hospital(
        val nombre: String,
        val lat: Double,
        val lng: Double,
        val direccion: String,
        val telefono: String,
        val horario: String
    )

    private val hospitales = listOf(
        Hospital(
            "Hospital Civil",
            20.666, -103.333,
            "Av. Independencia #123",
            "33-1234-5678",
            "24 horas"
        ),
        Hospital(
            "IMSS 45",
            20.671, -103.345,
            "Av. Vallarta #2000",
            "33-9876-5432",
            "Lunes a viernes"
        )
        // Puedes agregar más hospitales aquí
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        hospitales.forEach { hospital ->
            val marcador = mMap.addMarker(
                MarkerOptions().position(LatLng(hospital.lat, hospital.lng)).title(hospital.nombre)
            )
            marcador?.tag = hospital
        }

        val primerHospital = hospitales[0]
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(primerHospital.lat, primerHospital.lng), 12f))

        mMap.setOnMarkerClickListener { marcador ->
            val hospitalSeleccionado = marcador.tag as Hospital
            val intent = intent
            intent.putExtra("HOSPITAL_NOMBRE", hospitalSeleccionado.nombre)
            intent.putExtra("HOSPITAL_LAT", hospitalSeleccionado.lat)
            intent.putExtra("HOSPITAL_LNG", hospitalSeleccionado.lng)
            intent.putExtra("HOSPITAL_DIRECCION", hospitalSeleccionado.direccion)
            intent.putExtra("HOSPITAL_TELEFONO", hospitalSeleccionado.telefono)
            intent.putExtra("HOSPITAL_HORARIO", hospitalSeleccionado.horario)
            setResult(RESULT_OK, intent)
            finish()
            true
        }
    }
}