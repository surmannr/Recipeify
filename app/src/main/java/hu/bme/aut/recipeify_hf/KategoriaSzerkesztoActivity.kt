package hu.bme.aut.recipeify_hf

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.recipeify.adapter.ReceptAdapter
import hu.bme.aut.recipeify.data.Kategoria
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify.database.ReceptDatabase
import hu.bme.aut.recipeify.fragments.NewReceptDialogFragment
import hu.bme.aut.recipeify_hf.adapter.KategoriaAdapter
import hu.bme.aut.recipeify_hf.fragments.NewKategoriaDialogFragment
import kotlinx.android.synthetic.main.activity_kategoria_szerkeszto.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread

class KategoriaSzerkesztoActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, KategoriaAdapter.KategoriaItemClickListener, NewKategoriaDialogFragment.NewKategoriaDialogListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var database: ReceptDatabase
    private lateinit var adapter: KategoriaAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kategoria_szerkeszto)

        val toolbar: Toolbar = findViewById(R.id.toolbar_kategoriaszerkeszto)
        setSupportActionBar(toolbar)

        fabkategoria.setOnClickListener{
            NewKategoriaDialogFragment().show(
                supportFragmentManager,
                NewKategoriaDialogFragment.TAG
            )
        }

        drawer = findViewById(R.id.drawer_layout)

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
            R.id.nav_fooldal -> runNewActivity("Fooldal")
            R.id.nav_receptlista -> runNewActivity("Receptlista")
            R.id.nav_mitfozzekma -> runNewActivity("Mitfozzekma")
            R.id.nav_etrendtervezo -> runNewActivity("Etrendtervezo")
            R.id.nav_kategoriabeallitas -> runNewActivity("Kategoriaszerkeszto")
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
        if(intent==="Fooldal"){
            myIntent.setClass(this@KategoriaSzerkesztoActivity, FooldalActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Receptlista") {
            myIntent.setClass(this@KategoriaSzerkesztoActivity, MainActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Mitfozzekma") {
            myIntent.setClass(this@KategoriaSzerkesztoActivity, MitFozzekMaActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Etrendtervezo"){
            myIntent.setClass(this@KategoriaSzerkesztoActivity, EtrendTervezoActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Kategoriaszerkeszto"){
            // Itt semmi
        } else {
            Toast.makeText(this, "Hiba történt", Toast.LENGTH_SHORT).show()
        }

    }

    // -------- adatbázis -------------

    private fun initRecyclerView() {
        recyclerView = KategoriaRecycleView
        adapter = KategoriaAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadItemsInBackground() {
        thread {
            val items = database.kategoriaDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }
    override fun onItemChanged(item: Kategoria) {
        thread {
            database.kategoriaDao().update(item)
        }
    }

    override fun onItemDelete(idx: Int) {
        thread {
            val kategoria: Kategoria = database.kategoriaDao().getAll().get(idx)
            database.kategoriaDao().deleteItem(kategoria)
            database.kategoriaDao().update(kategoria)
            recyclerView.invalidate()
        }
    }

    override fun onKategoriaItemCreated(newItem: Kategoria) {
        thread {
            val newId = database.kategoriaDao().insert(newItem)
            val newKategoriaItem = newItem.copy(
                id = newId
            )
            runOnUiThread {
                adapter.addItem(newKategoriaItem)
            }
        }
    }

}