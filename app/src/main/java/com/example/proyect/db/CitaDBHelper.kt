package com.example.proyect.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CitaDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    override fun onCreate(db: SQLiteDatabase) {
        // 1. CREACIÓN DE TABLAS EXISTENTES
        db.execSQL("""
        CREATE TABLE CITA(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombrePaciente TEXT,
            especialista TEXT,
            fecha TEXT,
            hora TEXT,
            tipoCita TEXT
        )
    """)

        db.execSQL("""
        CREATE TABLE HISTORIAL(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombrePaciente TEXT,
            especialista TEXT,
            tipoCita TEXT
        )
    """)

        db.execSQL("""
        CREATE TABLE NOTIFICACION(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            mensaje TEXT,
            fecha TEXT,
            hora TEXT
        )
    """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS CITA")
        db.execSQL("DROP TABLE IF EXISTS HISTORIAL")
        db.execSQL("DROP TABLE IF EXISTS NOTIFICACION")
        onCreate(db)
    }
    companion object{
        private const val DATABASE_NAME = "citas.db"
        private const val DATABASE_VERSION = 1
    }
}