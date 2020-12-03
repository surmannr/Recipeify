package hu.bme.aut.recipeify_hf.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.marginStart
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.recipeify.data.Etkezes
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify_hf.R
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class EtrendAdapter(private val listener: EtrendItemClickListener) :
        RecyclerView.Adapter<EtrendAdapter.EtrendViewHolder>() {

    private val items = mutableListOf<Etkezes>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtrendViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_etrend, parent, false)
        return EtrendViewHolder(itemView)
    }

    inner class EtrendViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var receptnev: TextView = v.findViewById(R.id.receptnev)
        var removebtn: ImageButton = v.findViewById(R.id.btn_EtkezesRemove)
        var syncbtn: ImageButton = v.findViewById(R.id.btn_EtkezesSync)
        var editbtn: ImageButton = v.findViewById(R.id.btn_EtkezesEdit)
        var datum: TextView = v.findViewById(R.id.etrend_datum)
        var item: Etkezes? = null

        init {
            removebtn.setOnClickListener(){
                listener.onItemDelete(adapterPosition)
                delete(adapterPosition)
            }
            syncbtn.setOnClickListener{
                listener.onItemSyncToCalendar(adapterPosition)
            }
            editbtn.setOnClickListener {
                listener.ItemModify(adapterPosition)
            }
        }
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: EtrendViewHolder, position: Int) {
        val formatter: SimpleDateFormat = SimpleDateFormat("yyyy.MM.dd. HH:mm")
        val item = items[position]
        val c : Calendar = Calendar.getInstance()
        holder.receptnev.text = item.recept_neve
        if(item.datum !== null)
            c.add(Calendar.DATE, -1)
                   item.datum.add(Calendar.MONTH, -1)
            if(c.time.before(item.datum.time)){
                holder.datum.text = formatter.format(item.datum.time)
            } else {
                holder.datum.text = formatter.format(item.datum.time)+ " lejárt!"
                holder.datum.setTextColor(Color.RED)
            }
                 item.datum.add(Calendar.MONTH, 1)

        holder.item = item
    }

    override fun getItemCount(): Int {
        return items.size
    }
    fun addItem(item: Etkezes) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(Items: List<Etkezes>) {
        items.clear()
        items.addAll(Items)
        notifyDataSetChanged()
    }

    fun delete(idx: Int){
        items.removeAt(idx)
        notifyItemRemoved(idx)
    }

    fun updateItem(item: Etkezes, idx: Int){
        items.removeAt(idx)
        notifyItemRemoved(idx)
        items.add(idx,item)
        notifyDataSetChanged()
    }

    interface EtrendItemClickListener {
        fun onItemChanged(item: Etkezes)
        fun onItemDelete(idx: Int)
        fun onItemSyncToCalendar(idx: Int)
        fun ItemModify(idx: Int)
    }

}