package com.example.proyect

import java.io.Serializable

data class Receta(
    var id: String = "",
    val doctorId: String = "",
    val nombreDoctor: String = "",
    val fecha: String = "",
    val instrucciones: String = "",
    val estado: String = "", // "ACTIVA", "SURTIDA"
    // Los medicamentos vienen como un ArrayList de Mapas desde Firebase
    val medicamentos: ArrayList<Map<String, Any>> = ArrayList()
) : Serializable