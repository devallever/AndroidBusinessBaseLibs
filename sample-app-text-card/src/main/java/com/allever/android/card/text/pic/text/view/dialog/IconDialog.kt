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
import com.allever.android.card.text.pic.text.model.IconTypeData
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.databinding.DialogIconBinding
import com.allever.android.card.text.pic.text.util.DisplayHelper
import com.allever.android.card.text.pic.text.view.adapter.IconTypeAdapter

class IconDialog(val block: (iconId: Int) -> Unit = {}) : BottomSheetDialogFragment() {

    protected lateinit var mBinding: DialogIconBinding

    private val mAdapter = IconTypeAdapter(TextCardCore.iconTypeData).apply {
        iconTypeListener = object : IconTypeAdapter.Listener {
            override fun onItemSelect(typePosition: Int, iconPosition: Int, item: IconTypeData) {
                val iconItem = item.iconList[iconPosition]
                data.map {
                    it.iconList.map {
                        if (it.icon == iconItem.icon) {
                            it.selected = true
                        } else {
                            it.selected = false
                        }
                    }
                }
                notifyDataSetChanged()
                block.invoke(item.iconList[iconPosition].icon)
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
        mBinding = DialogIconBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mBinding.apply {
            rvIconType.layoutManager = LinearLayoutManager(context)
            rvIconType.adapter = mAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as BottomSheetDialog?
        val frameLayout =
            dialog?.delegate?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        frameLayout?.let {
            val layoutParams = frameLayout.layoutParams
            layoutParams.height = DisplayHelper.dip2px(500)
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
        private val TAG = IconDialog::class.java.simpleName
    }
}