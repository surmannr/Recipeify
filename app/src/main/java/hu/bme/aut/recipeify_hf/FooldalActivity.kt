package hu.bme.aut.recipeify_hf

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.recipeify.adapter.ReceptAdapter
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify.database.ReceptDatabase
import hu.bme.aut.recipeify.fragments.NewReceptDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class FooldalActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    ReceptAdapter.ReceptItemClickListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReceptAdapter
    private lateinit var database: ReceptDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fooldal)

        val toolbar: Toolbar = findViewById(R.id.toolbar_fooldal)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        database = Room.databaseBuilder(
            applicationContext,
            ReceptDatabase::class.java,
            "recept-list"
        ).fallbackToDestructiveMigration().build()
        initRecyclerView()
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
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
            val items = database.receptDao().getAllFavorites()
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
            val recept: Recept = database.receptDao().getAllFavorites().get(idx)
            recept.kedvenc = !recept.kedvenc;
            database.receptDao().update(recept)
            loadItemsInBackground()
            recyclerView.invalidate()
        }
    }

    override fun onItemDelete(idx: Int) {
        thread {
            val recept: Recept = database.receptDao().getAllFavorites().get(idx)
            database.receptDao().deleteItem(recept)
            database.receptDao().update(recept)
            recyclerView.invalidate()
        }
    }

    override fun ItemModify(idx: Int) {
        Toast.makeText(applicationContext,"Csak a Receptlista nézeten szerkeszthető!", Toast.LENGTH_LONG).show()
        runNewActivity(getString(R.string.nav_receptlista))
    }

    // Intentkezelés
    fun runNewActivity(intent: String){
        val myIntent: Intent = Intent()
        if(intent===getString(R.string.nav_fooldal)){
            // itt semmit
        } else if(intent===getString(R.string.nav_receptlista)) {
            myIntent.setClass(this@FooldalActivity, MainActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_mitfozzekma)) {
            myIntent.setClass(this@FooldalActivity, MitFozzekMaActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_etrendtervezo)){
            myIntent.setClass(this@FooldalActivity, EtrendTervezoActivity::class.java)
            startActivity(myIntent)
        } else if(intent===getString(R.string.nav_kategoriaszerkeszto)){
            myIntent.setClass(this@FooldalActivity, KategoriaSzerkesztoActivity::class.java)
            startActivity(myIntent)
        } else {
            Toast.makeText(this, "Hiba történt", Toast.LENGTH_SHORT).show()
        }

    }
}