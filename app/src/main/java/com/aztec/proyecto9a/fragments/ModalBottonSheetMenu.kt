package com.aztec.proyecto9a.fragments

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.aztec.proyecto9a.R
import com.aztec.proyecto9a.activities.HistoriesActivity
import com.aztec.proyecto9a.activities.MainActivity
import com.aztec.proyecto9a.activities.MapActivity
import com.aztec.proyecto9a.activities.MapTripActivity
import com.aztec.proyecto9a.activities.ProfileActivity
import com.aztec.proyecto9a.models.Booking
import com.aztec.proyecto9a.models.Client
import com.aztec.proyecto9a.models.Driver
import com.aztec.proyecto9a.providers.AuthProvider
import com.aztec.proyecto9a.providers.BookingProvider
import com.aztec.proyecto9a.providers.ClientProvider
import com.aztec.proyecto9a.providers.GeoProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.toObject

class ModalBottonSheetMenu: BottomSheetDialogFragment() {

    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    var txtUserName: TextView? = null
    var linearLayoutLogout: LinearLayout? = null
    var linearLayoutProfile: LinearLayout? = null
    var LinearLayoutHistory: LinearLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view = inflater.inflate(R.layout.modal_botton_sheet_menu,container, false)
        txtUserName = view.findViewById(R.id.txtUserName)
        linearLayoutLogout = view.findViewById(R.id.LinearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.LinearLayoutProfile)
        LinearLayoutHistory = view.findViewById(R.id.LinearLayoutHistory)

        getClient()

        linearLayoutLogout?.setOnClickListener {
            goToMain()
        }

        linearLayoutProfile?.setOnClickListener {
            goToProfile()
        }

        LinearLayoutHistory?.setOnClickListener {
            goToHistory()
        }

        return view
    }

    private fun goToProfile(){
        val i = Intent(activity, ProfileActivity::class.java)
        startActivity(i)
    }

  private fun goToHistory(){
        val i = Intent(activity, HistoriesActivity::class.java)
        startActivity(i)
    }


    private fun goToMain(){
        authProvider.logout()
        val i = Intent(activity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun getClient(){
        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                val client = document.toObject(Client::class.java)
                txtUserName?.text = "${client?.nombre} ${client?.apellido}"
            }
        }
    }

    companion object {
        const val TAG = "ModalBottonSheetMenu"
    }

}