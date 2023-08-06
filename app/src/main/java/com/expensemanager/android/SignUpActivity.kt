package com.expensemanager.android

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import java.util.*

class SignUpActivity : AppCompatActivity()
{
    lateinit var databaseReference: DatabaseReference
    lateinit var database: DatabaseReference
    lateinit var userName:EditText
    var isAvailable:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        userName = findViewById(R.id.signup_text_name)
        val userPass:EditText = findViewById(R.id.signup_text_password)
        val userCPass:EditText = findViewById(R.id.signup_text_confirm_password)
        val createAccount:MaterialButton = findViewById(R.id.create_account)
        val alreadyAccount:TextView = findViewById(R.id.already_account)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        createAccount.setOnClickListener {
            val name = userName.text.toString()
            val pass = userPass.text.toString()
            val cPass = userCPass.text.toString()

            if (name.isNotBlank() && name.isNotEmpty())
            {
                if(isEmailValid(name)) {
                    if (pass == cPass) {
                        userNameAvailable(name, pass)
                    } else if (pass.isEmpty()) {
                        userPass.error = "Password cannot be Empty"
                    } else {
                        userPass.error = "Password Mismatch!"
                    }
                }else{
                    userName.error = "Invalid Email"
                }
            }
            else
            {
                userName.error = "Email cannot be Empty!"
            }
        }

        alreadyAccount.setOnClickListener {
            val intent:Intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

    }

    fun pushData(uName:String,uPass:String)
    {
        val realName:String = uName
        val uName = encodeEmail(uName)
        val userModal = UserModal(uName,uPass,realName)
        databaseReference.child(uName).setValue(userModal).addOnSuccessListener {
            closeKeyBoard()
            Snackbar.make(findViewById(R.id.create_account),"Account Created Successfully",Snackbar.LENGTH_SHORT).show()
            val intent:Intent = Intent(this,LoginActivity::class.java)
            Thread {
                Thread.sleep(700)
               startActivity(intent)
            }.start()
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"Unable to create account",Toast.LENGTH_SHORT).show()
        }

    }

    private fun userNameAvailable(uName: String, uPass: String)
    {
        val data:DatabaseReference = FirebaseDatabase.getInstance().reference
        val userNameReference:DatabaseReference = data.child("Users").child(encodeEmail(uName))
        val valueEventListener = object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists())
                {
                    userName.error = null
                    pushData(uName,uPass)
                }
                else
                {
                    userName.error = "Email already taken"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        userNameReference.addListenerForSingleValueEvent(valueEventListener)
    }
    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")
        return email.matches(emailPattern)
    }

    private fun encodeEmail(email: String): String {
        return Base64.encodeToString(email.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }
}