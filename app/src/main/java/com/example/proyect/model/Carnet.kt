package com.example.proyect.model
data class Carnet(
    val userID: String? = null,
    val nombreCompleto: String? = null,
    val fechaNacimiento: String? = null,
    val tipoSangre: String? = null,
    val alergias: List<String>? = null,
    val contactoEmergenciaNombre: String? = null,
    val contactoEmergenciaTelefono: String? = null,
    val condicionesCronicas: List<String>? = null,
    val medicamentosActuales: List<String>? = null,
    val fechaEmision: String? = null,
    val numeroAfiliacion: String? = null, // Número de ID de paciente o seguro
    val fechaVencimiento: String? = null // Fecha en que caduca el carnet
)