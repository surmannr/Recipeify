package hu.bme.aut.recipeify_hf.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isNotEmpty
import androidx.fragment.app.DialogFragment
import hu.bme.aut.recipeify.data.Etkezes
import hu.bme.aut.recipeify_hf.R
import java.util.*
import kotlin.collections.ArrayList


class NewEtkezesDialogFragment : DialogFragment() {

    lateinit var spinner_receptnev: Spinner
    lateinit var datepicker: DatePicker
    lateinit var timepicker: TimePicker

    private lateinit var _context: Context

    private var idx: Int? = null
    private var receptneve: String? = null
    private var datum: Calendar? = null

    var recept_nevek: ArrayList<String> = ArrayList()

    interface NewEtkezesDialogListener {
        fun onEtkezesItemCreated(newItem: Etkezes)
        fun onEtkezesModify(item: Etkezes, kivalasztott: Int?)
    }

    private lateinit var listener: NewEtkezesDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        _context = context

        arguments?.getInt("KIVALASZTOTT_ID")?.let {
            idx = it
        }
        arguments?.getString("RECEPT_NEVE")?.let {
            receptneve = it
        }
        arguments?.getLong("DATUM")?.let {
            datum = Calendar.getInstance()
            datum?.timeInMillis = it
            datum?.add(Calendar.MONTH,-3)
        }

        listener = context as? NewEtkezesDialogListener
            ?: throw RuntimeException("Activity must implement the NewEtkezesDialogListener interface!")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if(receptneve===null) {
            return AlertDialog.Builder(requireContext())
                    .setTitle(R.string.new_etkezes_item)
                    .setView(getContentView())
                    .setPositiveButton(R.string.ok) { _, _ ->
                        if (isValid()) {
                            listener.onEtkezesItemCreated(getEtkezesItem())
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
        }
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_etkezes_item2)
                .setView(getContentView())
                .setPositiveButton(R.string.ok2) { _, _ ->
                    if (isValid()) {
                        listener.onEtkezesModify(getEtkezesItem(),idx)
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
    }

    private fun isValid() = spinner_receptnev.selectedItem.toString().isNotEmpty() && datepicker.isNotEmpty()

    private fun getEtkezesItem() : Etkezes{
        var year : Int = datepicker.year
        var month: Int = datepicker.month+1
        var day : Int = datepicker.dayOfMonth

        var hour: Int = timepicker.hour
        var min: Int = timepicker.minute

        var calendar: Calendar = Calendar.getInstance()

        calendar.set(year, month, day, hour, min)

        //Log.d("TESZT", "Datum: ev/ho/nap: " + year + " " + month + " " + day + " " + hour + " " + min)
        //Log.d("TESZT2", "Datum: ev/ho/nap: " + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.MONTH) + " " + calendar.get(Calendar.DAY_OF_MONTH))
        return Etkezes(
                id = null,
                recept_neve = spinner_receptnev.selectedItem.toString(),
                datum = calendar
        )
    }


    private fun getContentView(): View {
        val contentView =
            LayoutInflater.from(context).inflate(R.layout.dialog_new_etkezes_item, null)
            spinner_receptnev = contentView.findViewById(R.id.spinner_receptek)
            datepicker = contentView.findViewById(R.id.datum_valasztas)
            if(datum!==null) {
                datepicker.init(datum!!.get(Calendar.YEAR),datum!!.get(Calendar.MONTH)+2,datum!!.get(Calendar.DAY_OF_MONTH), null)
            }
            timepicker = contentView.findViewById(R.id.ido_valasztas)
        if(datum!==null) {
            timepicker.hour = datum!!.get(Calendar.HOUR_OF_DAY)
            timepicker.minute = datum!!.get(Calendar.MINUTE)
        }
            timepicker.setIs24HourView(true);

            spinner_receptnev.adapter = ArrayAdapter(
                    _context,
                    R.layout.support_simple_spinner_dropdown_item,
                    recept_nevek
            )
            return contentView
    }

    override fun onStop() {
        super.onStop()
        if(receptneve!==null) recept_nevek.removeAt(0)
    }
    companion object {
        const val TAG = "NewEtkezesDialogFragment"
    }
}