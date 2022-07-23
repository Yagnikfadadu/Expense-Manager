package com.expensemanager.android

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RecyclerActivity : AppCompatActivity()
{
    lateinit var firebaseReference: DatabaseReference
    lateinit var recyclerView: RecyclerView
    lateinit var transactionArrayList: ArrayList<TransactionDataClass>
    lateinit var transactionReferenceId : ArrayList<String>
    lateinit var transactionsAdapter:TransactionsAdapter
    lateinit var name:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        supportActionBar?.title = "All Transactions"
        supportActionBar?.setDisplayShowTitleEnabled(true)

        transactionReferenceId = arrayListOf<String>()
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        transactionArrayList = arrayListOf<TransactionDataClass>()

        val sharedPreferences = this.getSharedPreferences("my_expense_manager", MODE_PRIVATE)
        name = sharedPreferences.getString("user_name","Please Login").toString()

        initializeRecyclerView()
        transactionsAdapter = TransactionsAdapter(transactionArrayList)
        recyclerView.adapter = transactionsAdapter

        val swipeToUpdate = object :SwipeToUpdate(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
            {
                when(direction)
                {
                    ItemTouchHelper.LEFT -> {
                        val position = viewHolder.absoluteAdapterPosition
                        viewHolder.setIsRecyclable(true)
                        deleteChildFromTransactionId(transactionReferenceId[position])
                        Snackbar.make(recyclerView,"Transaction Removed",Snackbar.LENGTH_SHORT).show()
                    }
                    ItemTouchHelper.RIGHT -> {
                        val position = viewHolder.absoluteAdapterPosition
                        viewHolder.setIsRecyclable(true)
                        showCustomBottomDialog(transactionReferenceId[position],position)
                    }
                }

            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeToUpdate)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showCustomBottomDialog(arrayListRefID:String,position:Int) {
        val stringArray = arrayOf("Food & Drinks","Rent","Water","Electricity","Internet","Grocery","Travel","Entertainment","Medical","Clothing","Gift","Book","Credit","Insurance","Others")
        val calendar = Calendar.getInstance()
        val sYear = calendar.get(Calendar.YEAR)
        val sMonth = calendar.get(Calendar.MONTH)+1
        val sDay = calendar.get(Calendar.DAY_OF_MONTH)

        val arrayAdapter = ArrayAdapter(applicationContext,R.layout.list_item_edit,stringArray)

        val dialog = BottomSheetDialog(this)

        dialog.setContentView(R.layout.custom_bottom_sheet_edit)
        val imageView: ImageView? = dialog.findViewById(R.id.dialog_back1)
        val autoCompleteTextView: AutoCompleteTextView? = dialog.findViewById(R.id.auto_complete_text1)
        autoCompleteTextView?.setAdapter(arrayAdapter)
        val cash: RadioButton? = dialog.findViewById(R.id.dialog_payment_mode_cash1)
        val online: RadioButton? = dialog.findViewById(R.id.dialog_payment_mode_online1)
        val amount: EditText? = dialog.findViewById(R.id.dialog_amount1)
        val note: EditText? = dialog.findViewById(R.id.dialog_note1)
        val date: EditText? = dialog.findViewById(R.id.dialog_date1)
        val save: TextView? = dialog.findViewById(R.id.dialog_save1)
        var type:String = "Others"
        var mode:String = "Cash"
        var dateTimeRev :String = ""

        amount?.setText(transactionArrayList[position]._amount.replace("-",""))
        when(transactionArrayList[position]._mode)
        {
            "Cash" -> cash?.isChecked = true
            "UPI/Net-Banking" -> online?.isChecked = true
        }
        autoCompleteTextView?.setText(transactionArrayList[position]._type,false)
        type = transactionArrayList[position]._type
        note?.setText(transactionArrayList[position]._note)

        imageView?.setOnClickListener {
            dialog.dismiss()
        }

        cash?.setOnClickListener {
            if (cash.isChecked) {
                mode = cash.text.toString()
            }
        }

        online?.setOnClickListener {
            if (online.isChecked) {
                mode = online.text.toString()
            }
        }

        date?.setText("$sDay/$sMonth/$sYear")
        dateTimeRev = "$sYear/$sMonth/$sDay"

        autoCompleteTextView?.setOnItemClickListener { _, _, _, id ->
            type = stringArray[id.toInt()]
        }


        date?.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                date.setText("$dayOfMonth/${month.toInt()+1}/$year")
                dateTimeRev = "$year/${month.toInt()+1}/$dayOfMonth"
            },sYear,sMonth,sDay)
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        save?.setOnClickListener {
            if (amount?.text?.isNotBlank() == true && amount.text?.isNotEmpty() == true) {
                amount.error = null
                updateTransaction(arrayListRefID,amount.text.toString(),note?.text.toString(),type,mode,dateTimeRev)
                initializeRecyclerView()
                dialog.dismiss()
                Snackbar.make(recyclerView,"Transaction Updated",Snackbar.LENGTH_SHORT).show()
            }
            else {
                amount?.error = "Amount Cannot be Blank"
            }
        }
        dialog.setOnDismissListener {
            transactionsAdapter.notifyDataSetChanged()
        }
        dialog.show()
    }

    private fun deleteChildFromTransactionId(_id:String ) {
        firebaseReference.child(_id).removeValue()
    }


    private fun initializeRecyclerView() {
        firebaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(name).child("transactions")
        firebaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    transactionArrayList.clear()
                    for (transactionSnapshot in snapshot.children) {
                        val transactionClass = TransactionDataClass(_amount = "-"+transactionSnapshot.child("amount").value.toString(),
                                                                    _note = transactionSnapshot.child("note").value.toString(),
                                                                    _date = transactionSnapshot.child("date").value.toString(),
                                                                    _mode = transactionSnapshot.child("mode").value.toString(),
                                                                    _type = transactionSnapshot.child("type").value.toString())
                        transactionArrayList.add(transactionClass)
                        transactionReferenceId.add(transactionSnapshot.key.toString())
                    }
                    transactionArrayList.reverse()
                    transactionReferenceId.reverse()
                    transactionsAdapter.notifyDataSetChanged()
                }
                else {
                    Snackbar.make(recyclerView, "Please add transaction to show here", Snackbar.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateTransaction(id:String, amount:String, note:String, type:String, mode:String, date:String)
    {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val formatted = current.format(formatter)
        val userDateTime = "$date $formatted"
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val mydate: Date = sdf.parse(userDateTime) as Date
        val millis: Long = mydate.time
        val expenseTransactionModal = ExpenseTransactionModal(amount,note,type,mode,date)
        firebaseReference.child(id).removeValue()
        firebaseReference.child(millis.toString()).setValue(expenseTransactionModal).addOnSuccessListener {
        }.addOnFailureListener {
            Toast.makeText(applicationContext,"Failed to Proceed now!",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(0,androidx.appcompat.R.anim.abc_slide_out_bottom)
    }

}