package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login_popup.*
import kotlinx.android.synthetic.main.settings_menu.*

class SettingsMenu : AppCompatActivity() {

    var userId = intent.getStringExtra("userId")
    var householdId = intent.getStringExtra("userId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_menu)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = ""
        }
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        logout()
        setNames()

    }

    fun logout() {
        val logout = findViewById<Button>(R.id.bt_logout)
        logout.setOnClickListener {
            userId = ""
            householdId = ""
            intent = Intent(this, MainActivity::class.java).also {
                it.putExtra("userId", userId)
                it.putExtra("householdId", householdId)
                startActivity(it)
            }
        }
    }

    fun setNames(){
        tv_UserIdDrawer.text = userId
        HouseholdIdDrawer.text = householdId

    }
}