package hu.bme.aut.recipeify_hf

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify.database.ReceptDatabase
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.random.Random

class MitFozzekMaActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var spinner: Spinner

    private lateinit var database: ReceptDatabase

    private lateinit var button: ImageButton
    private lateinit var result: TextView

    private var items: ArrayList<Recept> = ArrayList()
    private var resulstString: String? = String()

    var kategoriak: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mit_fozzek_ma)

        val toolbar: Toolbar = findViewById(R.id.toolbar_mitfozzekma)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        button = findViewById(R.id.btn_random)
        result = findViewById(R.id.result)

        spinner = findViewById(R.id.spinner_kategoriak)

        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        database = Room.databaseBuilder(
            applicationContext,
            ReceptDatabase::class.java,
            "recept-list"
        ).fallbackToDestructiveMigration().build()
        loadData()
        button.setOnClickListener(){
            if(items.isNotEmpty()){
                resulstString = items.random().nev.toString()
                result.text = resulstString
            }
            else{
                result.text = getString(R.string.no_recept_kategoria)
            }
        }
        spinnerToltes()
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(kategoriak.get(position)!==getString(R.string.nincs_szures))
                    thread {
                        val itemsById = database.receptDao().getAll().filter { recept -> recept.kategoria.contains(kategoriak.get(position)) }
                        runOnUiThread {
                            items = itemsById as ArrayList<Recept>
                        }
                    } else{
                    loadItemsInBackground()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // üres
            }
        }
    }
    // -------- nav drawer ----------
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_fooldal -> runNewActivity(getString(R.string.nav_fooldal))
            R.id.nav_receptlista -> runNewActivity(getString(R.string.nav_receptlista))
            R.id.nav_mitfozzekma -> runNewActivity(getString(R.string.nav_mitfozzekma))
            R.id.nav_etrendtervezo -> runNewActivity(getString(R.string.nav_etrendtervezo))
            R.id.nav_kategoriabeallitas -> runNewActivity(getString(R.string.nav_kategoriaszerkeszto))
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    // Intentkezelés
    fun runNewActivity(intent: String){
        val myIntent: Intent = Intent()
        if(intent===getString(R.string.nav_fooldal)){
            myIntent.setClass(this@MitFozzekMaActivity, FooldalActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_receptlista)) {
            myIntent.setClass(this@MitFozzekMaActivity, MainActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_mitfozzekma)) {
           // Itt semmi
        } else if(intent===getString(R.string.nav_etrendtervezo)){
            myIntent.setClass(this@MitFozzekMaActivity, EtrendTervezoActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_kategoriaszerkeszto)){
            myIntent.setClass(this@MitFozzekMaActivity, KategoriaSzerkesztoActivity::class.java)
            startActivity(myIntent)
        } else {
            Toast.makeText(this, "Hiba történt", Toast.LENGTH_SHORT).show()
        }

    }

    fun loadData() = CoroutineScope(Dispatchers.Main).launch {
        val task = async(Dispatchers.IO) {
            database.receptDao().getAll()
        }
        items = task.await() as ArrayList<Recept>
    }

    fun spinnerToltes() = CoroutineScope(Dispatchers.Main).launch {
        val task = async(Dispatchers.IO) {
            database.kategoriaDao().getAll().forEach {
                kategoriak.add(it.nev)
            }
            kategoriak.add(0,getString(R.string.nincs_szures))
        }
        task.await()
        spinner.adapter = ArrayAdapter(applicationContext,
            android.R.layout.simple_spinner_dropdown_item, kategoriak)
    }

    private fun loadItemsInBackground() {
        thread {
            val items_list = database.receptDao().getAll()
            runOnUiThread {
                items = items_list as ArrayList<Recept>
            }
        }
    }

}