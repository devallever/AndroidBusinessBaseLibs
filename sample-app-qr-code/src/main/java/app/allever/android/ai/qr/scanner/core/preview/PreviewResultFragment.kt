package app.allever.android.ai.qr.scanner.core.preview

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.google.zxing.client.android.PreferencesActivity
import com.google.zxing.client.android.history.HistoryManager
import com.google.zxing.client.android.result.ResultButtonListener
import com.google.zxing.client.android.result.ResultHandler
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.URIParsedResult
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.core.RateGuide
import app.allever.android.ai.qr.scanner.core.result.ResultHandlerFactory
import app.allever.android.ai.qr.scanner.core.result.URIResultHandler
import app.allever.android.ai.qr.scanner.core.result.supplement.SupplementalInfoRetriever
import java.text.DateFormat
import java.util.*


class PreviewResultFragment : AppCompatDialogFragment() {

    private lateinit var mDecodeBrowser: DecodeBrowser
    private lateinit var mHistoryManager: HistoryManager
    private lateinit var mContentView: View
    private var mRawResult: Result? = null
    private var mBarcode: Bitmap? = null
    private var mNeedRefresh = false
    private var mListener: OnPreviewResultListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mHistoryManager = HistoryManager(context)
        mDecodeBrowser = DecodeBrowser2(mHistoryManager)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return initView()
    }

    private fun initView(): Dialog {
        val builder = AlertDialog.Builder(context!!)
        mContentView = LayoutInflater.from(context).inflate(mDecodeBrowser.getLayoutId(), null)
        val dialog = builder.setView(mContentView).create()
        dialog.setOnShowListener(object : DialogInterface.OnShowListener {
            override fun onShow(dialog: DialogInterface?) {
                mListener?.onShow()
            }

        })
        return dialog
    }

    override fun onStart() {
        super.onStart()

        handleDecodeInternally()

        val window = dialog?.window
        if (window != null) {
            // 必须设置，否则无法全屏
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            //设置dialog在屏幕底部
            window.setGravity(Gravity.BOTTOM)
            //设置dialog弹出时的动画效果，从屏幕底部向上弹出
            window.setWindowAnimations(R.style.DialogStyle)
            //获得window窗口的属性
            val lp = window.attributes
            //设置窗口宽度为充满全屏
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            //设置窗口高度为包裹内容
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            //将设置好的属性set回去
            window.attributes = lp
        }
    }

    override fun show(manager: androidx.fragment.app.FragmentManager, tag: String?) {
        try {
            manager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
        } catch (e: Exception) {

        }
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        mListener?.onCancel()


    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        val context = context
        if (context != null) {
            RateGuide.Builder().setSpecificAction(true).show(context)
        }

        mListener?.onDismiss()
        (mContentView.findViewById<View>(R.id.ad_content) as? FrameLayout)?.removeAllViews()
    }

    fun hide() {
        dialog?.cancel()
    }

    fun setDecode(rawResult: Result, barcode: Bitmap?) {
        mNeedRefresh = mRawResult != rawResult || barcode != mBarcode
        mRawResult = rawResult
        mBarcode = barcode

    }

    fun setOnPreviewResultListner(listener: OnPreviewResultListener?) {
        this.mListener = listener
    }

    // Put up our own UI for how to handle the decoded contents.
    private fun handleDecodeInternally() {
        if (!mNeedRefresh) {
            return
        }
        mNeedRefresh = false
        mDecodeBrowser.handleDecodeInternally(activity!!, mContentView, mRawResult!!, mBarcode)
    }

    companion object {
        private val TAG = PreviewResultFragment::class.java.name

        private val DISPLAYABLE_METADATA_TYPES = EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
                ResultMetadataType.SUGGESTED_PRICE,
                ResultMetadataType.ERROR_CORRECTION_LEVEL,
                ResultMetadataType.POSSIBLE_COUNTRY)

        fun show(parentFragment: androidx.fragment.app.Fragment, rawResult: Result, barcode: Bitmap? = null, listener: OnPreviewResultListener? = null) {
            val manager = parentFragment.childFragmentManager
            if (manager == null || manager.isDestroyed) {
                return
            }
            var fragment: PreviewResultFragment? = manager.findFragmentByTag(TAG) as? PreviewResultFragment
            if (fragment == null) {
                fragment = PreviewResultFragment()
            }
//            val args = Bundle()
//            // 添加参数
//            fragment.arguments = args
            fragment.setOnPreviewResultListner(listener)
            fragment.setDecode(rawResult, barcode)
            fragment.show(manager, TAG)
        }

        fun destroy(manager: androidx.fragment.app.FragmentManager?) {
            if (manager != null) {
                val prev = manager.findFragmentByTag(TAG)
                if (prev != null) {
                    manager.beginTransaction().remove(prev).commit()
                }
            }
        }
    }

    internal class DecodeBrowser2(historyManager: HistoryManager) : DecodeBrowser(historyManager) {
        override fun getLayoutId(): Int {
            return R.layout.layout_preview_result
        }
    }

    internal open class DecodeBrowser(private val historyManager: HistoryManager) {

        open fun getLayoutId(): Int {
            return R.layout.layout_preview_result_old
        }

        fun handleDecodeInternally(activity: Activity, mContentView: View, rawResult: Result, barcode: Bitmap?) {
            val resources = activity.resources
            val resultHandler = ResultHandlerFactory.makeResultHandler(activity, rawResult)

            val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
            if (resultHandler.defaultButtonID != null && prefs.getBoolean(PreferencesActivity.KEY_AUTO_OPEN_WEB, false)) {
                resultHandler.handleButtonPress(resultHandler.defaultButtonID!!)
            }

            val resultTypeUIData = ResultUIModel.get(resultHandler.type)
            val titleBar = mContentView.findViewById<View>(R.id.title_bar)
            ResultUIModel.changeBackground(resultHandler.type, titleBar, false)
            val titleIcon = mContentView.findViewById<ImageView>(R.id.title_icon)
            titleIcon.setImageDrawable(resultTypeUIData.drawable)

            val titleView = mContentView.findViewById<View>(R.id.title_view) as TextView
            titleView.text = resultTypeUIData.name

            val barcodeImageView = mContentView.findViewById<View>(R.id.barcode_image_view) as ImageView
            if (barcode == null) {
                barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(resources,
                        R.drawable.launcher_icon))
            } else {
                barcodeImageView.setImageBitmap(barcode)
            }

            val formatTextView = mContentView.findViewById<View>(R.id.format_text_view) as TextView
            formatTextView.text = rawResult.barcodeFormat.toString()

            val typeTextView = mContentView.findViewById<View>(R.id.type_text_view) as TextView
            typeTextView.text = resultHandler.type.toString()

            val formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
            val timeTextView = mContentView.findViewById<View>(R.id.time_text_view) as TextView
            timeTextView.text = formatter.format(rawResult.timestamp)


            val metaTextView = mContentView.findViewById<View>(R.id.meta_text_view) as TextView
            val metaTextViewLabel = mContentView.findViewById<View>(R.id.meta_text_view_label)
            metaTextView.visibility = View.GONE
            metaTextViewLabel?.visibility = View.GONE
            val metadata = rawResult.resultMetadata
            if (metadata != null) {
                val metadataText = StringBuilder(20)
                for ((key, value) in metadata) {
                    if (DISPLAYABLE_METADATA_TYPES.contains(key)) {
                        metadataText.append(value).append('\n')
                    }
                }
                if (metadataText.length > 0) {
                    metadataText.setLength(metadataText.length - 1)
                    metaTextView.text = metadataText
                    metaTextView.visibility = View.VISIBLE
                    metaTextViewLabel?.visibility = View.VISIBLE
                }
            }

            val displayContents = resultHandler.displayContents
            val contentsTextView = mContentView.findViewById<View>(R.id.contents_text_view) as TextView
            contentsTextView.text = displayContents
            //val scaledSize = Math.max(22, 32 - displayContents.length / 4)
            //contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize.toFloat())

            val webSafeCheck = mContentView.findViewById<View>(R.id.web_safe_check) as WebSafeCheckLayout
            if (resultHandler.type == ParsedResultType.URI) {
                webSafeCheck.visibility = View.VISIBLE
                val uri = ((resultHandler as URIResultHandler).result as URIParsedResult).uri
                webSafeCheck.start(uri)

            } else {
                webSafeCheck.visibility = View.GONE
            }

            val supplementTextView = mContentView.findViewById<View>(R.id.contents_supplement_text_view) as TextView
            supplementTextView.text = ""
            supplementTextView.setOnClickListener(null)
            if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(
                            PreferencesActivity.KEY_SUPPLEMENTAL, true)) {
                SupplementalInfoRetriever.maybeInvokeRetrieval(supplementTextView,
                        resultHandler.result,
                        historyManager,
                        activity)
            }

            val buttonCount = resultHandler.buttonCount
            val buttonView = mContentView.findViewById<View>(R.id.result_button_view) as ViewGroup
            buttonView.requestFocus()
            for (x in 0 until ResultHandler.MAX_BUTTON_COUNT) {
                val button = buttonView.getChildAt(x) as TextView
                if (x < buttonCount) {
                    if (x == 0) {
                        ResultUIModel.changeBackground(resultHandler.type, button, true)
                    }
                    button.visibility = View.VISIBLE
                    button.setText(resultHandler.getButtonText(x))
                    button.setOnClickListener(ResultButtonListener(resultHandler, x))
                } else {
                    button.visibility = View.GONE
                }
            }
        }
    }

    interface OnPreviewResultListener {
        fun onShow()
        fun onCancel()
        fun onDismiss()
    }
}
