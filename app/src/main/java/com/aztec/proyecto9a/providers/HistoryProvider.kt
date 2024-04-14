package com.aztec.proyecto9a.providers

import android.util.Log
import com.aztec.proyecto9a.models.Booking
import com.aztec.proyecto9a.models.Client
import com.aztec.proyecto9a.models.History
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class HistoryProvider {
    val db = Firebase.firestore.collection("Histories")
    val authProvider = AuthProvider()


    fun create(history: History): Task<DocumentReference> {
       return db.add(history).addOnFailureListener {
           Log.d("FIRESTORE","Error: ${it.message}")
       }
    }

    fun getLastHistory(): Query{
        //Consulta compuesta - indice
        return db.whereEqualTo("idCliente",authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
    }

    fun getHistoryById(id: String): Task<DocumentSnapshot> {
        return db.document(id).get()
    }


    fun getBooking(): Query {
        return db.whereEqualTo("idDriver", authProvider.getId())
    }

    fun getHistories(): Query{
        //Consulta compuesta - indice
        return db.whereEqualTo("idCliente",authProvider.getId()).orderBy("timestamp", Query.Direction.DESCENDING)
    }

    fun updateCalificationToMedico (id: String, calification: Float): Task<Void> {
        return db.document(id).update("calificationToMedico", calification).addOnFailureListener { exception ->
            Log.d("FIRESTORE", "ERROR ${exception.message}")
        }
    }
}