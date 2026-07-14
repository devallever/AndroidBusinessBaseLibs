package com.allever.android.card.text.pic.text.view.dialog

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.model.WordFormatItem
import com.allever.android.card.text.pic.text.databinding.DialogWordCountFormatBinding
import com.allever.android.card.text.pic.text.util.DisplayHelper
import com.allever.android.card.text.pic.text.view.adapter.WordFormatAdapter

class WordCountFormatDialog(val block: (format: Int) -> Unit = { }) :
    BottomSheetDialogFragment() {

    protected lateinit var mBinding: DialogWordCountFormatBinding

    private val mAdapter = WordFormatAdapter(TextCardCore.wordCountFormatData).apply {


        listener = object : WordFormatAdapter.Listener {
            override fun onItemClick(position: Int, item: WordFormatItem) {
                data.map {
                    it.selected = it.format == item.format
                }

                notifyDataSetChanged()
                block.invoke(item.format)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogWordCountFormatBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.apply {
            rvDateFormat.layoutManager = LinearLayoutManager(context)
            rvDateFormat.adapter = mAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as BottomSheetDialog?
        val frameLayout =
            dialog?.delegate?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        frameLayout?.let {
            val layoutParams = frameLayout.layoutParams
            layoutParams.height = DisplayHelper.dip2px(300)
            val behavior = BottomSheetBehavior.from(frameLayout)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    open fun show(manager: FragmentManager) {
        if (context is Activity) {
            if ((context as Activity).isFinishing || (context as Activity).isDestroyed) {
                return
            }
        }
        if (isAdded || manager.findFragmentByTag(TAG) != null) {
            return
        }
        super.show(manager, TAG)
    }

    companion object {
        private val TAG = WordCountFormatDialog::class.java.simpleName
    }
}