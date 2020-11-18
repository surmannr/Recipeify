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

    private lateinit var _context: Context
    interface NewKategoriaDialogListener {
        fun onKategoriaItemCreated(newItem: Kategoria)
    }

    private lateinit var listener: NewKategoriaDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context
        listener = context as? NewKategoriaDialogListener
            ?: throw RuntimeException("Activity must implement the NewReceptDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

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
        return contentView
    }
    companion object {
        const val TAG = "NewReceptDialogFragment"
    }
}