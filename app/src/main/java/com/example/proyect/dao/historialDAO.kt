package com.example.appproy.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.proyect.db.CitaDBHelper
import com.example.appproy.model.Historial

class historialDAO(private val dbHelper: CitaDBHelper) : Icrud<Historial, Int> {
    companion object{
        private const val TABLE_NAME = "historial"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE_PACIENTE = "nombrePaciente"
        private const val COLUMN_ESPECIALISTA = "especialista"
        private const val COLUMN_TIPO_CITA = "tipoCita"
        private val ALL_COLUMNS = arrayOf(COLUMN_ID, COLUMN_NOMBRE_PACIENTE, COLUMN_ESPECIALISTA, COLUMN_TIPO_CITA)

    }
    override fun insertar(obj: Historial): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // No incluimos el ID ya que es AUTOINCREMENT
            put(COLUMN_NOMBRE_PACIENTE, obj.nombrePaciente)
            put(COLUMN_ESPECIALISTA, obj.especialista)
            put(COLUMN_TIPO_CITA, obj.tipoCita)
        }
        // Inserta en la tabla historial
        return db.insert(TABLE_NAME, null, values)
    }

    // --- ACTUALIZAR ---
    override fun actualizar(obj: Historial): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE_PACIENTE, obj.nombrePaciente)
            put(COLUMN_ESPECIALISTA, obj.especialista)
            put(COLUMN_TIPO_CITA, obj.tipoCita)
        }

        val whereClause = "$COLUMN_ID = ?"
        // El ID debe ser el criterio para saber qué fila actualizar
        val whereArgs = arrayOf(obj.id.toString())

        return db.update(TABLE_NAME, values, whereClause, whereArgs)
    }

    // --- ELIMINAR  ---
    override fun eliminar(id: Int): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())

        // Elimina la fila donde el ID coincide
        return db.delete(TABLE_NAME, whereClause, whereArgs)
    }

    override fun consultar(id: Int): Historial? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            ALL_COLUMNS, // Usamos el array de columnas
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val historial: Historial? = if (cursor.moveToFirst()) {
            // Recuperamos la data usando los índices
            val paciente = cursor.getString(1) // Índice 1: COLUMN_NOMBRE_PACIENTE
            val especialista = cursor.getString(2) // Índice 2: COLUMN_ESPECIALISTA
            val tipo = cursor.getString(3) // Índice 3: COLUMN_TIPO_CITA

            // Creamos y retornamos el objeto Historial
            Historial(id, paciente, especialista, tipo)
        } else {
            null // Retorna null si no se encontró
        }

        cursor.close()
        return historial
    }

    // --- LISTAR ---
    override fun listar(): List<Historial> {
        val lista = mutableListOf<Historial>()
        val db: SQLiteDatabase = dbHelper.readableDatabase
        // Construimos el query usando las constantes
        val sql = "SELECT ${ALL_COLUMNS.joinToString(", ")} FROM $TABLE_NAME"
        val cursor = db.rawQuery(sql, null)

        while (cursor.moveToNext()) {
            // Recuperamos la data por el índice (que sigue el orden de ALL_COLUMNS)
            val id = cursor.getInt(0)
            val paciente = cursor.getString(1)
            val especialista = cursor.getString(2)
            val tipo = cursor.getString(3)

            // Añadimos el objeto Historial a la lista
            lista.add(Historial(id, paciente, especialista, tipo))
        }
        cursor.close()
        return lista
    }
}
