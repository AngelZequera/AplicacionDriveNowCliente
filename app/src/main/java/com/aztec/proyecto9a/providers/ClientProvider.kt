package com.aztec.proyecto9a.providers

import android.net.Uri
import android.util.Log
import com.aztec.proyecto9a.models.Client
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

class ClientProvider {
    val db = Firebase.firestore.collection("Clients")
    var storage = FirebaseStorage.getInstance().getReference().child("profile")


    fun create(client: Client): Task<Void> {
        return db.document(client.id!!).set(client)
    }

    fun getClientById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }


    fun uploadImage(id: String, file: File): StorageTask<UploadTask.TaskSnapshot> {
        var fromeFile = Uri.fromFile(file)
        val ref = storage.child("$id.jpg")
        storage = ref
        val uploadTask = ref.putFile(fromeFile)

        return uploadTask.addOnFailureListener {
            Log.d("FIREBASE", "ERROR {${it.message}}")
        }
    }


    fun getImageUrl(): Task<Uri> {
        return storage.downloadUrl
    }

    fun createToken(idClient: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful){
                val token = it.result // Token de notificaciones
                updateToken(idClient, token)
            }
        }
    }


    fun updateToken(idClient: String, token: String): Task<Void> {

        val map: MutableMap<String, Any> = HashMap()
        map["token"] = token
        return db.document(idClient).update(map)
    }


    fun update(client: Client): Task<Void> {

        val map: MutableMap<String, Any> = HashMap()
        map["nombre"] = client?.nombre!!
        map["apellido"] = client?.apellido!!
        map["telefono"] = client?.telefono!!
        map["imagen"] = client?.imagen!!
        return db.document(client?.id!!).update(map)
    }

}