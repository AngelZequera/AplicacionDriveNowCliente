package com.aztec.proyecto9a.models

class FCMBody (
    val to: String,
    val priority: String,
    val ttl: String,
    val data: MutableMap<String, String>,
){

}