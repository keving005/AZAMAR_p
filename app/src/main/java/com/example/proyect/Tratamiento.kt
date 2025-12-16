package com.example.proyect

data class Tratamiento(
    var id: String = "",
    val uidUsuario: String = "", // Para saber de quién es
    val nombreMedicamento: String = "",
    val dosis: String = "",
    val frecuenciaHoras: String = "",
    val duracionDias: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)