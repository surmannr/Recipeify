package hu.bme.aut.recipeify.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify_hf.R

class ReceptAdapter(private val listener: ReceptItemClickListener) :
        RecyclerView.Adapter<ReceptAdapter.ReceptViewHolder>() {

    private val items = mutableListOf<Recept>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceptViewHolder {
        val itemView: View = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_recept, parent, false)
        return ReceptViewHolder(itemView)
    }

    inner class ReceptViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var nev: TextView = v.findViewById(R.id.receptnev)
        var tudnivalok: Button = v.findViewById(R.id.btn_tudnivalok)
        val btn_kedvenc: ImageButton = v.findViewById(R.id.btn_ReceptFavorite)
        val btn_remove: ImageButton = v.findViewById(R.id.btn_ReceptRemove)
        var item: Recept? = null
        init{
             btn_kedvenc.setOnClickListener(){
                 listener.onItemChangeFavorite(adapterPosition)
                 changeFav(adapterPosition,btn_kedvenc)
             }
             btn_remove.setOnClickListener(){
                 listener.onItemDelete(adapterPosition)
                 delete(adapterPosition)
             }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ReceptViewHolder, position: Int) {
        val item = items[position]
        holder.nev.text = item.nev
        if(item.kedvenc) holder.btn_kedvenc.setColorFilter(Color.RED)
        else holder.btn_kedvenc.setColorFilter(Color.DKGRAY)
        holder.item = item
        holder.tudnivalok.text = "Tudnivalók"
    }

    override fun getItemCount(): Int {
        return items.size
    }
    fun addItem(item: Recept) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun update(shoppingItems: List<Recept>) {
        items.clear()
        items.addAll(shoppingItems)
        notifyDataSetChanged()
    }

    fun changeFav(idx: Int, button: ImageButton){
        items.get(idx).kedvenc = !items.get(idx).kedvenc
        if (items.get(idx).kedvenc) {
            button.setColorFilter(Color.RED);
        } else {
            button.setColorFilter(Color.parseColor("#3d3d3d"));
        }
        notifyDataSetChanged()
    }

    fun delete(idx:Int){
        items.removeAt(idx)
        notifyItemRemoved(idx)
    }

    interface ReceptItemClickListener {
        fun onItemChanged(item: Recept)
        fun onItemChangeFavorite(idx: Int)
        fun onItemDelete(idx: Int)
    }
}