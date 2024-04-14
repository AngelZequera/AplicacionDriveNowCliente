package com.aztec.proyecto9a.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.aztec.proyecto9a.databinding.ActivityCalificationBinding
import com.aztec.proyecto9a.models.History
import com.aztec.proyecto9a.providers.HistoryProvider
class CalificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalificationBinding
    private var historyprovider = HistoryProvider()
    private var calification = 0f
    private var history: History? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, value, fromUser ->
            calification = value
        }

        binding.btnCalification.setOnClickListener {
            if (history?.id != null){
                updateCalification(history?.id!!)
            }else{
                Toast.makeText(this, "El id del historial es nulo", Toast.LENGTH_LONG).show()
            }
        }

        getHistory()
    }

    private fun updateCalification(idDocument: String){
        historyprovider.updateCalificationToMedico(idDocument, calification).addOnCompleteListener {
            if(it.isSuccessful){
                goToMap()
            }else{
                Toast.makeText(this@CalificationActivity, "Error al actulizar la calificacion", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }


    private fun getHistory(){
        historyprovider.getLastHistory().get().addOnSuccessListener { query ->
            if(query != null){
                if(query.documents.size > 0){
                    history = query.documents[0].toObject(History::class.java)
                    history?.id = query.documents[0].id
                    binding.txtOrigin.text = history?.origin
                    binding.txtDestination.text = history?.destination
                    binding.txtPrice.text = "${String.format("%.1f",history?.price)}"
                    binding.txtTimeAndDistance.text = "${history?.time} Min - ${String.format("%.1f",history?.km)}"
                    Log.d("FIRESTORE", "History: ${history?.toJson()}")
                }else{
                    Toast.makeText(this, "No se encontro el historial", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}