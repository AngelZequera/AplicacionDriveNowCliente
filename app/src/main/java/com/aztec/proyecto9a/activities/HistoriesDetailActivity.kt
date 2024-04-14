package com.aztec.proyecto9a.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.aztec.proyecto9a.databinding.ActivityHistoriesDetailBinding
import com.aztec.proyecto9a.models.Client
import com.aztec.proyecto9a.models.Driver
import com.aztec.proyecto9a.models.History
import com.aztec.proyecto9a.providers.ClientProvider
import com.aztec.proyecto9a.providers.DriverProvider
import com.aztec.proyecto9a.providers.HistoryProvider
import com.aztec.proyecto9a.utils.RelativeTime
import com.bumptech.glide.Glide

class HistoriesDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoriesDetailBinding
    private var historyProvider = HistoryProvider()
    private var driverProvider = DriverProvider()
    private var extraid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        extraid = intent.getStringExtra("id")!!

        getHistory()

        binding.imgBack.setOnClickListener { finish() }
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraid).addOnSuccessListener { document ->
            if(document.exists()){
                val history = document.toObject(History::class.java)
                binding.txtOrigin.text  = history?.origin
                binding.txtDestination.text  = history?.destination
                binding.txtDate.text  = RelativeTime.getTimeAgo(history?.timestamp!!, this@HistoriesDetailActivity)
                binding.txtPrice.text  = "$ ${String.format("%.1f",history?.price)}"
                binding.txtMyCalification.text  = "${history?.calificationToMedico}"
                binding.txtClientCalification.text  = "${history?.calificationToClient}"
                binding.txtTimeAndDistance.text  = "${history?.time} Min - ${String.format("%.1f", history?.km)} Km"

                getDriverInfo(history?.idDriver!!)

            }
        }
    }

    private fun getDriverInfo(id: String){
        driverProvider.getDriver(id).addOnSuccessListener { document ->
            if(document.exists()){
                val driver = document.toObject(Driver::class.java)
                binding.txtEmail.text = driver?.correo
                binding.txtnombre.text = "${driver?.nombre} ${driver?.apellido}"

                if(driver?.imagen != null){
                    if(driver?.imagen != ""){
                        Glide.with(this).load(driver?.imagen).into(binding.imgProfile)
                    }
                }
            }
        }
    }
}