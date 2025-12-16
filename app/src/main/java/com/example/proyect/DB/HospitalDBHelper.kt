package com.example.proyect

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class HospitalDBHelper(context: Context) :
    SQLiteOpenHelper(context, "hospitales.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE hospital (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                lat REAL,
                lng REAL,
                direccion TEXT,
                telefono TEXT,
                horario TEXT
            )
        """
        db.execSQL(createTable)

        // Datos iniciales
        insertarHospital(
            db,
            "Hospital SAME Chalco",
            19.2687548,
            -98.8941361,
            "Av. Cuauhtémoc No. 15",
            "55-1734-0930",
            "24 hrs"
        )

        insertarHospital(
            db,
            "Hospital de Maria",
            19.261497,
            -98.890372,
            "Calle Centro, Chalco de Díaz Covarrubias, Méx.",
            "55-5975-5886",
            "Horario local"
        )

        insertarHospital(
            db,
            "Sanatorio Y Maternidad Montserrat",
            19.258914,
            -98.898135,
            "Alzate 13, La Conchita, 56600 Chalco de Díaz Covarrubias, Méx.",
            "55-5975-5886",
            "Horario local"
        )

        insertarHospital(
            db,
            "Clinica Del Angel",
            19.262434,
            -98.903467,
            "C. Mariano Matamoros 19, San Sebastian, 56600 Chalco de Díaz Covarrubias, Méx.",
            "5559750307",
            "24 hrs"
        )

        insertarHospital(
            db,
            "Unidad Médica del Ángel",
            19.279553,
            -98.921307,
            "C. Ote. 37 Manzana 008 Mz. 22 Lt. 28, Providencia, 56616 Valle de Chalco Solidaridad, Méx.",
            "5565662559",
            "24 hrs"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS hospital")
        onCreate(db)
    }

    private fun insertarHospital(
        db: SQLiteDatabase,
        nombre: String,
        lat: Double,
        lng: Double,
        direccion: String,
        telefono: String,
        horario: String
    ) {
        val values = ContentValues().apply {
            put("nombre", nombre)
            put("lat", lat)
            put("lng", lng)
            put("direccion", direccion)
            put("telefono", telefono)
            put("horario", horario)
        }
        db.insert("hospital", null, values)
    }
}
