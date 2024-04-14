package com.aztec.proyecto9a.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.aztec.proyecto9a.databinding.ActivitySearchBinding
import com.aztec.proyecto9a.models.Booking
import com.aztec.proyecto9a.models.Driver
import com.aztec.proyecto9a.models.FCMBody
import com.aztec.proyecto9a.models.FCMResponse
import com.aztec.proyecto9a.providers.AuthProvider
import com.aztec.proyecto9a.providers.BookingProvider
import com.aztec.proyecto9a.providers.DriverProvider
import com.aztec.proyecto9a.providers.GeoProvider
import com.aztec.proyecto9a.providers.NotificationProvider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private var listernerBooking: ListenerRegistration? = null
    private lateinit var binding: ActivitySearchBinding

    private var extraOriginName: String = ""
    private var extraDestinationName: String = ""
    private var extraOriginLat: Double = 0.0
    private var extraOriginLng: Double = 0.0
    private var extraDestinationLat: Double = 0.0
    private var extraDestinationLng: Double = 0.0
    private var extraTime: Double = 0.0
    private var extraDistance: Double = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    // Providers
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val bookingProvider = BookingProvider()
    private val notificationProvider = NotificationProvider()
    private val driverProvider = DriverProvider()

    //Busqueda del medico
    private var radius = 0.2
    private var idDriverMedico =""
    private var driver: Driver? = null
    private var isDriverMedicoFound = false
    private var driverMedicoLatLng: LatLng? = null
    private var limitRadius = 20


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        //Extras
        extraOriginName = intent.getStringExtra("origen")!!
        extraDestinationName = intent.getStringExtra("destination")!!
        extraOriginLat = intent.getDoubleExtra("origen_lat", 0.0)
        extraOriginLng = intent.getDoubleExtra("origen_lng", 0.0)
        extraDestinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        extraDestinationLng = intent.getDoubleExtra("destination_lng", 0.0)
        extraTime = intent.getDoubleExtra("time", 0.0)
        extraDistance = intent.getDoubleExtra("distance", 0.0)

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)

        getclosesDriverMedico()
        checkIfDriverAccept()
    }



    private fun checkIfDriverAccept(){
        listernerBooking = bookingProvider.getBooking().addSnapshotListener { snapshot, e ->
            if (e != null){
                Log.d("Firestore","Error ${e.message}")
                return@addSnapshotListener
            }

            if(snapshot != null && snapshot.exists()){
                val booking = snapshot.toObject(Booking::class.java)

                //Validacion de las respuestas por parte del conductor
                if(booking?.status == "accept"){
                    Toast.makeText(this@SearchActivity, "Viaje del medico aceptado", Toast.LENGTH_SHORT).show()
                    listernerBooking?.remove()
                    goToMapTrip()
                }else if(booking?.status == "cancel"){
                    Toast.makeText(this@SearchActivity, "Viaje del medico cancelado", Toast.LENGTH_SHORT).show()
                    listernerBooking?.remove()
                    goToMap()
                }
            }
        }
    }

    private fun goToMapTrip(){
        val i = Intent(this, MapTripActivity::class.java)
        startActivity(i)
    }

   private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
       i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    private fun createBooking(idDriver: String){
        val booking = Booking(
            idCliente = authProvider.getId(),
            idDriver = idDriver,
            status = "create",
            destination = extraDestinationName,
            origin = extraOriginName,
            time = extraTime,
            km = extraDistance,
            originLat = extraOriginLat,
            originLng = extraOriginLng,
            destinationLat = extraDestinationLat,
            destinationLng = extraDestinationLng
        )

        bookingProvider.create(booking).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(this@SearchActivity, "Datos del viaje creado", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@SearchActivity, "Error al crear los datos", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun getDriverInfo(){
        driverProvider.getDriver(idDriverMedico).addOnSuccessListener { document ->
            if (document.exists()){
                driver = document.toObject(Driver::class.java)
                sendNotification()
            }
        }
    }

    private fun sendNotification(){
        val map = HashMap<String, String>()
        map.put("title","ALGUIEN SOLICITA UN MEDICO")
        map.put("body","Un usuario esta solicitando un servicio medico a ${String.format("%.1f", extraDistance)} km y ${String.format("%.1f", extraTime)} Min")

        map.put("idBooking",authProvider.getId())

        val body = FCMBody(
            to = driver?.token!!,
            priority = "high",
            ttl = "4500s",
            data = map
        )
        notificationProvider.sendNotification(body).enqueue(object: Callback<FCMResponse> {
            override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                if (response.body() != null){
                    if (response.body()!!.succes == 1){
                        Toast.makeText(this@SearchActivity, "Se envio la notificación", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this@SearchActivity, "No se puedo enviar la notificación", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this@SearchActivity, "Error al enviar notificación", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                Log.d("NOTIFICATION","ERROR: ${t.message}")
            }

        })
    }

    // Obtener el medico mas cercano
    private fun getclosesDriverMedico(){
        val context = this // Almacena el contexto de la actividad en una variable

        geoProvider.getNeartyDoctors(originLatLng!!, radius).addGeoQueryEventListener(object : GeoQueryEventListener{

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if(!isDriverMedicoFound){
                    isDriverMedicoFound = true
                    idDriverMedico = documentID
                    getDriverInfo()
                    Log.d("FIRESTORE","Conductor id: $idDriverMedico")
                    Toast.makeText(context, "$idDriverMedico", Toast.LENGTH_LONG).show()
                    driverMedicoLatLng = LatLng(location.latitude, location.longitude)
                    binding.txtSearch.text = "MEDICO ENCONTRADO\nESPERANDO RESPUESTA"
                    createBooking(documentID)
                }
            }

            override fun onKeyExited(documentID: String) {

            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {

            }


            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() { // Cuando termina la busqueda
               if(!isDriverMedicoFound){
                   radius = radius + 0.2

                   if(radius > limitRadius){
                       binding.txtSearch.text = "NO SE ENCONTRO NINGUN MEDICO"
                       return
                   }else{
                       getclosesDriverMedico()
                   }
               }
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        //Eliminamos el escuchador de eventos
        listernerBooking?.remove()
    }
}