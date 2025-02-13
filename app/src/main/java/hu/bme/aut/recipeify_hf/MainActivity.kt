package hu.bme.aut.recipeify_hf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import co.dift.ui.SwipeToAction
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.recipeify.adapter.ReceptAdapter
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify.database.ReceptDatabase
import hu.bme.aut.recipeify.fragments.NewReceptDialogFragment
import hu.bme.aut.recipeify_hf.fragments.NewKategoriaDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    ReceptAdapter.ReceptItemClickListener, NewReceptDialogFragment.NewReceptDialogListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var spinner: Spinner

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceptAdapter
    private lateinit var database: ReceptDatabase

    var kategoriak: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val nr : NewReceptDialogFragment = NewReceptDialogFragment()
        nr.setDataKategoriak(kategoriak)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
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
        initRecyclerView()

        spinnerToltes()
        fab.setOnClickListener{
            nr.setDataKategoriak(kategoriak)
            nr.show(
                    supportFragmentManager,
                    NewReceptDialogFragment.TAG
            )
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(kategoriak.get(position)!==getString(R.string.nincs_szures))
                thread {
                    val items = database.receptDao().getAll().filter { recept -> recept.kategoria.contains(kategoriak.get(position)) }
                    runOnUiThread {
                        adapter.update(items)
                    }
                } else{
                    loadItemsInBackground()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nem kell semmit csinálnia
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

    // -------- database -----------

    private fun initRecyclerView() {
        recyclerView = ReceptRecycleView
        adapter = ReceptAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.receptDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }
    override fun onItemChanged(item: Recept) {
        thread {
            database.receptDao().update(item)
        }
    }

    override fun onItemChangeFavorite(idx: Int) {
        thread {
            val recept: Recept = database.receptDao().getAll().get(idx)
            recept.kedvenc = !recept.kedvenc;
            database.receptDao().update(recept)
            recyclerView.invalidate()
        }
    }

    override fun onItemDelete(idx: Int) {
        thread {
            val recept: Recept = database.receptDao().getAll().get(idx)
            database.receptDao().deleteItem(recept)
            database.receptDao().update(recept)
            recyclerView.invalidate()
        }
    }

    override fun ItemModify(idx: Int) {
        CoroutineScope(Dispatchers.Main).launch{
            val task = async(Dispatchers.IO) {
                database.receptDao().getAll()[idx]
            }
            var recept: Recept = task.await()
            var nr : NewReceptDialogFragment = NewReceptDialogFragment()
            nr.setDataKategoriak(kategoriak)
            nr.apply {
                arguments = Bundle().apply {
                    putString("RECEPT_NEV", recept.nev)
                    putString("RECEPT_HOZZAVALOK", recept.hozzavalok)
                    putStringArrayList("RECEPT_KATEGORIAK", recept.kategoria)
                    putString("RECEPT_URISTRING", recept.kep.toString())
                    putInt("KIVALASZTOTT_ID", idx)
                }
            }.show(
                    supportFragmentManager,
                    NewReceptDialogFragment.TAG
            )
        }
    }

    override fun onReceptItemCreated(newItem: Recept) {
        thread {
            val newId = database.receptDao().insert(newItem)
            val newReceptItem = newItem.copy(
                id = newId
            )
            runOnUiThread {
                adapter.addItem(newReceptItem)
            }
        }
    }

    override fun onReceptModify(item: Recept, kivalasztott: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            val recept = database.receptDao().getAll().get(kivalasztott!!)
            recept.nev = item.nev
            recept.hozzavalok = item.hozzavalok
            recept.kategoria = item.kategoria
            recept.kep = item.kep
            database.receptDao().update(recept)
        }
        adapter.updateItem(item,kivalasztott!!)
    }

    // Intentkezelés
    fun runNewActivity(intent: String){
        val myIntent: Intent = Intent()
        if(intent===getString(R.string.nav_fooldal)){
            myIntent.setClass(this@MainActivity, FooldalActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_receptlista)) {
            // Itt semmi
        } else if(intent===getString(R.string.nav_mitfozzekma)) {
            myIntent.setClass(this@MainActivity, MitFozzekMaActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_etrendtervezo)){
            myIntent.setClass(this@MainActivity, EtrendTervezoActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_kategoriaszerkeszto)){
            myIntent.setClass(this@MainActivity, KategoriaSzerkesztoActivity::class.java)
            startActivity(myIntent)
        } else {
            Toast.makeText(this, "Hiba történt", Toast.LENGTH_SHORT).show()
        }

    }

    // szálkezelés
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

}