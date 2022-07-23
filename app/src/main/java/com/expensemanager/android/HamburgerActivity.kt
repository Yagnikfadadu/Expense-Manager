package com.expensemanager.android

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class HamburgerActivity : AppCompatActivity()
{
    lateinit var uname:String
    lateinit var navigationView:NavigationView
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    lateinit var drawerLayout: DrawerLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hamburger)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        actionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#3CAE5C")))
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val sharedPreferences = this.getSharedPreferences("my_expense_manager", MODE_PRIVATE)
        uname = sharedPreferences.getString("user_name","Please Login").toString()

        navigationView.itemIconTintList = null
        navigationView.setNavigationItemSelectedListener { item->
            val id = item.itemId
            drawerLayout.closeDrawer(GravityCompat.START)
            when(id)
            {
                R.id.nav_account ->{
                    Toast.makeText(applicationContext, "Nav Account", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.nav_settings ->{
                    Toast.makeText(applicationContext, "Nav Settings", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_logout ->{
                    Toast.makeText(applicationContext, "Nav Logout", Toast.LENGTH_SHORT).show()
                    true
                }
                else ->{
                    false
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        else
        {
            super.onBackPressed()
        }
    }
}