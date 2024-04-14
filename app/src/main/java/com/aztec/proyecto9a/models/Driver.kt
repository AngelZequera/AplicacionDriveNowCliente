package com.aztec.proyecto9a.models

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Driver (
    val id: String? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    var imagen: String? = null,
    val placa: String? = null,
    val colorCar: String? = null,
    val marcaAuto: String? = null,
    var token: String? = null
) {


    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Driver>(json)
    }
}