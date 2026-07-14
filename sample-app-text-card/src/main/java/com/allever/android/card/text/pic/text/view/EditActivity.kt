package com.allever.android.card.text.pic.text.view

import android.Manifest
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.allever.android.card.text.pic.text.util.StatusBarUtil
import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.base.AppActivity
import com.allever.android.card.text.pic.text.model.ColorData
import com.allever.android.card.text.pic.text.model.DateFormat
import com.allever.android.card.text.pic.text.model.SwitchItem
import com.allever.android.card.text.pic.text.model.TemplateManager
import com.allever.android.card.text.pic.text.model.TemplateModel
import com.allever.android.card.text.pic.text.model.TextCardCore
import com.allever.android.card.text.pic.text.databinding.TcActivityEditBinding
import com.allever.android.card.text.pic.text.databinding.TcPopExportBinding
import com.allever.android.card.text.pic.text.util.ActivityHelper
import com.allever.android.card.text.pic.text.util.DisplayHelper
import com.allever.android.card.text.pic.text.util.KeyboardUtils
import com.allever.android.card.text.pic.text.util.KeyboardUtils.SoftKeyboardListener.OnSoftKeyboardChangeListener
import com.allever.android.card.text.pic.text.util.PermissionHelper
import com.allever.android.card.text.pic.text.util.ShareHelper
import com.allever.android.card.text.pic.text.util.ViewHelper
import com.allever.android.card.text.pic.text.util.toast
import com.allever.android.card.text.pic.text.view.adapter.Pager2Adapter
import com.allever.android.card.text.pic.text.view.adapter.SwitchItemAdapter
import com.allever.android.card.text.pic.text.view.adapter.TemplateItemAdapter
import com.allever.android.card.text.pic.text.view.dialog.DateTimeFormatDialog
import com.allever.android.card.text.pic.text.view.dialog.IconDialog
import com.allever.android.card.text.pic.text.view.dialog.WordCountFormatDialog
import com.allever.android.card.text.pic.text.viewmodel.EditViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class EditActivity : AppActivity<TcActivityEditBinding, EditViewMode>() {

    private val RC_PERMISSION = 1000
    private val mPermissionsList = ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private lateinit var mPopBinding: TcPopExportBinding
    private lateinit var mPopExport: PopupWindow

    private val mIconDialog by lazy {
        IconDialog {
            TemplateManager.currentTemplate.getIconView().setImageResource(it)
            TextCardCore.cardData.iconResId = it
            TextCardCore.saveCardData()
        }
    }

    private val mDateTimeFormatDialog by lazy {
        DateTimeFormatDialog { text, format ->
            TemplateManager.currentTemplate.getDateView().text = text
            TextCardCore.cardData.dateFormatType = format
            TextCardCore.saveCardData()
        }
    }

    private val mWordFormatDialog by lazy {
        WordCountFormatDialog {
            val etContent = TemplateManager.currentTemplate.getContentView()
            TemplateManager.currentTemplate.getWordCountView().text =
                getString(it, etContent.text.toString().length)
            TextCardCore.cardData.wordCountFormatType = it
            TextCardCore.saveCardData()
        }
    }

    private val templateAdapter by lazy {
        TemplateItemAdapter().apply {
            data.clear()
            data.addAll(TemplateManager.templateData)
            itemClick = object : TemplateItemAdapter.ItemClick {
                override fun onItemClick(position: Int, item: TemplateModel<*>) {
                    handleTemplateClick(this@apply, item)
                }
            }
        }
    }

    private fun handleTemplateClick(adapter: TemplateItemAdapter, item: TemplateModel<*>) {
        adapter.data.map {
            it.selected = false
            if (it.getTemplateName() == item.getTemplateName()) {
                it.selected = true
            }
        }
        TemplateManager.currentTemplate = item
        adapter.notifyDataSetChanged()
        changeTemplate(item)
        TextCardCore.saveCardData()
    }

    private val mColorListener = object : ColorFragment.ColorListener {
        override fun onColorSelect(pageIndex: Int, colorData: ColorData) {
            TemplateManager.currentTemplate.updateCardBg(pageIndex == 1, colorData)
            updateMenuColorText(pageIndex == 1, colorData)
            TextCardCore.cardData.setBgColorType(pageIndex)
            TextCardCore.cardData.setBgColorName(colorData.name)
            TextCardCore.saveCardData()
        }
    }

    private val colorFragmentList = mutableListOf<ColorFragment>().apply {
        add(ColorFragment(0).apply {
            colorListener = mColorListener
        })//Light
        add(ColorFragment(1).apply {
            colorListener = mColorListener
        })//Dark
    }

    private val colorPagerAdapter by lazy {
        Pager2Adapter(this, colorFragmentList)
    }

    private val switchAdapter by lazy {
        SwitchItemAdapter().apply {
            data.clear()
            data.addAll(TemplateManager.switchData)
            notifyDataSetChanged()

            itemClick = object : SwitchItemAdapter.ItemClick {
                override fun onItemClick(position: Int, item: SwitchItem) {
                    item.show = !item.show
                    notifyItemChanged(position, position)
                    handleSwitch(item, position)
                }
            }
        }
    }

    private fun handleSwitch(item: SwitchItem, position: Int) {
        when (position) {
            0 -> {
                //icon
                TemplateManager.templateData.map {
                    it.showOrHideIcon(item.show)
                }
                TextCardCore.cardData.switchIcon = item.show
            }

            1 -> {
                //Date
                TemplateManager.templateData.map {
                    it.showOrHideDate(item.show)
                }
                TextCardCore.cardData.switchDate = item.show
            }

            2 -> {
                //Title
                TemplateManager.templateData.map {
                    it.showOrHideTitle(item.show)
                }
                TextCardCore.cardData.switchTitle = item.show
            }

            3 -> {
                //Text
                TemplateManager.templateData.map {
                    it.showOrHideContent(item.show)
                }
                TextCardCore.cardData.switchText = item.show
            }

            4 -> {
                //Author
                TemplateManager.templateData.map {
                    it.showOrHideAuthor(item.show)
                }
                TextCardCore.cardData.switchQuote = item.show
            }

            5 -> {
                //Count
                TemplateManager.templateData.map {
                    it.showOrHideWordCount(item.show)
                }
                TextCardCore.cardData.switchCount = item.show
            }

            6 -> {
                //qrcode
                TemplateManager.templateData.map {
                    it.showOrHideQrCode(item.show)
                }
                TextCardCore.cardData.switchQrCode = item.show
            }

            7 -> {
                //MARK
                TemplateManager.templateData.map {
                    it.showOrHideMark(item.show)
                }
                TextCardCore.cardData.switchWaterMark = item.show
            }
        }

        TextCardCore.saveCardData()
    }

    private lateinit var rootView: View


    override fun viewModelClass() = EditViewMode::class.java

    override fun inflate() = TcActivityEditBinding.inflate(layoutInflater)

    override fun init() {
        TemplateManager.initTemplateView()
        rootView = this.findViewById(android.R.id.content)

        mBinding.apply {
            StatusBarUtil.fixStatusBar(toolBar)
            changeTemplate(TemplateManager.currentTemplate)

            scrollView.post {
                ViewHelper.setMarginTop(
                    contentContainer,
                    StatusBarUtil.getStatusBarHeight(this@EditActivity) + DisplayHelper.dip2px(
                        54 + 10
                    )
                )
            }

            KeyboardUtils.SoftKeyboardListener.setListener(
                this@EditActivity,
                object : OnSoftKeyboardChangeListener {
                    override fun hide(height: Int) {
                        btnDone.isVisible = false
                        btnExport.isVisible = true
                        ivClearText.isVisible = true
                        mViewModel.saveEdittextContent()
                        TemplateManager.currentTemplate.getTitleView().clearFocus()
                        TemplateManager.currentTemplate.getContentView().clearFocus()
                        TemplateManager.currentTemplate.getAuthorView().clearFocus()
                    }

                    override fun show(height: Int) {
                        btnDone.isVisible = true
                        btnExport.isVisible = false
                        ivClearText.isVisible = false
                    }
                })

            //Template
            val templateLayoutManager =
                LinearLayoutManager(this@EditActivity, LinearLayoutManager.HORIZONTAL, false)
            templateContent.layoutManager = templateLayoutManager
            templateContent.adapter = templateAdapter

            //BgColor
            viewPager.adapter = colorPagerAdapter
            viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    TextCardCore.cardData.setBgColorType(position)
                    TextCardCore.saveCardData()
                    if (position == 0) {
                        indicateFirst.setBackgroundResource(R.drawable.tc_shape_google_blue_r45)
                        indicateSecond.setBackgroundResource(R.drawable.shape_999999_r45)
                        tvColorStyle.text = "Light"
                    } else {
                        indicateFirst.setBackgroundResource(R.drawable.shape_999999_r45)
                        indicateSecond.setBackgroundResource(R.drawable.tc_shape_google_blue_r45)
                        tvColorStyle.text = "Dark"
                    }
                }
            })

            //Switch
            switchContent.layoutManager =
                LinearLayoutManager(this@EditActivity, LinearLayoutManager.HORIZONTAL, false)
            switchContent.adapter = switchAdapter
        }

        initPopMenu()

        initClickListener()

//        mShakeViewContainer = ShakeViewContainer(mBinding.ivRecommend)
//        mShakeViewContainer.start()
//        mBinding.ivRecommend.setOnClickListener {
//            ActivityHelper.startActivity(this, RecommendListActivity::class.java)
//        }
    }

    private fun initPopMenu() {
        mPopBinding = TcPopExportBinding.inflate(layoutInflater)

        mPopExport = PopupWindow(
            mPopBinding.root,
            DisplayHelper.dip2px(240),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        TemplateManager.destroyTemplate()
    }

    override fun onBackPressed() {
        if (mBinding.menuContainer.isVisible) {
            mBinding.btnEditStyle.performClick()
        } else {
        }
    }

    private fun checkPermission() =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        true
    } else {
        PermissionHelper.hasPermissionOrigin(this@EditActivity, mPermissionsList)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISSION) {
            val hasPermission = checkPermission()
            if (hasPermission) {
                handleSaveView()
            }
        }
    }

    private fun handleSaveView() {
        mViewModel.saveView { result, path ->
            if (result) {
                toast("save success: $path")
            } else {
                toast("save fail")
            }
        }
    }

    private fun initClickListener() {
        mBinding.apply {
            ivSetting.setOnClickListener {
                ActivityHelper.startActivity(this@EditActivity, SettingActivity::class.java)
            }

            btnDone.setOnClickListener {
                KeyboardUtils.hideInput(this@EditActivity)
            }

            btnEditStyle.setOnClickListener {
                menuContainer.isVisible = !menuContainer.isVisible
                if (menuContainer.isVisible) {
                    btnEditStyle.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this@EditActivity,
                            R.color.tc_page_bg
                        )
                    )
                    tvEditStyle.visibility = View.INVISIBLE
                    ivBtnEditStyleArrow.animate().rotation(-180f).start()
                } else {
                    btnEditStyle.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this@EditActivity,
                            R.color.color_252525
                        )
                    )
                    tvEditStyle.visibility = View.VISIBLE
                    ivBtnEditStyleArrow.animate().rotation(0f).start()
                }

                updateContentMarginBottom()
            }

            val selectTextColor = ContextCompat.getColor(this@EditActivity, R.color.white)
            val unSelectColor = ContextCompat.getColor(this@EditActivity, R.color.color_999999)
            btnTemplate.setOnClickListener {
                ivTemplate.isVisible = true
                tvTemplate.setTextColor(selectTextColor)
                btnTemplate.background =
                    ContextCompat.getDrawable(this@EditActivity, R.drawable.tc_shape_edit_page_btn_bg)
                templateContent.isVisible = true

                ivBgColor.isVisible = false
                tvBgColor.setTextColor(unSelectColor)
                btnBgColor.background = null
                bgColorContent.isVisible = false

                ivSwitch.isVisible = false
                tvSwitch.setTextColor(unSelectColor)
                btnSwitch.background = null
                switchContent.isVisible = false

                updateContentMarginBottom()
            }

            btnBgColor.setOnClickListener {
                changeBgColorData(TemplateManager.currentTemplate)

                ivTemplate.isVisible = false
                tvTemplate.setTextColor(unSelectColor)
                btnTemplate.background = null
                templateContent.isVisible = false

                ivBgColor.isVisible = true
                tvBgColor.setTextColor(selectTextColor)
                btnBgColor.background =
                    ContextCompat.getDrawable(this@EditActivity, R.drawable.tc_shape_edit_page_btn_bg)
                bgColorContent.isVisible = true

                ivSwitch.isVisible = false
                tvSwitch.setTextColor(unSelectColor)
                btnSwitch.background = null
                switchContent.isVisible = false

                updateContentMarginBottom()
            }

            btnSwitch.setOnClickListener {
                ivTemplate.isVisible = false
                tvTemplate.setTextColor(unSelectColor)
                btnTemplate.background = null
                templateContent.isVisible = false

                ivBgColor.isVisible = false
                tvBgColor.setTextColor(unSelectColor)
                btnBgColor.background = null
                bgColorContent.isVisible = false

                ivSwitch.isVisible = true
                tvSwitch.setTextColor(selectTextColor)
                btnSwitch.background =
                    ContextCompat.getDrawable(this@EditActivity, R.drawable.tc_shape_edit_page_btn_bg)
                switchContent.isVisible = true

                updateContentMarginBottom()
            }

            mPopBinding.btnSave.setOnClickListener {
                mPopExport.dismiss()
                val hasPermission = checkPermission()

                if (hasPermission) {
                    handleSaveView()
                } else {
                    AlertDialog.Builder(this@EditActivity)
                        .apply {
                            setMessage(R.string.tc_request_permission_message)
                            setTitle(R.string.tc_permission_tips_title)
                            setPositiveButton(getString(R.string.tc_agree)) { dialog, which ->
                                dialog.dismiss()
                                val array: Array<String> =
                                    mPermissionsList.toArray(arrayOfNulls<String>(mPermissionsList.size))
                                ActivityCompat.requestPermissions(
                                    this@EditActivity,
                                    array,
                                    RC_PERMISSION
                                );
                            }
                            setNegativeButton(getString(R.string.tc_reject)) { dialog, which ->
                                dialog.dismiss()
                            }
                        }.show()
                }
            }
            mPopBinding.btnShare.setOnClickListener {
                mPopExport.dismiss()
                //
                val path =
                    "${cacheDir.absolutePath}${File.separator}${System.currentTimeMillis()}.jpg"
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = mViewModel.saveViewAsImageToCache(
                        TemplateManager.currentTemplate.getTemplateContentView(),
                        path
                    )
                    if (result) {
                        ShareHelper.shareImage(this@EditActivity, path)
                    } else {
                        toast("Share fail")
                    }
                }
            }
            btnExport.setOnClickListener {
                mPopExport.showAsDropDown(
                    btnExport,
                    0,
                    DisplayHelper.dip2px(22)
                )
            }

            ivClearText.setOnClickListener {
                AlertDialog.Builder(this@EditActivity)
                    .setMessage(R.string.tc_clear_text_tips)
                    .setTitle(R.string.tc_clear_all_text)
                    .setPositiveButton(R.string.tc_clear) { dialog, which ->
                        TemplateManager.currentTemplate.getTitleView().setText("")
                        TemplateManager.currentTemplate.getContentView().setText("")
                        TemplateManager.currentTemplate.getAuthorView().setText("")
                        TextCardCore.cardData.title = ""
                        TextCardCore.cardData.text = ""
                        TextCardCore.cardData.author = ""
                        TextCardCore.saveCardData()
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.tc_cancle) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun updateContentMarginBottom() {
        mBinding.apply {
            menuContainer.post {
                val marginBottom = if (menuContainer.isVisible) {
                    menuContainer.height + DisplayHelper.dip2px(10)
                } else {
                    0
                }

                ViewHelper.setMarginBottom(contentContainer, marginBottom)
            }

        }
    }

    private fun changeTemplate(templateModel: TemplateModel<*>) {
        mBinding.apply {
            TextCardCore.cardData.templateName = templateModel.getTemplateName()
            contentContainer.removeAllViews()
            contentContainer.addView(templateModel.mBinding?.root)
            initTemplateListener()
            initTemplateData()
            contentContainer.post {
                changeBgColorData(TemplateManager.currentTemplate)
            }
        }
    }

    private fun initTemplateListener() {
        TemplateManager.currentTemplate.apply {
            getIconView().setOnClickListener {
                mIconDialog.show(supportFragmentManager)
            }
            getDateView().setOnClickListener {
                mDateTimeFormatDialog.show(supportFragmentManager)
            }
            getWordCountView().setOnClickListener {
                mWordFormatDialog.show(supportFragmentManager)
            }

            getTitleView().addTextChangedListener {
                TextCardCore.cardData.title = it?.toString() ?: ""
            }
            getContentView().addTextChangedListener {
                TextCardCore.cardData.text = it?.toString() ?: ""
                getWordCountView().text = getString(
                    TextCardCore.cardData.wordCountFormatType,
                    getContentView().text.toString().length
                )
            }
            getAuthorView().addTextChangedListener {
                TextCardCore.cardData.author = it?.toString() ?: ""
            }
        }

        //when hide keyboard saveData
    }

    private fun initTemplateData() {
        val cardData = TextCardCore.cardData
        TemplateManager.currentTemplate.apply {
            //icon
            getIconView().setImageResource(cardData.iconResId)
            //date always today
            getDateView().text =
                DateFormat.format(cardData.dateFormatType, System.currentTimeMillis())

            //title
            getTitleView().setText(cardData.title)
            //text
            getContentView().setText(cardData.text)
            //author
            getAuthorView().setText(cardData.author)
            //word
            getWordCountView().text =
                getString(cardData.wordCountFormatType, getContentView().text.toString().length)

            //bgColor

        }

        //showOrHide
        TemplateManager.templateData.map {
            it.showOrHideIcon(cardData.switchIcon)
            it.showOrHideDate(cardData.switchDate)
            it.showOrHideTitle(cardData.switchTitle)
            it.showOrHideContent(cardData.switchText)
            it.showOrHideAuthor(cardData.switchQuote)
            it.showOrHideWordCount(cardData.switchCount)
            it.showOrHideQrCode(cardData.switchQrCode)
        }
    }

    private fun changeBgColorData(templateModel: TemplateModel<*>, isChangeData: Boolean = true) {
        val lightList = templateModel.getTemplateBgColor()[0].colorDataList
        val darkList = templateModel.getTemplateBgColor()[1].colorDataList
        val isDark = TextCardCore.cardData.getBgColorType() == 1
        val colorName = TextCardCore.cardData.getBgColorName()

        lightList.map {
            it.selected = false
        }
        darkList.map {
            it.selected = false
        }

        var setSuccess = false
        if (isDark) {
            darkList.map {
                if (it.name == colorName) {
                    it.selected = true
                    setSuccess = true
                }
            }
        } else {
            lightList.map {
                if (it.name == colorName) {
                    it.selected = true
                    setSuccess = true
                }
            }
        }

        if (!setSuccess) {
            lightList[0].selected = true
        }

        if (isChangeData) {
            colorFragmentList[0].updateData(lightList)
            colorFragmentList[1].updateData(darkList)
        } else {
            colorFragmentList[0].notifySelectedChanged()
            colorFragmentList[1].notifySelectedChanged()
        }


        val colorData = TemplateManager.currentTemplate.getBgColorData()
        colorData.let {
            TemplateManager.currentTemplate.updateCardBg(isDark, it)
        }
        updateMenuColorText(isDark, colorData)

    }

    private fun updateMenuColorText(isDark: Boolean, colorData: ColorData) {
        mBinding.tvColorName.text = colorData.name
        if (isDark) {
            mBinding.tvColorStyle.text = "Dark"
        } else {
            mBinding.tvColorStyle.text = "Light"
        }
    }


}