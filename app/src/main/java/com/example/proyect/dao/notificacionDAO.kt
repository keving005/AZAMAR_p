package com.example.appproy.dao

import android.content.ContentValues
import com.example.proyect.db.CitaDBHelper
import com.example.appproy.model.Notificacion

class notificacionDAO (private val dbHelper: CitaDBHelper) : Icrud<Notificacion, Int>{

    companion object{
        private const val TABLE_NAME = "notificacion"
        private const val COLUMN_ID = "id"
        private const val COLUMN_MENSAJE = "mensaje"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
    }

    override fun insertar(notificacion: Notificacion): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombrePaciente", notificacion.id)
            put("Mensaje", notificacion.mensaje)
            put("Hora de la cita", notificacion.hora)
            put("Dia de la cita", notificacion.fecha)
        }
        return db.insert("NOTIFICACION", null, values)
    }

    // --- ACTUALIZAR ---
    override fun actualizar(notificacion: Notificacion): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // Aquí puedes actualizar solo los campos que cambian o todos
            put("nombrePaciente", notificacion.id)
            put("", notificacion.mensaje)
            put("se actualizo ", notificacion.fecha)
            put("", notificacion.hora)
        }
        val whereClause = "id = ?"
        val whereArgs = arrayOf(notificacion.id.toString())

        return db.update("NOTIFICACION", values, whereClause, whereArgs)
    }

    override fun eliminar(id: Int): Int {
        val db = dbHelper.writableDatabase
        val whereClause = "id = ?"
        // Convierte el Int a String para el array de argumentos
        val whereArgs = arrayOf(id.toString())

        // Elimina la fila donde 'id' coincide
        return db.delete("NOTIFICACION", whereClause, whereArgs)
    }

    // --- CONSULTAR (Leer un solo registro) ---
    override fun consultar(id: Int): Notificacion? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "NOTIFICACION", // Nombre de la tabla
            arrayOf("id", "nombrePaciente", "mensaje", "hora", "fecha"), // Columnas a retornar
            "id = ?", // Cláusula WHERE
            arrayOf(id.toString()), // Argumentos WHERE
            null, null, null
        )

        val notificacion: Notificacion? = if (cursor.moveToFirst()) {
            // Se recupera la data por el índice de la columna:
            val paciente = cursor.getString(1)
            val especialista = cursor.getString(2)
            val noti = cursor.getString(3)
            // Crea y retorna el objeto Notificacion
            Notificacion(id, paciente, especialista, noti)
        } else {
            null // Retorna null si no se encontró
        }

        // Siempre cierra el cursor
        cursor.close()
        return notificacion
    }

    // --- LISTAR (Leer todos los registros) ---
    override fun listar(): List<Notificacion> {
        val lista = mutableListOf<Notificacion>()
        val db = dbHelper.readableDatabase
        // Consulta SQL para seleccionar todos los campos
        val sql = "SELECT id, nombrePaciente, especialista, notificaciones, tipoCita FROM NOTIFICACION"
        val cursor = db.rawQuery(sql, null)

        while (cursor.moveToNext()) {
            // Se recupera la data por el índice de la columna
            val id = cursor.getInt(0)
            val paciente = cursor.getString(1)
            val especialista = cursor.getString(2)
            val noti = cursor.getString(3)
            // Añade el objeto Notificacion a la lista
            lista.add(Notificacion(id, paciente, especialista, noti))
        }
        cursor.close()
        return lista
    }
}
