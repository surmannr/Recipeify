package hu.bme.aut.recipeify.fragments


import android.R.attr
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.DialogFragmentNavigatorDestinationBuilder
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify_hf.R
import hu.bme.aut.recipeify_hf.R.*
import hu.bme.aut.recipeify_hf.R.drawable.*


class NewReceptDialogFragment : DialogFragment() {

    private lateinit var receptnevEditForm: EditText
    private lateinit var receptkategoriakEditForm: ListView
    private lateinit var recepthozzavalokEditForm: EditText
    private lateinit var kep: ImageView
    val selectedItems : ArrayDeque<String> = ArrayDeque()

    private var receptnev : String? = null
    private var recepthozzavalok : String? = null
    private var receptkategoriak: ArrayList<String>? = null
    private var imageUri: Uri? = null
    private var idx: Int? = null

    private lateinit var btnKategoria: Button
    private lateinit var btnKepvalasztas: Button
    private lateinit var spinnerKategoria: Spinner
    private lateinit var _context: Context

    private var kategoria_nevek: ArrayList<String> = ArrayList()

    interface NewReceptDialogListener {
        fun onReceptItemCreated(newItem: Recept)
        fun onReceptModify(item: Recept, kivalasztott: Int?)
    }

    private lateinit var listener: NewReceptDialogListener

    fun setDataKategoriak(data: ArrayList<String>){
        kategoria_nevek = data
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
        arguments?.getString("RECEPT_NEV")?.let {
            receptnev = it
        }
        arguments?.getString("RECEPT_HOZZAVALOK")?.let {
            recepthozzavalok = it
        }
        arguments?.getStringArrayList("RECEPT_KATEGORIAK")?.let {
            receptkategoriak = it
            selectedItems.addAll(receptkategoriak!!)
        }
        arguments?.getInt("KIVALASZTOTT_ID")?.let {
            idx = it
        }
        arguments?.getString("RECEPT_URISTRING")?.let {
            imageUri = Uri.parse(it)
        }
        listener = context as? NewReceptDialogListener
            ?: throw RuntimeException("Activity must implement the NewReceptDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if(receptnev===null){
            return AlertDialog.Builder(requireContext())
                    .setTitle(string.new_recept_item)
                    .setView(getContentView())
                    .setPositiveButton(string.ok) { _, _ ->
                        if (isValid()) {
                            listener.onReceptItemCreated(getReceptItem())
                        }
                    }
                    .setNegativeButton(string.cancel, null)
                    .create()
        }
        return AlertDialog.Builder(requireContext())
                .setTitle(string.new_recept_item2)
                .setView(getContentView())
                .setPositiveButton(string.ok2) { _, _ ->
                    if (isValid()) {
                        listener.onReceptModify(getReceptItem(), idx)
                    }
                }
                .setNegativeButton(string.cancel, null)
                .create()
    }

    private fun receptkat_buttonset() {
        spinnerKategoria.adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, kategoria_nevek)
        btnKategoria.setOnClickListener(){
            selectedItems.addFirst(spinnerKategoria.selectedItem.toString())
            var param : ViewGroup.LayoutParams = receptkategoriakEditForm.layoutParams
            param.height =
                (param.height + 50 * _context.resources.displayMetrics.density).toInt();
            receptkategoriakEditForm.layoutParams = param
            receptkategoriakEditForm.height.plus(50)
            receptkategoriakEditForm.adapter = ArrayAdapter<String>(this._context, android.R.layout.simple_list_item_1, selectedItems)
            receptkategoriakEditForm.invalidate()
        }
        receptkategoriakEditForm.onItemClickListener = OnItemClickListener { _, _, position, _ ->
            selectedItems.removeAt(position)
            receptkategoriakEditForm.adapter = ArrayAdapter<String>(this._context, android.R.layout.simple_list_item_1, selectedItems)
            receptkategoriakEditForm.invalidate()
        }
    }
    private fun isValid() = receptnevEditForm.text.isNotEmpty()

    private fun getReceptItem() : Recept{

        var list: ArrayList<String> = ArrayList()
        list.addAll(selectedItems)
        selectedItems.clear()
        return Recept(
                id = null,
                kep = imageUri,
                nev = receptnevEditForm.text.toString(),
                hozzavalok = recepthozzavalokEditForm.text.toString(),
                kategoria = list.clone() as ArrayList<String>,
                kedvenc = false
        )
    }


    @SuppressLint("ResourceType")
    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(layout.dialog_new_recept_item, null)
            receptnevEditForm = contentView.findViewById(R.id.ReceptName_form)
            if(receptnev!==null) receptnevEditForm.setText(receptnev)
            recepthozzavalokEditForm = contentView.findViewById(R.id.Hozzavalok_form)
            if(recepthozzavalok!==null) recepthozzavalokEditForm.setText(recepthozzavalok)
            receptkategoriakEditForm = contentView.findViewById(R.id.ReceptKategoriak_form)
            if(receptkategoriak!==null)  receptkategoriakEditForm.adapter = ArrayAdapter<String>(this._context, android.R.layout.simple_list_item_1, receptkategoriak!!)
            btnKategoria = contentView.findViewById(R.id.btn_receptkategoriak)
            spinnerKategoria = contentView.findViewById(R.id.spinner_kategoriak)
            btnKepvalasztas = contentView.findViewById(R.id.btn_kephozzaadas)
            kep = contentView.findViewById(R.id.kep)
            if(imageUri!==null) kep.setImageURI(imageUri)
            else kep.setImageResource(noimage)
            receptkat_buttonset()
            btnKepvalasztas.setOnClickListener{
                val intent = Intent()
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, imageUri)
                intent.type = "image/*"
                startActivityForResult(intent, 9)
            }
            var param : ViewGroup.LayoutParams = receptkategoriakEditForm.layoutParams
            if(receptkategoriak !== null)
            param.height =
                (param.height + receptkategoriak!!.size * 50 * _context.resources.displayMetrics.density).toInt();

        return contentView
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Check if the intent was to pick image, was successful and an image was picked
        if(requestCode == 9 && data != null){

            //Get selected image uri here
            if(requestCode == 9 && data != null){


                val takeFlags: Int = data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                imageUri = data.data!!
                try {
                    requireActivity().contentResolver.takePersistableUriPermission(imageUri!!, takeFlags)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
                kep.setImageURI(imageUri)
            }

        }
    }

    override fun onStop() {
        super.onStop()
        selectedItems.clear()
    }

    companion object {
        const val TAG = "NewReceptDialogFragment"
    }
}