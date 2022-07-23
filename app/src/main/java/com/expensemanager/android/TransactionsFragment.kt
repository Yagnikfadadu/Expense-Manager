package com.expensemanager.android

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class TransactionsFragment : Fragment()
{
    lateinit var firebaseReference: DatabaseReference
    lateinit var recyclerView: RecyclerView
    lateinit var transactionArrayList: ArrayList<TransactionDataClass>
    lateinit var transactionReferenceId : ArrayList<String>
    lateinit var transactionsAdapter:TransactionsAdapter
    lateinit var animation: SpringAnimation
    lateinit var linearProgressIndicator: LinearProgressIndicator
    lateinit var cardView: CardView
    var totalExpense:Int = 0
    lateinit var totalExpenseText:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val view: View = inflater.inflate(R.layout.fragment_transactions,container,false)
        val stringArray = arrayOf("Food & Drinks","Rent","Water","Electricity","Internet","Grocery","Travel","Entertainment","Medical","Clothing","Gift","Book","Credit","Insurance","Others")
        val textView:TextView
        val calendar = Calendar.getInstance()
        val sYear = calendar.get(Calendar.YEAR)
        val sMonth = calendar.get(Calendar.MONTH)+1
        val sDay = calendar.get(Calendar.DAY_OF_MONTH)

        val floatingActionButton:FloatingActionButton = view.findViewById(R.id.add_expense)
        linearProgressIndicator = view.findViewById(R.id.linear_progress_bar)

        val arrayAdapter = ArrayAdapter(requireContext(),R.layout.list_item,stringArray)
        cardView = view.findViewById(R.id.transactions_card)
        totalExpenseText = view.findViewById(R.id.total_expense)
        transactionReferenceId = ArrayList<String>()
        recyclerView = view.findViewById(R.id.fragment_recycler_view)
        textView = view.findViewById(R.id.fragment_show_more)
        val layoutManager:LinearLayoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        transactionArrayList = arrayListOf<TransactionDataClass>()

        totalExpenseText.text = "₹ 0"
        initializeRecyclerView()
        transactionsAdapter = TransactionsAdapter(transactionArrayList)
        recyclerView.adapter = transactionsAdapter


        floatingActionButton.setOnClickListener{
            val dialog = BottomSheetDialog(requireContext())
            dialog.setContentView(R.layout.custom_bottom_sheet_dialog)
            val imageView: ImageView? = dialog.findViewById(R.id.dialog_back)
            val autoCompleteTextView: AutoCompleteTextView? = dialog.findViewById(R.id.auto_complete_text)
            autoCompleteTextView?.setAdapter(arrayAdapter)
            val cash:RadioButton? = dialog.findViewById(R.id.dialog_payment_mode_cash)
            val online:RadioButton? = dialog.findViewById(R.id.dialog_payment_mode_online)
            val amount:EditText? = dialog.findViewById(R.id.dialog_amount)
            val note:EditText? = dialog.findViewById(R.id.dialog_note)
            val date:EditText? = dialog.findViewById(R.id.dialog_date)
            val save:TextView? = dialog.findViewById(R.id.dialog_save)
            var type:String = "Others"
            var mode:String = "Cash"
            var dateTimeRev :String = ""

            dialog.dismissWithAnimation = true

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
                val datePickerDialog = DatePickerDialog((activity as MainHomeActivity),DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        date.setText("$dayOfMonth/${month.toInt()+1}/$year")
                        dateTimeRev = "$year/${month.toInt()+1}/$dayOfMonth"
                    },sYear,sMonth,sDay)
                    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
                    datePickerDialog.show()
            }

            save?.setOnClickListener {
                if (amount?.text?.isNotBlank() == true && amount.text?.isNotEmpty() == true) {
                    amount.error = null
                    (activity as MainHomeActivity).pushTransaction(amount.text.toString(),note?.text.toString(),type,mode,dateTimeRev)
                    initializeRecyclerView()
                    dialog.dismiss()
                    Snackbar.make(view,"Transaction saved",Snackbar.LENGTH_SHORT).show()
                }
                else {
                    amount?.error = "Amount Cannot be Blank"
                }
            }
            dialog.show()
        }

        textView.setOnClickListener {
            val intent = Intent(requireContext(),RecyclerActivity::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(0, androidx.appcompat.R.anim.abc_slide_out_top)
        }

        return view
    }
    private fun initializeRecyclerView() {
        firebaseReference = FirebaseDatabase.getInstance().reference.child("Users").child((activity as MainHomeActivity).name).child("transactions")
        firebaseReference.orderByKey().limitToLast(7).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    transactionArrayList.clear()
                    totalExpense = 0
                    for (transactionSnapshot in snapshot.children) {
                        val transactionClass = TransactionDataClass(_amount = "-"+transactionSnapshot.child("amount").value.toString(),
                            _note = transactionSnapshot.child("note").value.toString(),
                            _date = transactionSnapshot.child("date").value.toString(),
                            _mode = transactionSnapshot.child("mode").value.toString(),
                            _type = transactionSnapshot.child("type").value.toString())
                        transactionArrayList.add(transactionClass)
                        transactionReferenceId.add(transactionSnapshot.key.toString())
                    }
                    linearProgressIndicator.visibility = View.INVISIBLE
                    transactionsAdapter.notifyDataSetChanged()
                    getTotalExpense()
                }
                else {
                    linearProgressIndicator.visibility = View.INVISIBLE
                    Snackbar.make(recyclerView,"Please add transaction to show here",Snackbar.LENGTH_SHORT).show()
                    totalExpenseText.text = "₹ 0"
                    transactionArrayList.clear()
                    transactionsAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    
    public fun getTotalExpense() {
        firebaseReference = FirebaseDatabase.getInstance().reference.child("Users").child((activity as MainHomeActivity).name).child("transactions")
        firebaseReference.orderByKey().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    totalExpense = 0
                    for (transactionSnapshot in snapshot.children) {
                        val exp:String = transactionSnapshot.child("amount").value.toString()
                        totalExpense += exp.toInt()
                        totalExpenseText.text = "₹ $totalExpense"
                    }
                }
                else {
                    totalExpenseText.text = "₹ 0"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        initializeRecyclerView()
    }

}