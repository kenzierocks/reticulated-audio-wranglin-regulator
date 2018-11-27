package net.octyl.rawr.android

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity

class RawrApp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

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



}
