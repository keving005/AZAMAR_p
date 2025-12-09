package com.example.proyect.model

data class Historial(
    var id: String = "", // ID de texto para Firebase
    var nombrePaciente: String = "",
    var especialista: String = "",
    var tipoCita: String = ""
)