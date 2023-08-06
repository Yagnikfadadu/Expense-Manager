package com.expensemanager.android

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

class ForgetPassActivity : AppCompatActivity() {

    lateinit var response: String
    private lateinit var requestQueue: RequestQueue
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceFetcher: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_pass)

        supportActionBar?.hide()

        val email = intent.getStringExtra("email")
        requestQueue = Volley.newRequestQueue(this)

        databaseReference = FirebaseDatabase.getInstance().reference.child("Users")


        var emailEditText:EditText = findViewById(R.id.fp_email)
        val buttonOTP:MaterialButton = findViewById(R.id.fp_button_get_OTP)

        val otpEditText:EditText = findViewById(R.id.fp_OTP)
        val buttonValidate:MaterialButton = findViewById(R.id.fp_button_validate_OTP)

        val passwordEditText:EditText = findViewById(R.id.fp_text_password)
        val buttonReset:MaterialButton = findViewById(R.id.fp_button_reset_password)

        emailEditText.setText(email)
        response = ""

        buttonOTP.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty() && isEmailValid(emailEditText.text.toString())) {
                val customRetryPolicy = object : DefaultRetryPolicy(
                    5000, // Timeout in milliseconds
                    DEFAULT_MAX_RETRIES,
                    DEFAULT_BACKOFF_MULT
                ) {
                    override fun retry(error: VolleyError) {
                        if (error is TimeoutError) {
                            // Delay before retrying
                            Thread.sleep(1000) // 1000 milliseconds = 1 second
                        }
                        super.retry(error)
                    }
                }

                val url =
                    "https://authentication-management-api.yagnikpatel.repl.co/?recipient=" + emailEditText.text.toString() + "&app=Expense%20Manager"
                val stringRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        val jsonObject = JSONObject(response)
                        val authenticationCode = jsonObject.getString("authenticationCode")
                        this.response = authenticationCode
                    },
                    { _ ->
                        // Handle error
                    })

                stringRequest.retryPolicy = customRetryPolicy
                requestQueue.add(stringRequest)

                otpEditText.visibility = View.VISIBLE
                buttonValidate.visibility = View.VISIBLE
            }else{
                emailEditText.error = "Invalid Email"
            }
        }

        buttonValidate.setOnClickListener {
            if (response!="") {
                if (isValidOTP(otpEditText.text.toString(), response)) {
                    passwordEditText.visibility = View.VISIBLE
                    buttonReset.visibility = View.VISIBLE

                    emailEditText.visibility = View.GONE
                    buttonOTP.visibility = View.GONE
                    otpEditText.visibility = View.GONE
                    otpEditText.setText("")
                    emailEditText.hint = ""
                    emailEditText.visibility = View.GONE
                    emailEditText.setHintTextColor(Color.WHITE)
                    buttonValidate.visibility = View.GONE
                }else{
                    otpEditText.error = "Invalid OTP"
                }
            }
        }

        buttonReset.setOnClickListener {
            if (passwordEditText.text.toString().isNotEmpty()){
                val data:DatabaseReference = FirebaseDatabase.getInstance().reference
                val userNameReference:DatabaseReference = data.child("Users").child(encodeEmail(emailEditText.text.toString()))
                val valueEventListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists())
                        {
                            Snackbar.make(emailEditText,"No account found",Snackbar.LENGTH_SHORT).show()
                        }
                        else
                        {
                            changePassword(emailEditText.text.toString(),passwordEditText.text.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                }
                userNameReference.addListenerForSingleValueEvent(valueEventListener)

            }
        }

    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = Regex("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")
        return email.matches(emailPattern)
    }

    private fun isValidOTP(otp:String, response:String):Boolean{
        return calculateMD5(otp) == (response)
    }

    private fun calculateMD5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(input.toByteArray())

        val hexString = StringBuilder()
        for (byte in messageDigest) {
            hexString.append(String.format("%02x", byte))
        }

        return hexString.toString()
    }

    private fun changePassword(user:String, pass:String){
        val valueReference = databaseReference.child(encodeEmail(user)+"/pass")
        valueReference.setValue(pass)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Password Changed !", Toast.LENGTH_SHORT).show()
                val intent:Intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Try again later !", Toast.LENGTH_SHORT).show()
            }
    }

    private fun encodeEmail(email: String): String {
        return Base64.encodeToString(email.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }
}