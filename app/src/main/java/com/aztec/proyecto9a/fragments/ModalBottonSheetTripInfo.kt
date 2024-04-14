package com.aztec.proyecto9a.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import com.aztec.proyecto9a.providers.DriverProvider
import com.aztec.proyecto9a.providers.GeoProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.toObject
import de.hdodenhof.circleimageview.CircleImageView

class ModalBottonSheetTripInfo: BottomSheetDialogFragment() {

    private var driver: Driver? = null
    private lateinit var booking: Booking

    //Información del cliente
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()

    var txtClientName: TextView? = null
    var txtOrigin: TextView? = null
    var txtDestino: TextView? = null
    var imgPhone: ImageView? = null
    var imgClient: CircleImageView? = null

    val REQUEST_PHONE_CALL = 30


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.modal_botton_sheet_trip_info,container, false)

        txtClientName = view.findViewById(R.id.txtClientName)
        txtOrigin = view.findViewById(R.id.txtOrigin)
        txtDestino = view.findViewById(R.id.txtDestination)
        imgPhone = view.findViewById(R.id.imgPhone)
        imgClient = view.findViewById(R.id.imgCliente)

        val data = arguments?.getString("booking")
        booking = Booking.fromJson(data!!)!!
     //   getDriverMedico()

        txtOrigin?.text = booking.origin
        txtDestino?.text = booking.destination
        imgPhone?.setOnClickListener {
         if(driver?.telefono != null){
             if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                 ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
             }
             call(driver?.telefono!!)
         }
        }

        getDriverInfo()
        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_CALL){
           if (driver?.telefono != null){
               call(driver?.telefono!!)
           }
        }
    }

    private fun call(phone: String){
        val i = Intent(Intent.ACTION_CALL)
        i.data = Uri.parse("tel:${phone}")

        if(ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            return
        }
        requireActivity().startActivity(i)
    }

    private fun getDriverInfo(){
        driverProvider.getDriver(booking.idDriver!!).addOnSuccessListener { document ->
            if(document.exists()){
                driver = document.toObject(Driver::class.java)
                txtClientName?.text = "${driver?.nombre} ${driver?.apellido}"


                if (driver?.imagen != null){
                    if (driver?.imagen != ""){
                        Glide.with(requireActivity()).load(driver?.imagen).into(imgClient!!)
                    }
                }
     //           txtUserName?.text = "${driver?.nombre} ${driver?.apellido}"
            }
        }
    }

    companion object {
        const val TAG = "ModalBottonSheetTripInfo"
    }

}