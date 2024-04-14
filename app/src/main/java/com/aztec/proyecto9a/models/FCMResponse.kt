package com.aztec.proyecto9a.models

class FCMResponse (
    val succes: Int? = null,
    val falla: Int? = null,
    val cannonical_ids: Int? = null,
    val multicast_id: Long? = null,
    val result: ArrayList<Any> = ArrayList<Any>(),
){
}