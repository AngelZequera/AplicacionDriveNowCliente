package com.aztec.proyecto9a.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.aztec.proyecto9a.databinding.ActivityMainBinding
import com.aztec.proyecto9a.providers.AuthProvider

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val authProvider = AuthProvider()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Quitar parte de arriba (Navbar de notificaciones) y la parte de nav de abajo para que se ajuste a la foto de la pantalla al 100%
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        binding.btnRegistrer.setOnClickListener { goToRegistrer() }
        binding.btnLogin.setOnClickListener { login() }

    }

    private fun login(){
        val email = binding.txtEmail.text.toString()
        val contrasena = binding.txtContrasena.text.toString()

        if(validacion(email, contrasena))
            authProvider.login(email, contrasena).addOnCompleteListener {
                if(it.isSuccessful){
                    goToMap()
                }else{
                    Toast.makeText(this@MainActivity, "Error iniciando sesion", Toast.LENGTH_SHORT).show()
                    Log.d("FIREBASE", "ERROR: ${it.exception.toString()}")
                }
            }
    //Toast.makeText(this, "Formulario valido", Toast.LENGTH_SHORT).show()
    }

    private fun goToMap(){
        val i = Intent(this, MapActivity::class.java)
        //Eliminamos las nuevas o viejas pantallas
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun validacion(email: String, contrasena: String): Boolean{
        if(email.isBlank() && email.isEmpty()){
            Toast.makeText(this, "Ingresa tu correo electronico", Toast.LENGTH_SHORT).show()
            return false
        }

        if(contrasena.isBlank() && contrasena.isEmpty()){
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show()
            return false
        }

        return true;
    }

    private  fun goToRegistrer(){
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    override fun onStart() {
        super.onStart()

        if(authProvider.existSeccion()){
            goToMap()
        }
    }
}