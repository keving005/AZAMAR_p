package com.example.proyect.model

data class Cita(
    // CAMBIO OBLIGATORIO:
    // 1. 'var' para que se pueda modificar.
    // 2. 'String' para aceptar el ID de Firebase.
    var id: String = "",
    var nombrePaciente: String = "",
    var especialista: String = "",
    var fecha: String = "",
    var hora: String = "",
    var tipoCita: String = ""
)