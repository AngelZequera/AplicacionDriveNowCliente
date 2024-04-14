package com.aztec.proyecto9a.providers

import com.aztec.proyecto9a.api.IFCMapi
import com.aztec.proyecto9a.api.RetrofitClient
import com.aztec.proyecto9a.models.FCMBody
import com.aztec.proyecto9a.models.FCMResponse
import retrofit2.Call
import retrofit2.create

class NotificationProvider {
    private val URL = "https://fcm.googleapis.com"

    fun sendNotification(body: FCMBody): Call<FCMResponse>{
        return RetrofitClient.getClient(URL).create(IFCMapi::class.java).send(body)

    }
}