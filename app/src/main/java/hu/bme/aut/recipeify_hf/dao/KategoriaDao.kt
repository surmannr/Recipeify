package hu.bme.aut.recipeify.dao

import androidx.room.*
import hu.bme.aut.recipeify.data.Kategoria

@Dao
interface KategoriaDao {
    @Query("SELECT * FROM kategoria")
    fun getAll(): List<Kategoria>

    @Insert
    fun insert(item: Kategoria): Long

    @Update
    fun update(item: Kategoria)

    @Delete
    fun deleteItem(item: Kategoria)
}