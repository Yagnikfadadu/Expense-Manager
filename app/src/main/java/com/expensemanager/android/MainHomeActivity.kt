package com.expensemanager.android

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class MainHomeActivity : AppCompatActivity()
{
    lateinit var transactionArrayList: ArrayList<ExpenseTransactionModal>
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    lateinit var name:String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceFetcher: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_home)

        val sharedPreferences = this.getSharedPreferences("my_expense_manager", MODE_PRIVATE)
        name = sharedPreferences.getString("user_name","Please Login").toString()
        supportActionBar?.title = "Pocket Manager"
        supportActionBar?.setDisplayShowTitleEnabled(true)

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(name).child("transactions")
        databaseReferenceFetcher = FirebaseDatabase.getInstance().getReference("Users").child(name).child("transactions")

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tab_layout)


        val customFragmentAdapter = CustomFragmentAdapter(supportFragmentManager)
        customFragmentAdapter.addFragment(TransactionsFragment(),"Records")
        customFragmentAdapter.addFragment(AnalysisFragment(),"Detailed Analysis")

        viewPager.adapter = customFragmentAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    fun pushTransaction(amount:String, note:String, type:String, mode:String, date:String)
    {
        val expenseTransactionModal = ExpenseTransactionModal(amount,note,type,mode,date)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatted = current.format(formatter)
        val userDateTime = "$date $formatted"
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val date: Date = sdf.parse(userDateTime) as Date
        val millis: Long = date.time

        databaseReference.child("$millis").setValue(expenseTransactionModal).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"Failed to Proceed now!",Toast.LENGTH_SHORT).show()
        }
    }

}