package com.aztec.proyecto9a.api

import com.aztec.proyecto9a.models.FCMBody
import com.aztec.proyecto9a.models.FCMResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Headers

interface IFCMapi {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAK5d0fD0:APA91bH5NdozFPwfK1egllc5B4C3l1O1S-r9iA_PQXJoxIsx3DLx3kFWsvBo7lURIlhRCE0sEKAlnR_q7ucgp5c9BY6MjeVjALlBGmIEB-trqbk0Vsq1CcTaIKRZ8ulc6W3Jl-Vd42bt"
    )
    @POST("fcm/send")
     fun send(@Body body: FCMBody): Call<FCMResponse>
}