package com.example.proyect

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Medicamento(
    var id: String = "",
    val nombre: String = "",
    val descripcion: String = "",      // Para detalles extra
    val gramaje: String = "",          // Ej. "500mg", "10ml"
    val patente: String = "",          // Ej. "Pfizer", "Genérico"
    val ingredientes: String = "",     // Ej. "Paracetamol, Excipiente cbp"
    val precio: Double = 0.0,
    val stock: Int = 0,
    val fotoUrl: String = "",
    val hospitalId: String = "",

    // CAMPO NUEVO (No se guarda en BD, solo sirve para la app mientras seleccionas)
    var cantidadSeleccionada: Int = 0
) : Parcelable