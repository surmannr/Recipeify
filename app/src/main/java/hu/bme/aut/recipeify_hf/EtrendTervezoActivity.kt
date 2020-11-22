package hu.bme.aut.recipeify_hf

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.navigation.NavigationView
import hu.bme.aut.recipeify.data.Etkezes
import hu.bme.aut.recipeify.data.Recept
import hu.bme.aut.recipeify.database.ReceptDatabase
import hu.bme.aut.recipeify.fragments.NewReceptDialogFragment
import hu.bme.aut.recipeify_hf.adapter.EtrendAdapter
import hu.bme.aut.recipeify_hf.fragments.NewEtkezesDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class EtrendTervezoActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, EtrendAdapter.EtrendItemClickListener, NewEtkezesDialogFragment.NewEtkezesDialogListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var database: ReceptDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EtrendAdapter
    private var receptek: ArrayList<String> = ArrayList()

    lateinit var items : List<Etkezes>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_etrend_tervezo)
        val nr : NewEtkezesDialogFragment = NewEtkezesDialogFragment()

        val toolbar: Toolbar = findViewById(R.id.toolbar_etrendtervezo)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
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
        nr.recept_nevek = receptek
        fab.setOnClickListener{
            nr.show(
                supportFragmentManager,
                NewEtkezesDialogFragment.TAG
            )
        }
        requestNeededPermission()
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
            myIntent.setClass(this@EtrendTervezoActivity, FooldalActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Receptlista") {
            myIntent.setClass(this@EtrendTervezoActivity, MainActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Mitfozzekma") {
            myIntent.setClass(this@EtrendTervezoActivity, MitFozzekMaActivity::class.java)
            startActivity(myIntent)
        } else if(intent==="Etrendtervezo"){
            // Itt semmi
        } else if(intent==="Kategoriaszerkeszto"){
            myIntent.setClass(this@EtrendTervezoActivity, KategoriaSzerkesztoActivity::class.java)
            startActivity(myIntent)
        } else {
            Toast.makeText(this, "Hiba történt", Toast.LENGTH_SHORT).show()
        }

    }

    // --------- database ------------

    private fun initRecyclerView() {
        recyclerView = ReceptRecycleView
        adapter = EtrendAdapter(this)
        loadItemsInBackground()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    private fun loadItemsInBackground() {
        thread {
            items = database.etkezesDao().getAll()
            runOnUiThread {
                adapter.update(items)
            }
        }
    }
    override fun onItemChanged(item: Etkezes) {
        thread {
            database.etkezesDao().update(item)
        }
    }

    override fun onItemDelete(idx: Int) {
        thread {
            val etkezes: Etkezes = database.etkezesDao().getAll().get(idx)
            database.etkezesDao().deleteItem(etkezes)
            database.etkezesDao().update(etkezes)
            recyclerView.invalidate()
        }
    }
    override fun onItemSyncToCalendar(idx: Int) {
        try {
            val dat : Calendar = items[idx].datum
            dat.add(Calendar.MONTH,-1)
            val values = ContentValues()
            values.put(CalendarContract.Events.DTSTART, dat.timeInMillis)
            values.put(CalendarContract.Events.DTEND, items[idx].datum.timeInMillis.plus(1200000))
            values.put(CalendarContract.Events.TITLE, "Étkezés")
            values.put(CalendarContract.Events.DESCRIPTION, items[idx].recept_neve+" elfogyasztása/elkészítése.")
            values.put(CalendarContract.Events.CALENDAR_ID, 3)
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

            val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

            Log.d("URI", uri.toString())

/*
            val calendarEvent: Calendar = Calendar.getInstance()
            var inte : Intent= Intent(Intent.ACTION_INSERT)
            inte.setData(CalendarContract.Events.CONTENT_URI)
            inte.putExtra(CalendarContract.Events.CALENDAR_ID, 1)
            inte.putExtra(CalendarContract.Events.TITLE, "Vége")
            inte.putExtra(CalendarContract.Events.DESCRIPTION, "Legyen már vége az órának")
            inte.putExtra(CalendarContract.Events.DTSTART, calendarEvent.timeInMillis)
            inte.putExtra(CalendarContract.Events.DTEND, calendarEvent.timeInMillis + 60000)
            inte.putExtra(CalendarContract.Events.DURATION, System.currentTimeMillis() + 60000)
            inte.putExtra(CalendarContract.Events.RRULE, "FREQ=ONCE")
            inte.putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)


            startActivity(inte)
*/
                Toast.makeText(this, "Az étkezés szinkronizálva a naptárral!", Toast.LENGTH_SHORT)
                    .show()

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun ItemModify(idx: Int) {
        CoroutineScope(Dispatchers.Main).launch{
            val task = async(Dispatchers.IO) {
                database.etkezesDao().getAll()[idx]
            }
            var etkezes: Etkezes = task.await()
            var nr : NewEtkezesDialogFragment = NewEtkezesDialogFragment()
            receptek.add(0,etkezes.recept_neve)
            nr.recept_nevek = receptek
            nr.apply {
                arguments = Bundle().apply {
                    putString("RECEPT_NEVE", etkezes.recept_neve)
                    putLong("DATUM", etkezes.datum.timeInMillis)
                    putInt("KIVALASZTOTT_ID", idx)
                }
            }.show(
                    supportFragmentManager,
                    NewEtkezesDialogFragment.TAG
            )
        }
    }

    override fun onEtkezesItemCreated(newItem: Etkezes) {
        thread {
            val newId = database.etkezesDao().insert(newItem)
            val newEtkezesItem = newItem.copy(
                id = newId
            )
            runOnUiThread {
                adapter.addItem(newEtkezesItem)
                loadItemsInBackground()
            }
        }
    }

    override fun onEtkezesModify(item: Etkezes, kivalasztott: Int?) {
        CoroutineScope(Dispatchers.IO).launch {
            val etkezes = database.etkezesDao().getAll().get(kivalasztott!!)
            etkezes.recept_neve = item.recept_neve
            etkezes.datum = item.datum
            database.etkezesDao().update(etkezes)
        }
        adapter.updateItem(item,kivalasztott!!)
    }
    // Spinnerhez lekérjük a receptek neveit
    fun spinnerToltes() = CoroutineScope(Dispatchers.Main).launch {
        val task = async(Dispatchers.IO) {
            database.receptDao().getAll().forEach {
                receptek.add(it.nev)
            }
        }
        task.await()
    }


    // Engedélykérések

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@EtrendTervezoActivity, "Permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EtrendTervezoActivity,
                        "Permissions are NOT granted", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
    fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_CALENDAR),
                101)
        } else {
            // we are ok
        }
    }
}