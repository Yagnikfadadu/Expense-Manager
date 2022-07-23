package com.expensemanager.android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionsAdapter(private val transactionList: ArrayList<TransactionDataClass>): RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder>()
{
    class TransactionsViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        var viewIconImage :ImageView = itemView.findViewById(R.id.transaction_icon_view)
        var transactionType :TextView = itemView.findViewById(R.id.transaction_text_title)
        var transactionDate :TextView = itemView.findViewById(R.id.transaction_date)
        var transactionMode :TextView = itemView.findViewById(R.id.transaction_mode)
        var transactionTextAmount :TextView = itemView.findViewById(R.id.transaction_text_amount)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item_view,parent,false)
        return TransactionsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        val currentItem = transactionList[position]
        holder.transactionDate.text = currentItem._date
        holder.transactionType.text = currentItem._type
        holder.transactionTextAmount.text = currentItem._amount
        holder.transactionMode.text = currentItem._mode


        when(currentItem._type)
        {
            "Food & Drinks" -> holder.viewIconImage.setImageResource(R.raw.burger)
            "Rent" -> holder.viewIconImage.setImageResource(R.raw.rent)
            "Water" -> holder.viewIconImage.setImageResource(R.raw.water)
            "Electricity" -> holder.viewIconImage.setImageResource(R.raw.electricity)
            "Internet" -> holder.viewIconImage.setImageResource(R.raw.internet)
            "Grocery" -> holder.viewIconImage.setImageResource(R.raw.grocery)
            "Travel" -> holder.viewIconImage.setImageResource(R.raw.travel)
            "Entertainment" -> holder.viewIconImage.setImageResource(R.raw.entertainment)
            "Medical" -> holder.viewIconImage.setImageResource(R.raw.medical)
            "Clothing" -> holder.viewIconImage.setImageResource(R.raw.clothing)
            "Gift" -> holder.viewIconImage.setImageResource(R.raw.gift)
            "Book" -> holder.viewIconImage.setImageResource(R.raw.book)
            "Credit" -> holder.viewIconImage.setImageResource(R.raw.credit)
            "Insurance" -> holder.viewIconImage.setImageResource(R.raw.insurance)
            else -> holder.viewIconImage.setImageResource(R.raw.rupee)
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}

