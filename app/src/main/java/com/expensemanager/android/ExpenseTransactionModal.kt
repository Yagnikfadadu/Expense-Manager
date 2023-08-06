package com.expensemanager.android

class ExpenseTransactionModal(_amount:String, _note:String="", _type:String = "Others", _mode:String = "Cash", _date:String)
{
    var amount:String = _amount.toInt().toString()
    var note:String = _note
    var type:String = _type
    var mode:String = _mode
    var date:String = _date
}