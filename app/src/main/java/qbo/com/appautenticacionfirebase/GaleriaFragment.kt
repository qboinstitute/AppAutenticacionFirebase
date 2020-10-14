package qbo.com.appautenticacionfirebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import qbo.com.appautenticacionfirebase.adapter.ImagenAdapter
import qbo.com.appautenticacionfirebase.model.Imagen


class GaleriaFragment : Fragment() {

    private lateinit var rvimagenes : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_galeria, container, false)
        rvimagenes = view.findViewById(R.id.rvimagenes)
        ListarImagenes(view)
        return view
    }

    fun ListarImagenes(vista: View){
        val storage = FirebaseStorage.getInstance()
        val storageref = storage.reference.child("/imagenesqbo/")
        val lstimagenes : ArrayList<Imagen> = ArrayList()
        val listAllTask : Task<ListResult> = storageref.listAll()
        listAllTask.addOnCompleteListener { result->
            val items: List<StorageReference> = result.result!!.items
            items.forEachIndexed { index, item ->
                item.downloadUrl.addOnCompleteListener {
                    rvimagenes.adapter = ImagenAdapter(lstimagenes, vista.context)
                    rvimagenes.layoutManager = GridLayoutManager(vista.context, 3)
                }.addOnSuccessListener {
                    lstimagenes.add(Imagen(it.toString()))
                }

            }

        }

    }

}