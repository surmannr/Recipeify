package hu.bme.aut.recipeify.fragments


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify_hf.R


class NewReceptDialogFragment : DialogFragment() {

    private lateinit var receptnevEditForm: EditText
    private lateinit var receptkategoriakEditForm: ListView
    private lateinit var recepthozzavalokEditForm: EditText
    val selectedItems : ArrayDeque<String> = ArrayDeque()

    private lateinit var btnKategoria: Button
    private lateinit var spinnerKategoria: Spinner
    private lateinit var _context: Context

    private var kategoria_nevek: ArrayList<String> = ArrayList()

    interface NewReceptDialogListener {
        fun onReceptItemCreated(newItem: Recept)
    }

    private lateinit var listener: NewReceptDialogListener

    public fun setDataKategoriak(data: ArrayList<String>){
        kategoria_nevek = data
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
        listener = context as? NewReceptDialogListener
            ?: throw RuntimeException("Activity must implement the NewReceptDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.new_recept_item)
            .setView(getContentView())
            .setPositiveButton(R.string.ok) { _, _ ->
                if (isValid()) {
                    listener.onReceptItemCreated(getReceptItem())
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    private fun receptkat_buttonset(_context: Context) {
        spinnerKategoria.adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, kategoria_nevek )
        btnKategoria.setOnClickListener(){
            selectedItems.addFirst(spinnerKategoria.selectedItem.toString())
            //Log.d("TESZT", spinnerKategoria.selectedItem.toString())
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
        return Recept(
                id = null,
                nev = receptnevEditForm.text.toString(),
                hozzavalok = recepthozzavalokEditForm.text.toString(),
                kategoria = list.clone() as List<String>,
                kedvenc = false
        )
    }


    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.dialog_new_recept_item, null)
            receptnevEditForm = contentView.findViewById(R.id.ReceptName_form)
            recepthozzavalokEditForm = contentView.findViewById(R.id.Hozzavalok_form)
            receptkategoriakEditForm = contentView.findViewById(R.id.ReceptKategoriak_form)
            btnKategoria = contentView.findViewById(R.id.btn_receptkategoriak)
            spinnerKategoria = contentView.findViewById(R.id.spinner_kategoriak)
            receptkat_buttonset(_context)
        return contentView
    }
    companion object {
        const val TAG = "NewReceptDialogFragment"
    }
}