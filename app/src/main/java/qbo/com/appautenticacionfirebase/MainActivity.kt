package qbo.com.appautenticacionfirebase

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun obtenerVista(): View{
        return findViewById(android.R.id.content)
    }

    private fun guardarPreferenciaIrAlApp(email :String, tipo : String,
                                          nombre:String, urlimagen : String){
        val preferencia : SharedPreferences.Editor =
            getSharedPreferences("appFirebaseQBO", Context.MODE_PRIVATE).edit()
        preferencia.putString("email", email)
        preferencia.putString("tipo", tipo)
        preferencia.putString("nombre", nombre)
        preferencia.putString("urlimg", urlimagen)
        preferencia.apply()
        startActivity(Intent(this, HomeActivity::class.java))

    }

    private fun validarPreferencia(){
        val preferencia : SharedPreferences = getSharedPreferences("appFirebaseQBO",
            Context.MODE_PRIVATE)
        val email: String? = preferencia.getString("email", null)
        val tipo: String? = preferencia.getString("tipo", null)
        val nombre: String? = preferencia.getString("nombre", null)
        val urlimagen: String? = preferencia.getString("urlimg", null)
        if(email != null && tipo != null && nombre != null && urlimagen != null){
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    private fun enviarMensaje(vista: View, mensaje: String){
        Snackbar.make(vista, mensaje, Snackbar.LENGTH_LONG).show()
    }


}