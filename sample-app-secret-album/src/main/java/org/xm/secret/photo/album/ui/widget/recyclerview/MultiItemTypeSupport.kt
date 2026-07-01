package org.xm.secret.photo.album.ui.widget.recyclerview

interface MultiItemTypeSupport<T> {
    fun getLayoutId(itemType: Int): Int
    fun getItemViewType(position: Int, t: T): Int
}