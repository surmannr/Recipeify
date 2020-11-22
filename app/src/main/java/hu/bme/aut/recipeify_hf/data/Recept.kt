package hu.bme.aut.recipeify.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "recept")
data class Recept(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "nev") var nev: String,
    @ColumnInfo(name = "hozzavalok") var hozzavalok: String,
    @ColumnInfo(name = "kategoria") var kategoria: ArrayList<String>,
    @ColumnInfo(name = "kedvenc") var kedvenc: Boolean,
    @ColumnInfo(name = "kep") var kep: Uri?
)