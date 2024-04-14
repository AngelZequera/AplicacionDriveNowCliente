package com.aztec.proyecto9a.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.aztec.proyecto9a.databinding.ActivityRegisterBinding
import com.aztec.proyecto9a.models.Client
import com.aztec.proyecto9a.providers.AuthProvider
import com.aztec.proyecto9a.providers.ClientProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val  authProvider = AuthProvider()
    private val clientProvider = ClientProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnGoToLogin.setOnClickListener { goToLogin() }

        binding.btnRegistro.setOnClickListener { register() }
    }

    private fun register(){
        val nombre = binding.txtNombre.text.toString()
        val apellido = binding.txtApellido.text.toString()
        val correo = binding.txtCorreo.text.toString()
        val telefono = binding.txtTelefono.text.toString()
        val contrasena = binding.txtContrasena.text.toString()
        val confirmarcontrasena = binding.txtConfirmarContrasena.text.toString()


        if(validacion(nombre, apellido, correo,telefono, contrasena, confirmarcontrasena)){
           authProvider.registro(correo, contrasena).addOnCompleteListener {
               if(it.isSuccessful){
                    val client = Client(
                        id = authProvider.getId(),
                        nombre = nombre,
                        apellido = apellido,
                        correo = correo,
                        telefono = telefono,
                    )
                   clientProvider.create(client).addOnCompleteListener {
                       if(it.isSuccessful){
                           Toast.makeText(this@RegisterActivity, "Registro exitoso", Toast.LENGTH_SHORT).show()
                           goToMap()
                       }else{
                           Toast.makeText(this@RegisterActivity, "Hubo un error Almacenando los datos del usuario ${it.exception.toString()}", Toast.LENGTH_SHORT).show()
                           Log.d("FIREBASE", "Error: ${it.exception.toString()}")
                       }
                   }
               }else{
                   //Mensaje de firebase del porque no se puedo registrar el usuario
                   Toast.makeText(this@RegisterActivity, "Registro fallido ${it.exception.toString()}", Toast.LENGTH_LONG).show()
                   //consola de bd
                   Log.d("FIREBASE", "Error: ${it.exception.toString()}")
               }
           }
        }
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        //Eliminamos las nuevas o viejas pantallas
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }


    private fun validacion(nombre: String,
                           apellido: String,
                           correo: String,
                           telefono: String,
                           contrasena: String,
                           confirmarContrasena: String
    ): Boolean{
        if (nombre.isEmpty() && nombre.isBlank()){
            Toast.makeText(this, "Debe ingresar tu nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        if (apellido.isEmpty() && apellido.isBlank()){
            Toast.makeText(this, "Debe ingresar tus apellidos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (telefono.isEmpty() && telefono.isBlank()){
            Toast.makeText(this, "Debe ingresar tu telefono", Toast.LENGTH_SHORT).show()
            return false
        }

        if (correo.isEmpty() && correo.isBlank()){
            Toast.makeText(this, "Debe ingresar un correo", Toast.LENGTH_SHORT).show()
            return false
        }
        if (contrasena.isEmpty() && contrasena.isBlank()){
            Toast.makeText(this, "Debe ingresar una contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        if (confirmarContrasena.isEmpty() && contrasena.isBlank()){
            Toast.makeText(this, "Debes ingresar la confirmación de contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        if(contrasena != confirmarContrasena){
            Toast.makeText(this, "Las contraseñas deben coincidir", Toast.LENGTH_SHORT).show()
            return false
        }

        if(contrasena.length < 6){
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun goToLogin(){
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
    }

}