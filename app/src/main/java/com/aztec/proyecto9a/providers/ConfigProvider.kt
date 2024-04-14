package com.aztec.proyecto9a.providers

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore

class ConfigProvider {
    val db = Firebase.firestore.collection("Config")
    fun getPrice(): Task<DocumentSnapshot> {
        return db.document("prices").get().addOnFailureListener { exception ->
            Log.d("Firebase", "Error: ${exception.message}")
        }
    }
}