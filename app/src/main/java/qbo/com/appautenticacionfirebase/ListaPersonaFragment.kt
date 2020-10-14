package qbo.com.appautenticacionfirebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import qbo.com.appautenticacionfirebase.adapter.PersonaAdapter
import qbo.com.appautenticacionfirebase.model.Persona


class ListaPersonaFragment : Fragment() {

    private lateinit var rvfirestore : RecyclerView
    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val vista : View = inflater.inflate(R.layout.fragment_lista_persona, container, false)
        val lstpersonas : ArrayList<Persona> = ArrayList()
        rvfirestore = vista.findViewById(R.id.rvfirestore)
        firestore = FirebaseFirestore.getInstance()
        firestore.collection("Persona")
            .addSnapshotListener { value, error ->
                if(error != null){
                    Toast.makeText(context, "Error ", Toast.LENGTH_LONG).show()
                }
                for (doc in value!!.documentChanges){
                    if(doc.type == DocumentChange.Type.ADDED){
                        lstpersonas.add(
                            Persona(
                            doc.document.data["nombre"].toString(),
                                doc.document.data["apellido"].toString(),
                                doc.document.data["edad"].toString().toInt()
                        ))
                    }
                }
                rvfirestore.adapter = PersonaAdapter(lstpersonas)
                rvfirestore.layoutManager = LinearLayoutManager(vista.context)
            }
        return vista
    }


}