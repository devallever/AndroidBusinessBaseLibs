package com.plinkopro.wincash.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

abstract class BaseBindingAdapter<T, VB : ViewBinding>(data: MutableList<T>? = null) :
    BaseQuickAdapter<T, BaseBindingAdapter.BaseBindViewHolder<VB>>(0, data) {
 
 
    abstract fun createViewBinding(inflater: LayoutInflater, parent: ViewGroup): VB
 
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseBindViewHolder<VB> {
        val binding = createViewBinding(LayoutInflater.from(parent.context), parent)
        return BaseBindViewHolder(binding, binding.root)
    }
 
    //支持binding的内容
    class BaseBindViewHolder<VB>(var binding: VB, view: View) : BaseViewHolder(view)
}