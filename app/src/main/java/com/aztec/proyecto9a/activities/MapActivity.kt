package com.aztec.proyecto9a.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aztec.proyecto9a.R
import com.aztec.proyecto9a.databinding.ActivityMapBinding
import com.aztec.proyecto9a.fragments.ModalBottonSheetMenu
import com.aztec.proyecto9a.models.Booking
import com.aztec.proyecto9a.models.DriverLocation
import com.aztec.proyecto9a.providers.AuthProvider
import com.aztec.proyecto9a.providers.BookingProvider
import com.aztec.proyecto9a.providers.ClientProvider
import com.aztec.proyecto9a.providers.GeoProvider
import com.aztec.proyecto9a.utils.CarMoveAnim
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ktx.addMarker
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLating: LatLng? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private val clientProvider = ClientProvider()
    private val bookingProvider = BookingProvider()

    //Google places
    private var places: PlacesClient? = null
    private var autocompleteOrigin: AutocompleteSupportFragment? = null
    private var autocompleteDestination: AutocompleteSupportFragment? = null
    private var originName: String = ""
    private var destinationName: String = ""
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var isLocationEnabled = false

    private val driverMarkersdoctors = ArrayList<Marker>()
    private val driverLocation = ArrayList<DriverLocation>()
    private val modalMenu = ModalBottonSheetMenu()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this,locationRequest, false,false,this)

        //Permisos para acceder a la ubicación
        locationpermission.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        startGoogleplaces()
        removeBooking()
        createToken()

        binding.btnRequestTrip.setOnClickListener { goToTripinfo() }
        binding.imgMenu.setOnClickListener { showModalMenu() }

    }

    val locationpermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            when {
                permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    Log.d("Localizacion", "Permiso concedido")
                     easyWayLocation?.startLocation();


                }

                permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("Localizacion", "Permiso concedido con limitacion")
                      easyWayLocation?.startLocation();

                }

                else -> {
                    Log.d("Localizacion", "Permiso no concedido")
                }
            }
        }
    }

    private fun createToken(){
        clientProvider.createToken(authProvider.getId())
    }

    private fun showModalMenu(){
        modalMenu.show(supportFragmentManager, ModalBottonSheetMenu.TAG)
    }


    private fun removeBooking(){
        bookingProvider.getBooking().get().addOnSuccessListener { document ->
            if(document.exists()){
                val booking = document.toObject(Booking::class.java)
                if (booking?.status == "create" || booking?.status == "cancel"){
                    bookingProvider.remove()
                }
            }
        }

    }

    private fun getNearDoctors(){

        if(myLocationLating == null) return

        geoProvider.getNeartyDoctors(myLocationLating!!,30.0).addGeoQueryEventListener(object : GeoQueryEventListener{

            override fun onKeyEntered(documentID: String, location: GeoPoint) {

                Log.d("FIRESTORE", "Document id: $documentID")
                Log.d("FIRESTORE", "location: $location")

                //Se ejecutara cuando encuentre algun doctor cercano
                for(marker in driverMarkersdoctors){
                    if(marker.tag != null){
                        if(marker.tag == documentID){
                            return
                        }
                    }
                }

                // De lo contrario se crea un nuevo marcador para el conductor conectado
                val driverdoctorLatLng = LatLng(location.latitude, location.longitude)
                val marker = googleMap?.addMarker(
                    MarkerOptions().position(driverdoctorLatLng).title("Medico disponible").icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.uber_car)
                )
                )

                marker?.tag = documentID
                driverMarkersdoctors.add(marker!!)

                // dl es el documento del conductor encontrado
                val dl = DriverLocation()
                dl.id = documentID
                driverLocation.add(dl)

            }

            override fun onKeyExited(documentID: String) {
                //Cuando el medico se desconecte
                for (marker in driverMarkersdoctors){
                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            marker.remove()
                            driverMarkersdoctors.remove(marker)
                            driverLocation.removeAt(getPositionDriver(documentID))
                            return
                        }
                    }
                }
            }


            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                //Cuando el medico se mueva (siempre se actualizara)
                for (marker in driverMarkersdoctors){

                    val start = LatLng(location.latitude, location.longitude)
                    var end: LatLng? = null
                    val position = getPositionDriver(marker.tag.toString())

                    if (marker.tag != null){
                        if (marker.tag == documentID){
                          //  marker.position = LatLng(location.latitude, location.longitude)

                            if(driverLocation[position].latlng != null){
                                end = driverLocation[position].latlng
                            }
                            driverLocation[position].latlng = LatLng(location.latitude, location.longitude)
                            if(end != null){
                                CarMoveAnim.carAnim(marker, end, start)
                            }
                        }
                    }
                }
            }

            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() {

            }






        })
    }

    // ------------------------------------------------- Solo en el caso de destino
    private fun goToTripinfo(){
        if(originLatLng != null && destinationLatLng != null){
            val i = Intent(this,TripinfoActivity::class.java)
            i.putExtra("origen", originName)
            i.putExtra("destination", destinationName)
            i.putExtra("origen_lat", originLatLng?.latitude)
            i.putExtra("origen_lng", originLatLng?.longitude)
            i.putExtra("destination_lat", destinationLatLng?.latitude)
            i.putExtra("destination_lng", destinationLatLng?.longitude)

            startActivity(i)
        }else{
            Toast.makeText(this, "Debes seleccionar el origen", Toast.LENGTH_LONG).show()
        }

    }

    private fun getPositionDriver(id:String): Int {
        var position = 0
        for(i in driverLocation.indices){
            if (id == driverLocation[i].id){
                position = i
                break
            }
        }
        return position
    }



    private fun onCamaraMove(){
        googleMap?.setOnCameraIdleListener {
            try {
                val geocoder = Geocoder(this)
                originLatLng = googleMap?.cameraPosition?.target

                if(originLatLng != null){
                    val addressList = geocoder.getFromLocation(originLatLng?.latitude!!, originLatLng?.longitude!!, 1)

                    if(addressList!!.size > 0){
                        val city = addressList!![0].locality
                        val country = addressList[0].countryName
                        val address = addressList[0].getAddressLine(0)
                        originName = "$address $city"
                        autocompleteOrigin?.setText("$address $city")
                    }
                }

            }catch (e: Exception){
                Log.d("ERROR", "Mensaje error: ${e.message}")
            }
        }
    }


    private fun startGoogleplaces(){
        if(!Places.isInitialized()){
            Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))

        }

        places = Places.createClient(this)
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()
    }

    private fun limitSearch(){
        val northSide = SphericalUtil.computeOffset(myLocationLating, 5000.0, 0.0)
        val southSide = SphericalUtil.computeOffset(myLocationLating, 5000.0, 180.0)

        autocompleteOrigin?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
        autocompleteDestination?.setLocationBias(RectangularBounds.newInstance(southSide, northSide))
    }

    private fun instanceAutocompleteOrigin(){
        autocompleteOrigin = supportFragmentManager.findFragmentById(R.id.placesAutocompletOrigin) as AutocompleteSupportFragment
        autocompleteOrigin?.setPlaceFields(
            listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            )
        )
        autocompleteOrigin?.setHint("Lugar de recogida")
        autocompleteOrigin?.setCountry("MX")
        autocompleteOrigin?.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onError(p0: Status) {

            }

            override fun onPlaceSelected(place: Place) {
                originName = place.name!!
                originLatLng = place.latLng
                Log.d("PLACES","Address: ${originName}")
                Log.d("PLACES","Latitud: ${originLatLng?.latitude}")
                Log.d("PLACES","Longitud: ${originLatLng?.longitude}")
            }
        })
    }

    private fun instanceAutocompleteDestination(){
        autocompleteDestination = supportFragmentManager.findFragmentById(R.id.placesAutocompletDestination) as AutocompleteSupportFragment
        autocompleteDestination?.setPlaceFields(
            listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            )
        )
        autocompleteDestination?.setHint("Destino")
        autocompleteDestination?.setCountry("MX")
        autocompleteDestination?.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }

            override fun onPlaceSelected(place: Place) {
                destinationName = place.name!!
                destinationLatLng = place.latLng
                Log.d("PLACES","Address: ${destinationName}")
                Log.d("PLACES","Latitud: ${destinationLatLng?.latitude}")
                Log.d("PLACES","Longitud: ${destinationLatLng?.longitude}")
            }
        })
    }


    override fun onResume() {
        super.onResume()
        easyWayLocation?.startLocation() // Cada vez que abrimos la pantalla actual
    }

    override fun onDestroy() { // Cierra la aplicacion o entra a otra Activity
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //Permitir dar zoom
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        onCamaraMove()
        //easyWayLocation?.startLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        googleMap?.isMyLocationEnabled = false

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

    override fun currentLocation(location: Location) { // Actualizacion de la posicion en tiempo real
        // Obtenido la LAT y LONG de la poscion actual
        myLocationLating = LatLng(location.latitude, location.longitude)

        if(!isLocationEnabled){ // Solo ingresa un sola vez
            isLocationEnabled = true
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationLating!!).zoom(15f).build()
            ))
            getNearDoctors()
            limitSearch()
        }

    }

    override fun locationCancelled() {

    }


}