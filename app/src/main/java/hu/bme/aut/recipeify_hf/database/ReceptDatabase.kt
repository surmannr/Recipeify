package hu.bme.aut.recipeify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bme.aut.recipeify.dao.EtkezesDao
import hu.bme.aut.recipeify.dao.KategoriaDao
import hu.bme.aut.recipeify.dao.ReceptDao
import hu.bme.aut.recipeify.data.Etkezes
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept

@TypeConverters(Converters::class)
@Database(entities = [Recept::class, Kategoria::class, Etkezes::class], version = 5,exportSchema = false)
abstract class ReceptDatabase : RoomDatabase() {
    abstract fun receptDao(): ReceptDao
    abstract fun kategoriaDao(): KategoriaDao
    abstract fun etkezesDao(): EtkezesDao

    companion object {
        private var INSTANCE: ReceptDatabase? = null

        fun getInstance(context: Context): ReceptDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    ReceptDatabase::class.java, "recept.db")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}