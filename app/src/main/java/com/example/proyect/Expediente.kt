package com.example.proyect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Expediente(
    var id: String = "",
    val pacienteId: String = "",
    val pacienteNombre: String = "",

    // --- LO QUE YA TENÍAS (BIOMÉTRICOS) ---
    val tipoSangre: String = "",
    val peso: String = "",
    val altura: String = "",

    // --- LO NUEVO (BOOLEANOS CLAVE) ---
    val esHipertenso: Boolean = false,
    val tieneDiabetes: Boolean = false,

    // --- LO QUE YA TENÍAS (ANTECEDENTES) ---
    val alergias: String = "",
    val cirugiasPrevias: String = "",
    val antecedentesHereditarios: String = "",
    val padecimientosCronicos: String = "", // Recuperado: Para otras enfermedades (ej. Asma)

    // --- LO NUEVO (ESTADO Y EMERGENCIA) ---
    val padecimientoActual: String = "",
    val contactoEmergenciaNombre: String = "",
    val contactoEmergenciaTelefono: String = "",

    // --- CONTROL ---
    val ultimoHospital: String = "",
    val ultimaActualizacion: String = ""
) : Parcelable