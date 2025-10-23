package com.example.appproy.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.proyect.db.CitaDBHelper
import com.example.appproy.model.Cita

class citaDAO(private val dbHelper: CitaDBHelper) : Icrud<Cita, Int> {
    companion object{
        private const val TABLE_NAME = "cita"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE_PACIENTE = "nombrePaciente"
        private const val COLUMN_ESPECIALISTA = "especialista"
        private const val COLUMN_FECHA = "fecha"
        private const val COLUMN_HORA = "hora"
        private const val COLUMN_TIPO_CITA = "tipoCita"
        private val ALL_COLUMNS = arrayOf(COLUMN_ID, COLUMN_NOMBRE_PACIENTE, COLUMN_ESPECIALISTA, COLUMN_FECHA, COLUMN_HORA, COLUMN_TIPO_CITA)

    }

    override fun insertar(obj: Cita): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE_PACIENTE, obj.nombrePaciente)
            put(COLUMN_ESPECIALISTA, obj.especialista)
            put(COLUMN_FECHA, obj.fecha)
            put(COLUMN_HORA, obj.hora)
            put(COLUMN_TIPO_CITA, obj.tipoCita)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    // --- ACTUALIZAR ---
    override fun actualizar(obj: Cita): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE_PACIENTE, obj.nombrePaciente)
            put(COLUMN_ESPECIALISTA, obj.especialista)
            put(COLUMN_FECHA, obj.fecha)
            put(COLUMN_HORA, obj.hora)
            put(COLUMN_TIPO_CITA, obj.tipoCita)
        }

        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(obj.id.toString())

        return db.update(TABLE_NAME, values, whereClause, whereArgs)
    }

    // --- ELIMINAR (Borrar) ---
    override fun eliminar(id: Int): Int {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())

        return db.delete(TABLE_NAME, whereClause, whereArgs)
    }

    // --- CONSULTAR (Leer un solo registro) ---
    // NOTA: Se corrige el tipo de retorno a Cita?
    override fun consultar(id: Int): Cita? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            ALL_COLUMNS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        val cita: Cita? = if (cursor.moveToFirst()) {
            // Recuperamos la data siguiendo el orden de ALL_COLUMNS
            val paciente = cursor.getString(1)
            val especialista = cursor.getString(2)
            val fecha = cursor.getString(3)
            val hora = cursor.getString(4)
            val tipo = cursor.getString(5)

            Cita(id, paciente, especialista, fecha, hora, tipo)
        } else {
            null
        }

        cursor.close()
        return cita
    }

    // --- LISTAR (Leer todos los registros) ---
    override fun listar(): List<Cita> {
        val lista = mutableListOf<Cita>()
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val sql = "SELECT ${ALL_COLUMNS.joinToString(", ")} FROM $TABLE_NAME"
        val cursor = db.rawQuery(sql, null)

        while (cursor.moveToNext()) {
            // Recuperamos la data siguiendo el orden de ALL_COLUMNS
            val id = cursor.getInt(0)
            val paciente = cursor.getString(1)
            val especialista = cursor.getString(2)
            val fecha = cursor.getString(3)
            val hora = cursor.getString(4)
            val tipo = cursor.getString(5)

            lista.add(Cita(id, paciente, especialista, fecha, hora, tipo))
        }
        cursor.close()
        return lista
    }
}
