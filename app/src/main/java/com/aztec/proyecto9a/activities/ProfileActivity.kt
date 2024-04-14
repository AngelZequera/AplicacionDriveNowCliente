package com.aztec.proyecto9a.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.aztec.proyecto9a.databinding.ActivityProfileBinding
import com.aztec.proyecto9a.models.Client
import com.aztec.proyecto9a.models.Driver
import com.aztec.proyecto9a.providers.AuthProvider
import com.aztec.proyecto9a.providers.ClientProvider
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import java.io.File

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        getClient()
        binding.imgBack.setOnClickListener { finish() }
        binding.btnUpdate.setOnClickListener { updateInfo() }
        binding.imgProfile.setOnClickListener { selectImage() }
    }


    private fun updateInfo(){
        val nombre = binding.txtFieldNombre.text.toString()
        val apellido = binding.txtFieldApellido.text.toString()
        val telefono = binding.txtFieldTelefono.text.toString()

        val client = Client(
            id = authProvider.getId(),
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
        )

        if (imageFile != null){
            clientProvider.uploadImage(authProvider.getId(), imageFile!!).addOnSuccessListener {
                    taskSnapshot ->
                clientProvider.getImageUrl().addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    client.imagen = imageUrl
                    clientProvider.update(client).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this@ProfileActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@ProfileActivity, "Error al actualizar", Toast.LENGTH_LONG).show()
                        }
                    }
                    Log.d("STORAGE", "url: $imageUrl")
                }
                // val  url = taskSnapshot.storage.downloadUrl.toString()
                //Log.d("STORAGE", "URL: ${url}")
            }
        }else{
            clientProvider.update(client).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@ProfileActivity, "Datos actualizados", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this@ProfileActivity, "Error al actualizar", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getClient(){
        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { document ->
            if(document.exists()){
                val client = document.toObject(Client::class.java)
                binding.txtEmail.text = client?.correo
                binding.txtFieldNombre.setText(client?.nombre)
                binding.txtFieldApellido.setText(client?.apellido)
                binding.txtFieldTelefono.setText(client?.telefono)

                if (client?.imagen != null){
                    if(client?.imagen != ""){
                        Glide.with(this).load(client.imagen).into(binding.imgProfile)
                    }
                }
            }
        }
    }

    private val starImagenResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        val resultCode = result.resultCode
        val data = result.data

        //Si selecciono la imagen correctamente
        if(resultCode == Activity.RESULT_OK){
            val fileUri = data?.data
            imageFile = File(fileUri?.path)
            binding.imgProfile.setImageURI(fileUri)
        }else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this, "Accion cancelada", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectImage(){
        ImagePicker.with(this)
            .crop() // Permitirle hacer un recorderte al usuario
            .compress(1024) // Tamaño de imagen
            .maxResultSize(1080, 1080)
            .createIntent { intent ->
                starImagenResult.launch(intent)
            }
    }
}