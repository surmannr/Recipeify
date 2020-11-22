package hu.bme.aut.recipeify_hf.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify_hf.R

class NewKategoriaDialogFragment : DialogFragment() {

    private lateinit var kategoriaEditForm: EditText

    private var katnev: String? = null
    private var idx: Int? = null

    private lateinit var _context: Context
    interface NewKategoriaDialogListener {
        fun onKategoriaItemCreated(newItem: Kategoria)
        fun onKategoriaModify(item: Kategoria, kivalasztott: Int?)
    }

    private lateinit var listener: NewKategoriaDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
        arguments?.getString("KATEGORIA_NEV")?.let {
            katnev = it
        }
        arguments?.getInt("KIVALASZTOTT_ID")?.let {
            idx = it
        }
        listener = context as? NewKategoriaDialogListener
            ?: throw RuntimeException("Activity must implement the NewKategoriaDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (katnev===null){
            return AlertDialog.Builder(requireContext())
                    .setTitle("Új kategória létrehozása")
                    .setView(getContentView())
                    .setPositiveButton(R.string.ok) { _, _ ->
                        if (isValid()) {
                            listener.onKategoriaItemCreated(getKategoriaItem())
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
        }
        return AlertDialog.Builder(requireContext())
                .setTitle("Kategória módosítása")
                .setView(getContentView())
                .setPositiveButton(R.string.ok2) { _, _ ->
                    if (isValid()) {
                        listener.onKategoriaModify(getKategoriaItem(),idx)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
    }

    private fun isValid() = kategoriaEditForm.text.isNotEmpty()

    private fun getKategoriaItem() : Kategoria {

        return Kategoria(
            id = null,
            nev = kategoriaEditForm.text.toString(),
        )
    }


    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.dialog_new_kategoria_item, null)
        kategoriaEditForm = contentView.findViewById(R.id.Kategorianev_form)
        if(katnev!==null) kategoriaEditForm.setText(katnev)
        return contentView
    }
    companion object {
        const val TAG = "NewReceptDialogFragment"
    }
}