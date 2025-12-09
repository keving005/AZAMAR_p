package com.example.proyect.dao

import com.example.proyect.model.Notificacion
import com.google.firebase.firestore.FirebaseFirestore

class notificacionDAO {

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "notificaciones" // Colección en la nube

    // --- INSERTAR ---
    fun insertar(obj: Notificacion, alTerminar: (Boolean) -> Unit) {
        val data = hashMapOf(
            "mensaje" to obj.mensaje,
            "fecha" to obj.fecha,
            "hora" to obj.hora
        )

        db.collection(collectionName)
            .add(data)
            .addOnSuccessListener { alTerminar(true) }
            .addOnFailureListener { alTerminar(false) }
    }

    // --- ACTUALIZAR ---
    fun actualizar(obj: Notificacion, alTerminar: (Boolean) -> Unit) {
        if (obj.id.isEmpty()) {
            alTerminar(false)
            return
        }

        val data = hashMapOf<String, Any>(
            "mensaje" to obj.mensaje,
            "fecha" to obj.fecha,
            "hora" to obj.hora
        )

        db.collection(collectionName).document(obj.id)
            .update(data)
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
    fun listar(alTerminar: (List<Notificacion>) -> Unit) {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                val lista = mutableListOf<Notificacion>()
                for (document in result) {
                    val noti = document.toObject(Notificacion::class.java)
                    noti.id = document.id // Guardamos el ID de Firebase
                    lista.add(noti)
                }
                alTerminar(lista)
            }
            .addOnFailureListener {
                alTerminar(emptyList())
            }
    }
}