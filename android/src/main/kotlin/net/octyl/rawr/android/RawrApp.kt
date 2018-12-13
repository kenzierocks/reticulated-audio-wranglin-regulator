package net.octyl.rawr.android

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import net.octyl.rawr.android.frags.PlaylistFragment

class RawrApp : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        drawerLayout = findViewById(R.id.root)

        supportActionBar!!.configureRawrApp()
    }

    private fun ActionBar.configureRawrApp() {
        // who needs a title here? not us
        title = ""

        setDisplayHomeAsUpEnabled(true)
        val menuIcon = getDrawable(R.drawable.ic_menu)
        menuIcon.setTint(getColor(R.color.menu))
        setHomeAsUpIndicator(menuIcon)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.tracks -> {
                val frag = PlaylistFragment()
                val opensaction = supportFragmentManager.beginTransaction()
                opensaction.add(R.id.main_frag_container, frag)
                opensaction.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
