package hu.bme.aut.recipeify.database

import android.net.Uri
import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept
import java.util.*
import kotlin.collections.ArrayList

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? = value?.let { value ->
        GregorianCalendar().also { calendar ->
            calendar.timeInMillis = value
        }
    }

    @TypeConverter
    fun toTimestamp(timestamp: Calendar?): Long? = timestamp?.timeInMillis

    @TypeConverter
    fun imageToString(uri: Uri?): String? = uri.toString()

    @TypeConverter
    fun stringToImage(uriString: String?) : Uri?{
        if (uriString != null)return Uri.parse(uriString)
        return null
    }

    @TypeConverter
    fun toRecept(gsonObj:String): Recept?{
        if(gsonObj==="") return (null);
        val recept = Gson().fromJson(gsonObj,Recept::class.java);
        return recept;
    }
    @TypeConverter
    fun getRecept(rec : Recept?) : String{
        if(rec===null){
            return "";
        }
        val jsonString : String = Gson().toJson(rec);
        return jsonString;
    }
    @TypeConverter
    fun toKategoria(jsonObj:String): ArrayList<String>?{
        if(jsonObj==="") return (null);
        val sType = object : TypeToken<ArrayList<String>>() {}.type;
        val kategoria = Gson().fromJson<ArrayList<String>>(jsonObj,sType)
        return kategoria;
    }
    @TypeConverter
    fun getKategoria(kat : ArrayList<String>?) : String{
        if(kat===null){
            return "";
        }
        val jsonString : String = Gson().toJson(kat);
        return jsonString;
    }
}
