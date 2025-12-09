package com.example.proyect.dao

import com.example.proyect.model.Historial
import com.google.firebase.firestore.FirebaseFirestore

class historialDAO {

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "historial" // Nombre de la colección en Firebase

    // --- INSERTAR ---
    fun insertar(obj: Historial, alTerminar: (Boolean) -> Unit) {
        val historialMap = hashMapOf(
            "nombrePaciente" to obj.nombrePaciente,
            "especialista" to obj.especialista,
            "tipoCita" to obj.tipoCita
        )

        db.collection(collectionName)
            .add(historialMap)
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }

    // --- ACTUALIZAR ---
    fun actualizar(obj: Historial, alTerminar: (Boolean) -> Unit) {
        if (obj.id.isEmpty()) {
            alTerminar(false)
            return
        }

        val datosActualizados = hashMapOf<String, Any>(
            "nombrePaciente" to obj.nombrePaciente,
            "especialista" to obj.especialista,
            "tipoCita" to obj.tipoCita
        )

        db.collection(collectionName).document(obj.id)
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
    fun listar(alTerminar: (List<Historial>) -> Unit) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Historial>()
                for (document in result) {
                    val historial = document.toObject(Historial::class.java)
                    historial.id = document.id
                    lista.add(historial)
                }
                alTerminar(lista)
            }
            .addOnFailureListener {
                alTerminar(emptyList())
            }
    }
}