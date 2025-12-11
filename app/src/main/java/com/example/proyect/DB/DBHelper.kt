package com.example.proyect.DB

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "citasDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE citas(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombrePaciente TEXT,
                especialista TEXT,
                tipoCita TEXT,
                fecha TEXT,
                hora TEXT,
                sexo TEXT,
                tipoSangre TEXT,
                peso TEXT,
                altura TEXT,
                alergias TEXT,
                hospitalNombre TEXT,
                hospitalDireccion TEXT,
                hospitalTelefono TEXT,
                hospitalHorario TEXT
            )
        """.trimIndent()
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS citas")
        onCreate(db)
    }

    fun agregarCita(
        nombrePaciente: String,
        especialista: String,
        tipoCita: String,
        fecha: String,
        hora: String,
        sexo: String,
        tipoSangre: String,
        peso: String,
        altura: String,
        alergias: String,
        hospitalNombre: String?,
        hospitalDireccion: String?,
        hospitalTelefono: String?,
        hospitalHorario: String?
    ): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put("nombrePaciente", nombrePaciente)
        cv.put("especialista", especialista)
        cv.put("tipoCita", tipoCita)
        cv.put("fecha", fecha)
        cv.put("hora", hora)
        cv.put("sexo", sexo)
        cv.put("tipoSangre", tipoSangre)
        cv.put("peso", peso)
        cv.put("altura", altura)
        cv.put("alergias", alergias)
        cv.put("hospitalNombre", hospitalNombre)
        cv.put("hospitalDireccion", hospitalDireccion)
        cv.put("hospitalTelefono", hospitalTelefono)
        cv.put("hospitalHorario", hospitalHorario)

        val resultado = db.insert("citas", null, cv)
        db.close()
        return resultado != -1L
    }
}
