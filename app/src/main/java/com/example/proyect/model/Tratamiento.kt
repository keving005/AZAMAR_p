package com.example.proyect.model

data class Tratamiento(
    var id: String = "",
    var nombreMedicamento: String = "", // Ej: "Ibuprofeno"
    var dosis: String = "",             // Ej: "500mg" o "1 pastilla"
    var frecuenciaHoras: Int = 0,       // Ej: 8 (cada 8 horas)
    var duracionDias: Int = 0,          // Ej: 5 (durante 5 días)
    var fechaInicio: String = ""        // Para saber cuándo empezó
)