package com.expensemanager.android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity()
{
    lateinit var userName:EditText
    lateinit var forgetPassword:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()

        userName = findViewById(R.id.login_text_name)
        val userPass:EditText = findViewById(R.id.login_text_password)
        val loginButton:MaterialButton = findViewById(R.id.login_proceed)
        val createNew:TextView = findViewById(R.id.create_new_account)

        forgetPassword = findViewById(R.id.forget_password);

        createNew.setOnClickListener {
            val intent:Intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val name = userName.text.toString()
            val pass = userPass.text.toString()

            if (name.isNotBlank())
            {
                if (pass.isNotBlank())
                {
                    checkCredentials(name,pass)
                }
                else
                {
                    userPass.error = "Password cannot be empty"
                }
            }
            else
            {
                userName.error = "Email cannot be empty"
            }
        }

        forgetPassword.setOnClickListener {
            val intent:Intent = Intent(this,ForgetPassActivity::class.java)
            intent.putExtra("email",userName.text.toString())
            startActivity(intent)
        }

    }
    private fun checkCredentials(name:String, pass:String)
    {
        val data: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        val userNameReferencePass: DatabaseReference = data.child("Users").child(encodeEmail(name)).child("pass")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value==pass)
                {
                    userName.error = null
                    Snackbar.make(findViewById(R.id.login_proceed),"Login Successful", Snackbar.LENGTH_SHORT).show()
                    val sharedPreferences:SharedPreferences = applicationContext.getSharedPreferences("my_expense_manager",
                        MODE_PRIVATE)
                    val sharedPreferencesEditor:SharedPreferences.Editor = sharedPreferences.edit()
                    sharedPreferencesEditor.putBoolean("successful_login",true)
                    sharedPreferencesEditor.putString("user_name",encodeEmail(name))
                    sharedPreferencesEditor.apply()

                    val intent:Intent = Intent(applicationContext,MainHomeActivity::class.java)
                    startActivity(intent)
                }
                else
                {
                    userName.error = "Incorrect Username or Password"
                    Snackbar.make(findViewById(R.id.login_proceed),"Incorrect Username or Password",Snackbar.LENGTH_SHORT).show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        userNameReferencePass.addListenerForSingleValueEvent(valueEventListener)
    }
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    private fun encodeEmail(email: String): String {
        return Base64.encodeToString(email.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }
}