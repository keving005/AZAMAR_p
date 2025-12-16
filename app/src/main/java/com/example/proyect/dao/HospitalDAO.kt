package com.example.proyect.dao

import com.example.proyect.HospitalDBHelper
import com.example.proyect.MapsActivity

class HospitalDAO(private val dbHelper: HospitalDBHelper) {

    fun obtenerHospitales(): List<MapsActivity.Hospital> {
        val lista = mutableListOf<MapsActivity.Hospital>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM hospital", null)

        while (cursor.moveToNext()) {
            lista.add(
                MapsActivity.Hospital(
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    lat = cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                    lng = cursor.getDouble(cursor.getColumnIndexOrThrow("lng")),
                    direccion = cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                    horario = cursor.getString(cursor.getColumnIndexOrThrow("horario"))
                )
            )
        }

        cursor.close()
        db.close()
        return lista
    }
}
