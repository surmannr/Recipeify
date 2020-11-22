package hu.bme.aut.recipeify_hf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify_hf.R
import hu.bme.aut.recipeify_hf.fragments.NewKategoriaDialogFragment


class KategoriaAdapter(private val listener: KategoriaItemClickListener) :
    RecyclerView.Adapter<KategoriaAdapter.KategoriaViewHolder>() {

    private val items = mutableListOf<Kategoria>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriaViewHolder {
        val itemView: View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_kategoria, parent, false)
        return KategoriaViewHolder(itemView)
    }

    inner class KategoriaViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var nev: TextView = v.findViewById(R.id.kategorianev)
        var removebtn: ImageButton = v.findViewById(R.id.btn_KategoriaRemove)
        var editbtn: ImageButton = v.findViewById(R.id.btn_kategoriaEdit)
        var item: Kategoria? = null

        init {
            removebtn.setOnClickListener(){
                listener.onItemDelete(adapterPosition)
                delete(adapterPosition)
            }
            editbtn.setOnClickListener{
                listener.ItemModify(adapterPosition)

            }
        }

    }

    override fun onBindViewHolder(holder: KategoriaViewHolder, position: Int) {
        val item = items[position]
        holder.nev.text = item.nev
        holder.item = item
    }

    override fun getItemCount(): Int {
        return items.size
    }
    fun addItem(item: Kategoria) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(shoppingItems: List<Kategoria>) {
        items.clear()
        items.addAll(shoppingItems)
        notifyDataSetChanged()
    }

    fun delete(idx:Int){
        items.removeAt(idx)
        notifyItemRemoved(idx)
    }

    fun updateItem(item: Kategoria, idx: Int){
        items[idx] = item
        notifyDataSetChanged()
    }

    interface KategoriaItemClickListener {
        fun onItemChanged(item: Kategoria)
        fun onItemDelete(idx: Int)
        fun ItemModify(idx: Int)
    }

}