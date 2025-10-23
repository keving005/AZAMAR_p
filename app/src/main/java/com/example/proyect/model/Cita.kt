package com.example.appproy.model

data class Cita (
    val id : Int? = null,
    val nombrePaciente : String,
    val especialista : String,
    val fecha : String,
    val hora : String,
    val tipoCita : String
)