package com.example.proyect

data class Usuario(
    var uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val rol: Int = 0,
    var esAfiliado: Boolean = false,
    var numeroAfiliacion: String = "",
    // AGREGAMOS ESTOS DOS:
    val especialidad: String = "",
    val hospitalId: String = ""
)