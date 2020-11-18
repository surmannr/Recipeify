package hu.bme.aut.recipeify.dao

import androidx.room.*
import hu.bme.aut.recipeify.data.Recept

@Dao
interface ReceptDao {
    @Query("SELECT * FROM recept")
    fun getAll(): List<Recept>

    @Insert
    fun insert(item: Recept): Long

    @Update
    fun update(item: Recept)

    @Delete
    fun deleteItem(item: Recept)

    @Query("SELECT * FROM recept WHERE kedvenc = 1")
    fun getAllFavorites(): List<Recept>
}