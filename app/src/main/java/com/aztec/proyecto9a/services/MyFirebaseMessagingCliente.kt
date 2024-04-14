package com.aztec.proyecto9a.services

import android.util.Log
import com.aztec.proyecto9a.channel.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingCliente: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"]
        val body = data["body"]

        Log.d("NOTIFICACION", "Data: $data")
        Log.d("NOTIFICACION", "Title: $title")
        Log.d("NOTIFICACION", "Body: $body")

        if (!title.isNullOrBlank() && !body.isNullOrBlank()) {
            showNotifcation(title, body)
        }
        //Enviar información
    }

    private fun showNotifcation(title: String, body: String) {
        val helper = NotificationHelper(baseContext)
        val builder = helper.getNotification(title, body)
        //Mostrar la notificación
        helper.getManager().notify(1, builder.build())

    }
}