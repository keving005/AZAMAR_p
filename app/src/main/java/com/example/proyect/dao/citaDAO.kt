package com.example.proyect.dao

import com.example.proyect.model.Cita
import com.google.firebase.firestore.FirebaseFirestore

class citaDAO {

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "citas"

    // --- INSERTAR ---
    fun insertar(cita: Cita, alTerminar: (Boolean) -> Unit) {
        val citaMap = hashMapOf(
            "nombrePaciente" to cita.nombrePaciente,
            "especialista" to cita.especialista,
            "fecha" to cita.fecha,
            "hora" to cita.hora,
            "tipoCita" to cita.tipoCita
        )

        db.collection(collectionName)
            .add(citaMap)
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }

    // --- ACTUALIZAR ---
    fun actualizar(cita: Cita, alTerminar: (Boolean) -> Unit) {
        if (cita.id.isEmpty()) {
            alTerminar(false)
            return
        }

        val datosActualizados = hashMapOf<String, Any>(
            "nombrePaciente" to cita.nombrePaciente,
            "especialista" to cita.especialista,
            "fecha" to cita.fecha,
            "hora" to cita.hora,
            "tipoCita" to cita.tipoCita
        )

        db.collection(collectionName).document(cita.id)
            .update(datosActualizados)
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }

    // --- ELIMINAR ---
    fun eliminar(id: String, alTerminar: (Boolean) -> Unit) {
        db.collection(collectionName).document(id)
            .delete()
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }

    // --- LISTAR ---
    fun listar(alTerminar: (List<Cita>) -> Unit) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Cita>()
                for (document in result) {
                    val cita = document.toObject(Cita::class.java)
                    cita.id = document.id
                    lista.add(cita)
                }
                alTerminar(lista)
            }
            .addOnFailureListener {
                alTerminar(emptyList())
            }
    }
}