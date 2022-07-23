package com.expensemanager.android

import android.graphics.Color
import android.graphics.Typeface
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*


class AnalysisFragment : Fragment(), OnChartValueSelectedListener 
{
    lateinit var cardView:MaterialCardView
    lateinit var recyclerView: RecyclerView
    lateinit var selectedType:String
    lateinit var pieChart: PieChart
    lateinit var firebaseReference: DatabaseReference
    lateinit var transactionsAdapterRecyclerAnalysis:TransactionAdapterClick
    lateinit var firebaseReferenceRecycler: DatabaseReference
    lateinit var transactionArrayList: ArrayList<TransactionDataClass>
    lateinit var transactionArrayListRecycler: ArrayList<TransactionDataClass>
    lateinit var transactionTotalMap: MutableMap<String,Float>
    lateinit var expenseAnalysisText:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_analysis,container,false)
        pieChart = view.findViewById(R.id.fragment_pie_chart)
        firebaseReference = FirebaseDatabase.getInstance().reference.child("Users").child((activity as MainHomeActivity).name).child("transactions")
        firebaseReferenceRecycler = FirebaseDatabase.getInstance().reference.child("Users").child((activity as MainHomeActivity).name).child("transactions")
        transactionArrayList = arrayListOf<TransactionDataClass>()
        transactionArrayListRecycler = arrayListOf<TransactionDataClass>()
        transactionTotalMap = mutableMapOf()


        recyclerView = view.findViewById(R.id.analysis_recyclerview)
        cardView = view.findViewById(R.id.analysis_type_card_view)
        expenseAnalysisText = view.findViewById(R.id.expense_analysis_text)


        val layoutManager: LinearLayoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager

        transactionsAdapterRecyclerAnalysis = TransactionAdapterClick(transactionArrayListRecycler)
        recyclerView.adapter = transactionsAdapterRecyclerAnalysis

        loadDataToArrayList()
        loadDataToArrayList("hhg")
        pieChart.legend.isWordWrapEnabled = true
        pieChart.setOnChartValueSelectedListener(this)

        return view
    }

    private fun loadDataToArrayList() {
        firebaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    transactionArrayList.clear()
                    for (transactionSnapshot in snapshot.children) {
                        val transactionClass = TransactionDataClass(_amount = "-"+transactionSnapshot.child("amount").value.toString(),
                            _note = transactionSnapshot.child("note").value.toString(),
                            _date = transactionSnapshot.child("date").value.toString(),
                            _mode = transactionSnapshot.child("mode").value.toString(),
                            _type = transactionSnapshot.child("type").value.toString())
                        val exp:String = transactionSnapshot.child("amount").value.toString()
                        transactionArrayList.add(transactionClass)
                    }
                }
                populatePieChart()
            }
            override fun onCancelled(error: DatabaseError) {
                val i:Int = 0
            }

        })
    }

    private fun loadDataToArrayList(transactionType:String) {
        firebaseReferenceRecycler.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    transactionArrayListRecycler.clear()
                    for (transactionSnapshot in snapshot.children) {
                        val transactionClass = TransactionDataClass(_amount = "-"+transactionSnapshot.child("amount").value.toString(),
                            _note = transactionSnapshot.child("note").value.toString(),
                            _date = transactionSnapshot.child("date").value.toString(),
                            _mode = transactionSnapshot.child("mode").value.toString(),
                            _type = transactionSnapshot.child("type").value.toString())
                        if (transactionClass._type==transactionType) {
                            transactionArrayListRecycler.add(transactionClass)
                        }
                    }
                    transactionsAdapterRecyclerAnalysis.notifyDataSetChanged()
                }
                else
                {
                    transactionArrayListRecycler.clear()
                    pieChart.clear()
                    cardView.visibility = View.INVISIBLE
                    transactionsAdapterRecyclerAnalysis.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        selectedType = transactionType
    }

    fun populatePieChart() {
        transactionTotalMap["Food & Drinks"] = (0).toFloat()
        transactionTotalMap["Rent"] = (0).toFloat()
        transactionTotalMap["Water"] = (0).toFloat()
        transactionTotalMap["Electricity"] = (0).toFloat()
        transactionTotalMap["Internet"] = (0).toFloat()
        transactionTotalMap["Grocery"] = (0).toFloat()
        transactionTotalMap["Travel"] = (0).toFloat()
        transactionTotalMap["Entertainment"] = (0).toFloat()
        transactionTotalMap["Medical"] = (0).toFloat()
        transactionTotalMap["Clothing"] = (0).toFloat()
        transactionTotalMap["Gift"] = (0).toFloat()
        transactionTotalMap["Book"] = (0).toFloat()
        transactionTotalMap["Credit"] = (0).toFloat()
        transactionTotalMap["Insurance"] = (0).toFloat()
        transactionTotalMap["Others"] = (0).toFloat()

        for (data in transactionArrayList) {
            data._amount = data._amount.replace("-","")
            when(data._type) {
                "Food & Drinks" -> transactionTotalMap["Food & Drinks"] = (transactionTotalMap["Food & Drinks"]!!.toFloat() + data._amount.toFloat())
                "Rent" -> transactionTotalMap["Rent"] = (transactionTotalMap["Rent"]!!.toFloat() + data._amount.toFloat())
                "Water" -> transactionTotalMap["Water"] = (transactionTotalMap["Water"]!!.toFloat() + data._amount.toFloat())
                "Electricity" -> transactionTotalMap["Electricity"] = (transactionTotalMap["Electricity"]!!.toFloat() + data._amount.toFloat())
                "Internet" -> transactionTotalMap["Internet"] = (transactionTotalMap["Internet"]!!.toFloat() + data._amount.toFloat())
                "Grocery" -> transactionTotalMap["Grocery"] = (transactionTotalMap["Grocery"]!!.toFloat() + data._amount.toFloat())
                "Travel" -> transactionTotalMap["Travel"] = (transactionTotalMap["Travel"]!!.toFloat() + data._amount.toFloat())
                "Entertainment" -> transactionTotalMap["Entertainment"] = (transactionTotalMap["Entertainment"]!!.toFloat() + data._amount.toFloat())
                "Medical" -> transactionTotalMap["Medical"] = (transactionTotalMap["Medical"]!!.toFloat() + data._amount.toFloat())
                "Clothing" -> transactionTotalMap["Clothing"] = (transactionTotalMap["Clothing"]!!.toFloat() + data._amount.toFloat())
                "Gift" -> transactionTotalMap["Gift"] = (transactionTotalMap["Gift"]!!.toFloat() + data._amount.toFloat())
                "Book" -> transactionTotalMap["Book"] = (transactionTotalMap["Book"]!!.toFloat() + data._amount.toFloat())
                "Credit" -> transactionTotalMap["Credit"] = (transactionTotalMap["Credit"]!!.toFloat() + data._amount.toFloat())
                "Insurance" -> transactionTotalMap["Insurance"] = (transactionTotalMap["Insurance"]!!.toFloat() + data._amount.toFloat())
                "Others" -> transactionTotalMap["Others"] = (transactionTotalMap["Others"]!!.toFloat() + data._amount.toFloat())
            }
        }

        val pieEntries = ArrayList<PieEntry>()

        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#7b920a"))
        colors.add(Color.parseColor("#00c3ff"))
        colors.add(Color.parseColor("#f05053"))
        colors.add(Color.parseColor("#f5af19"))
        colors.add(Color.parseColor("#0082c8"))
        colors.add(Color.parseColor("#ff5f67"))
        colors.add(Color.parseColor("#237a57"))
        colors.add(Color.parseColor("#CB3066"))
        colors.add(Color.parseColor("#D39D38"))
        colors.add(Color.parseColor("#237a57"))
        colors.add(Color.parseColor("#fc00ff"))
        colors.add(Color.parseColor("#237a57"))
        colors.add(Color.parseColor("#5433FF"))
        colors.add(Color.parseColor("#8A2387"))
        colors.add(Color.parseColor("#2F0743"))

        for (key in transactionTotalMap.keys) {
            if (transactionTotalMap[key]!=(0).toFloat()) {
                pieEntries.add(PieEntry(transactionTotalMap[key]!!,key))
            }
        }

        val dataSet = PieDataSet(pieEntries,"")
        dataSet.valueTextSize = 16f
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.isHighlightEnabled = true
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSet.valueLineColor = Color.BLACK
        dataSet.valueTextColor = Color.BLACK
        dataSet.colors = colors
        dataSet.sliceSpace = 1.1f

        val description = Description()
        description.text = ""
        description.textSize = 14f
        val pieData = PieData(dataSet)

        pieData.setDrawValues(false)
        pieChart.description = description
        pieChart.data = pieData
        pieChart.centerText = "Expenses"
        pieChart.setCenterTextSize(18f)
        pieChart.setEntryLabelTextSize(16f)
        pieChart.extraLeftOffset = 15f
        pieChart.extraRightOffset = 18f
        pieChart.extraBottomOffset = 10f
        pieChart.extraTopOffset = 10f
        pieChart.legend.position = Legend.LegendPosition.BELOW_CHART_CENTER
        pieChart.legend.formToTextSpace = 6.1f
        pieChart.legend.isWordWrapEnabled = true
        pieChart.legend.textSize = 10f
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateX(500,Easing.EasingOption.EaseInOutCirc)

        pieChart.invalidate()
    }

    override fun onValueSelected(p0: Entry?, p1: Highlight?) {
        recyclerView.visibility = View.VISIBLE
        expenseAnalysisText.text = (p0 as PieEntry).label
        cardView.visibility = View.VISIBLE
        pieChart.centerText = (p0 as PieEntry).label +"\n"+(p0 as PieEntry).value
        loadDataToArrayList((p0 as PieEntry).label)
    }

    override fun onNothingSelected() {
        var int:Int = 0
        pieChart.centerText = ""
        recyclerView.visibility = View.INVISIBLE
        cardView.visibility = View.INVISIBLE
    }
}