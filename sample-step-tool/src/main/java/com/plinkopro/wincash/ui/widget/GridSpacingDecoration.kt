package com.plinkopro.wincash.ui.widget

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingDecoration(
    private val space: Int,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val pos = parent.getChildAdapterPosition(view)
        val col = pos % spanCount
        val row = pos / spanCount

        // 中间用等分策略，边缘靠 RV 的 padding
        outRect.left = col * space / spanCount
        outRect.right = space - (col + 1) * space / spanCount
        outRect.top = if (row == 0) 0 else space
        outRect.bottom = 0
    }
}
