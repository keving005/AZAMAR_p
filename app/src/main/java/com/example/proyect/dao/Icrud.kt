package com.example.appproy.dao

import com.example.appproy.model.Cita
import com.example.appproy.model.Historial
import com.example.appproy.model.Notificacion

interface Icrud<T, K> {
    fun insertar(obj: T): Long
    fun actualizar(obj: T): Int
    fun eliminar(id: K): Int
    fun consultar(id: K): T?
    fun listar(): List<T>
}