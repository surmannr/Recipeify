package hu.bme.aut.recipeify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "recept")
data class Recept(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "nev") val nev: String,
    @ColumnInfo(name = "hozzavalok") val hozzavalok: String,
    @ColumnInfo(name = "kategoria") val kategoria: List<String>,
    @ColumnInfo(name = "kedvenc") var kedvenc: Boolean
)