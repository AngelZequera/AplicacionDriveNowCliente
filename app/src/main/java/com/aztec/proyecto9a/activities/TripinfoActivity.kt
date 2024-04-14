package com.aztec.proyecto9a.activities

import android.content.Intent
import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.aztec.proyecto9a.R
import com.aztec.proyecto9a.databinding.ActivityTripinfoBinding
import com.aztec.proyecto9a.models.Price
import com.aztec.proyecto9a.providers.ConfigProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class TripinfoActivity : AppCompatActivity(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private lateinit var binding: ActivityTripinfoBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null

    private var extraOriginName: String = ""
    private var extraDestinationName: String = ""
    private var extraOriginLat: Double = 0.0
    private var extraOriginLng: Double = 0.0
    private var extraDestinationLat: Double = 0.0
    private var extraDestinationLng: Double = 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var wayPoint: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG = "way_point_tag"
    private lateinit var directionUtil: DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestination: Marker? = null

    var distance: Double = 0.0
    var time:Double = 0.0

    private var configProvider = ConfigProvider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTripinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        extraOriginName = intent.getStringExtra("origen")!!
        extraDestinationName = intent.getStringExtra("destination")!!
        extraOriginLat = intent.getDoubleExtra("origen_lat", 0.0)
        extraOriginLng = intent.getDoubleExtra("origen_lng", 0.0)
        extraDestinationLat = intent.getDoubleExtra("destination_lat", 0.0)
        extraDestinationLng = intent.getDoubleExtra("destination_lng", 0.0)

        originLatLng = LatLng(extraOriginLat, extraOriginLng)
        destinationLatLng = LatLng(extraDestinationLat, extraDestinationLng)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }


        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        binding.textViewOrigin.text = extraOriginName
        binding.textViewDestination.text = extraDestinationName

        Log.d("LOCALIZACION", "Origen lat: ${originLatLng?.latitude}")
        Log.d("LOCALIZACION", "Origen lng: ${originLatLng?.longitude}")
        Log.d("LOCALIZACION", "Destination lat: ${destinationLatLng?.latitude}")
        Log.d("LOCALIZACION", "Destination lng: ${destinationLatLng?.longitude}")

        binding.imgBack.setOnClickListener { finish() }

        binding.btnConfirmRequest.setOnClickListener { goToSeatchDriverMedico() }

    }

    // ------------------------------------------------- Solo en el caso de destino
    private fun goToSeatchDriverMedico(){
        if(originLatLng != null && destinationLatLng != null){
            val i = Intent(this,SearchActivity::class.java)
            i.putExtra("origen", extraOriginName)
            i.putExtra("destination", extraDestinationName)
            i.putExtra("origen_lat", originLatLng?.latitude)
            i.putExtra("origen_lng", originLatLng?.longitude)
            i.putExtra("destination_lat", destinationLatLng?.latitude)
            i.putExtra("destination_lng", destinationLatLng?.longitude)
            i.putExtra("time", time)
            i.putExtra("distance", distance)

            startActivity(i)
        }else{
            Toast.makeText(this, "Debes seleccionar el origen", Toast.LENGTH_LONG).show()
        }

    }


    private fun getPrices(distance: Double, time: Double){
        configProvider.getPrice().addOnSuccessListener { document ->
            if(document.exists()){
                val prices = document.toObject(Price::class.java) // Obtenido el dodumento con la información

                val totalDistance = distance * prices?.km!! // Valor por kilometro
                Log.d("Prices","totalDistance: $totalDistance")
                val totaltime = time * prices?.min!! // Valor por minuto
                Log.d("Prices","totalTime: $totaltime")
                var total = totalDistance + totaltime // Total a pagar
                Log.d("Prices","total: $total")
                total = if(total < 100.0) prices?.minValue!! else total
                Log.d("Prices","new total: $total")

                var minTotal = total - prices?.difference!! // Restarle 40 pesos
                Log.d("Prices","min total: $minTotal")
                var maxTotal = total + prices?.difference!! // Sumarle 40 pesos
                Log.d("Prices","max total: $maxTotal")

                val minTotalString = String.format("%.2f",minTotal)
                val maxTotalString = String.format("%.2f",maxTotal)
                binding.textViewPrice.text = "$ $minTotalString - $maxTotalString"


            }
        }
    }

    private fun addOriginMarker(){
        markerOrigin = googleMap?.addMarker(MarkerOptions().position(originLatLng!!).title("Mi posicion")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }


    private fun addDestinationMarker(){
        markerDestination = googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Llegada")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
    }

    private fun easyDrawRoute(){
        wayPoint.add(originLatLng!!)
        wayPoint.add(destinationLatLng!!)
        directionUtil = DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng!!)
            .setWayPoints(wayPoint)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.black)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng!!)
            .build()

        directionUtil.initPath()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //Permitir dar zoom
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder().target(originLatLng!!).zoom(13f).build()
        ))

        easyDrawRoute()
        addOriginMarker()
        addDestinationMarker()

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style)
            )

            if(!success!!){
                Log.d("MAPAS","No se encontro el estilo del mapa")
            }
        }catch (e: Resources.NotFoundException){
            Log.d("MAPAS","Error: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {

    }

    override fun locationCancelled() {

    }

    override fun onDestroy() { // Cierra la aplicacion o entra a otra Activity
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
         distance = polyLineDetailsArray[1].distance.toDouble() // En metros
         time = polyLineDetailsArray[1].time.toDouble() // En segundos

        distance = if(distance < 1000.0) 1000.0 else distance // Si es menos de 1000 metros, su valor sera 1km
        time = if(time < 60.0) 60.0 else time

        distance = distance / 1000 // obtener los kilometros
        time = time / 60 // obtener los minutos

        val timeString = String.format("%.2f",time)
        val distanceString = String.format("%.2f",distance)

        getPrices(distance, time)

        binding.textViewTimeAndDistance.text = "$timeString min - $distanceString km"

        directionUtil.drawPath(WAY_POINT_TAG)
    }

}