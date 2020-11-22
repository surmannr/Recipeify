package hu.bme.aut.recipeify.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.*

@Entity(tableName = "etkezes")
data class Etkezes(
    @ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "recept") val recept_neve: String,
    @ColumnInfo(name = "datum") val datum: Calendar
)