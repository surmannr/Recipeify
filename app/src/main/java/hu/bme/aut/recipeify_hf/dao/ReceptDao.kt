package hu.bme.aut.recipeify.dao

import android.net.Uri
import androidx.room.*
import hu.bme.aut.recipeify.data.Recept

@Dao
interface ReceptDao {
    @Query("SELECT * FROM recept")
    fun getAll(): List<Recept>

    @Insert
    fun insert(item: Recept): Long

    @Update(entity = Recept::class,onConflict = OnConflictStrategy.REPLACE)
    fun update(item: Recept)

    @Delete
    fun deleteItem(item: Recept)

    @Query("SELECT * FROM recept WHERE kedvenc = 1")
    fun getAllFavorites(): List<Recept>

    @Query("update recept set nev=:nev where id=:idx")
    fun updateItem(idx: Int, nev: String)
}