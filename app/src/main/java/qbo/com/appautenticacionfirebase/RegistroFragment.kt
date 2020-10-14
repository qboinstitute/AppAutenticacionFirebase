package qbo.com.appautenticacionfirebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore


class RegistroFragment : Fragment() {

    private lateinit var etnompersona : TextInputEditText
    private lateinit var etapepersona : TextInputEditText
    private lateinit var etedadpersona : TextInputEditText
    private lateinit var btnregpersona : Button
    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val vista : View = inflater.inflate(R.layout.fragment_registro, container, false)
        etnompersona = vista.findViewById(R.id.etnompersona)
        etapepersona = vista.findViewById(R.id.etapepersona)
        etedadpersona = vista.findViewById(R.id.etedadpersona)
        btnregpersona = vista.findViewById(R.id.btnregpersona)
        firestore = FirebaseFirestore.getInstance()
        btnregpersona.setOnClickListener {
            if(etnompersona.text?.isNotEmpty()!! &&
                etapepersona.text?.isNotEmpty()!! &&
                etedadpersona.text?.isNotEmpty()!!){
                registrarPersona(it)
            }else{
                enviarMensaje(it, "Ingrese todos los datos.")
            }
        }
        return vista
    }

    private fun registrarPersona(vista: View){
        val persona = hashMapOf(
            "apellido" to etapepersona.text.toString(),
            "edad" to etedadpersona.text.toString().toInt(),
            "nombre" to etnompersona.text.toString()
        )
        firestore.collection("Persona")
            .add(persona)
            .addOnSuccessListener {
                enviarMensaje(vista, "El Id de registro es: ${it.id}")
            }
            .addOnFailureListener {

            }
    }


    private fun enviarMensaje(vista : View, mensaje: String){
        Snackbar.make(vista, mensaje, Snackbar.LENGTH_LONG).show()
    }


}