package com.expensemanager.android

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.button.MaterialButton

class LoginOrSignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_or_sign_up)

        supportActionBar?.hide()

        val sharedPreferences:SharedPreferences = this.getSharedPreferences("my_expense_manager",
            MODE_PRIVATE)
        val isLogin:Boolean = sharedPreferences.getBoolean("successful_login",false)
        if (isLogin)
        {
            val intent:Intent = Intent(this,MainHomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(androidx.appcompat.R.anim.abc_popup_enter,0)
            finish()
        }

        val loginButton:MaterialButton = findViewById(R.id.login_button)
        val signUpButton:MaterialButton = findViewById(R.id.signup_button)

        loginButton.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}