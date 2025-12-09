package com.example.proyect.dao

import android.util.Log
import com.example.proyect.model.Carnet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class carnetDAO {

        private val db = FirebaseFirestore.getInstance()
        private val coleccionCarnets = db.collection("carnets_medicos")
        private val TAG = "CarnetDAO"

        suspend fun guardarCarnet(carnet: Carnet) {
            // Usamos el userID como ID del documento en la colección
            carnet.userID?.let { uid ->
                try {
                    coleccionCarnets.document(uid).set(carnet).await()
                    Log.d(TAG, "Carnet guardado exitosamente para UID: $uid")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al guardar el carnet para UID: $uid", e)
                    throw e // Relanzar la excepción para manejo en la Activity
                }
            } ?: run {
                Log.e(TAG, "El userID es nulo, no se puede guardar el carnet.")
                throw IllegalArgumentException("UserID no puede ser nulo para guardar el carnet.")
            }
        }
        suspend fun obtenerCarnetPorID(userID: String): Carnet? {
            return try {
                val documentSnapshot = coleccionCarnets.document(userID)
                    .get()
                    .await() // Espera el resultado de la operación GET

                if (documentSnapshot.exists()) {
                    // Mapea el documento a la clase de datos Carnet
                    documentSnapshot.toObject(Carnet::class.java)
                } else {
                    Log.d(TAG, "No se encontró documento de carnet para UID: $userID")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener carnet por ID: $userID", e)
                null
            }
        }

        suspend fun eliminarCarnet(userID: String) {
            try {
                coleccionCarnets.document(userID).delete().await()
                Log.d(TAG, "Carnet eliminado exitosamente para UID: $userID")
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar el carnet para UID: $userID", e)
                throw e
            }
        }
    }