package com.example.proyect.dao

import com.example.proyect.model.Tratamiento
import com.google.firebase.firestore.FirebaseFirestore

class tratamientoDAO {

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "tratamientos"

    // --- GUARDAR TRATAMIENTO ---
    fun insertar(tratamiento: Tratamiento, alTerminar: (Boolean) -> Unit) {
        val data = hashMapOf(
            "nombreMedicamento" to tratamiento.nombreMedicamento,
            "dosis" to tratamiento.dosis,
            "frecuenciaHoras" to tratamiento.frecuenciaHoras,
            "duracionDias" to tratamiento.duracionDias,
            "fechaInicio" to tratamiento.fechaInicio
        )

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }

    // --- LISTAR TRATAMIENTOS ---
    fun listar(alTerminar: (List<Tratamiento>) -> Unit) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Tratamiento>()
                for (document in result) {
                    val t = document.toObject(Tratamiento::class.java)
                    t.id = document.id
                    lista.add(t)
                }
                alTerminar(lista)
            }
            .addOnFailureListener { alTerminar(emptyList()) }
    }

    // --- ELIMINAR (Cuando termine el tratamiento) ---
    fun eliminar(id: String, alTerminar: (Boolean) -> Unit) {
        db.collection(collectionName).document(id)
            .delete()
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }
}