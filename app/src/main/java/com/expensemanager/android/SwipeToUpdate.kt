package com.expensemanager.android

import android.R
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.fonts.Font
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


abstract class SwipeToUpdate: ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val swipe = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(0,swipe)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(
            c,
            recyclerView,
            viewHolder,
            dX,
            dY,
            actionState,
            isCurrentlyActive
        )
            .addBackgroundColor(ContextCompat.getColor(recyclerView.context, R.color.holo_red_light))
            .addActionIcon(R.drawable.ic_menu_delete)
            .addSwipeLeftBackgroundColor(Color.parseColor("#FF5353"))
            .addSwipeLeftActionIcon(R.drawable.ic_menu_delete)
            .addSwipeLeftLabel("Delete Transaction")
            .addSwipeRightBackgroundColor(Color.parseColor("#3CAE5C"))
            .addSwipeRightActionIcon(R.drawable.ic_menu_edit)
            .addSwipeRightLabel("Edit Transaction")
            .setSwipeRightLabelTypeface(android.graphics.Typeface.SANS_SERIF)
            .setSwipeLeftLabelTypeface(android.graphics.Typeface.SANS_SERIF)
            .setSwipeRightLabelTextSize(TypedValue.COMPLEX_UNIT_SP ,16f)
            .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_SP ,16f)
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}