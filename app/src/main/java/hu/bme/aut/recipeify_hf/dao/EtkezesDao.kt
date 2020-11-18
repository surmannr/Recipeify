package hu.bme.aut.recipeify.dao

import androidx.room.*
import hu.bme.aut.recipeify.data.Etkezes

@Dao
interface EtkezesDao {
    @Query("SELECT * FROM etkezes")
    fun getAll(): List<Etkezes>

    @Query("SELECT * FROM etkezes WHERE id LIKE :idx")
    fun getByID(idx: Long): Etkezes

    @Insert
    fun insert(item: Etkezes): Long

    @Update
    fun update(item: Etkezes)

    @Delete
    fun deleteItem(item: Etkezes)
}