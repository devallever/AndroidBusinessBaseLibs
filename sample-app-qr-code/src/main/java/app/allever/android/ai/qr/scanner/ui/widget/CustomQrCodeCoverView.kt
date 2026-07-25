package app.allever.android.ai.qr.scanner.ui.widget;
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.allever.app.qr.code.scaner.R
class CustomQrCodeCoverView  : RelativeLayout ,View.OnClickListener{
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, style: Int) : super(context, attrs, style)

    private var mRelativeDefault:RelativeLayout ?= null
    private var mRelativeDownload:RelativeLayout ?= null
    private var mRelativeProgressBar:RelativeLayout ?= null
    private var mProgressBar:ProgressBar ?= null
    private var mContentView:View ?= null
    var mDefaultClickListener: DefaultClickListener?=null
    init {
        mContentView = View.inflate(context, R.layout.qr_custom_qrcode_cover_layout, this)
        mRelativeDefault = mContentView?.findViewById(R.id.rl_qrcode_select_default)
        mRelativeDownload = mContentView?.findViewById(R.id.rl_qrcode_select_download)
        mRelativeProgressBar = mContentView?.findViewById(R.id.rl_qrcode_select_progress_bar)
        mProgressBar = mContentView?.findViewById(R.id.qrcode_select_progress_bar)
        mProgressBar?.max = 100

        mRelativeDefault?.setOnClickListener(this)
        mRelativeDownload?.setOnClickListener(this)
        mRelativeProgressBar?.setOnClickListener(this)
    }
    private val DEFAULT = 0
    private val DOWNLOAD = 1
    private val PROGRESS = 2
    //记录当前的模式
    var mode = DEFAULT

    override fun onFinishInflate() {
        super.onFinishInflate()
        showView(DEFAULT)
    }
    private fun showView(mode:Int) {
        when(mode){
            DEFAULT ->{
                mRelativeDefault?.visibility = View.VISIBLE
                mRelativeDownload?.visibility = View.GONE
                mRelativeProgressBar?.visibility = View.GONE
            }
            DOWNLOAD ->{
                mRelativeDefault?.visibility = View.GONE
                mRelativeDownload?.visibility = View.VISIBLE
                mRelativeProgressBar?.visibility = View.GONE
            }
            PROGRESS ->{
                mRelativeDefault?.visibility = View.GONE
                mRelativeDownload?.visibility = View.GONE
                mRelativeProgressBar?.visibility = View.VISIBLE
            }
        }
    }
    var start = 10
    var second = 30
    override fun onClick(v: View) {
        when(v.id){
            R.id.rl_qrcode_select_default ->{
                mDefaultClickListener?.itemClick()
            }
//            R.id.rl_qrcode_select_download ->{
//                mode = 2
//                start+=10
//                second+=10
//                mProgressBar?.progress = start
//                mProgressBar?.secondaryProgress = second
//            }
//            R.id.rl_qrcode_select_progress_bar ->{
//
//            }
        }

    }



    interface DefaultClickListener{
        fun itemClick()
    }
    interface DownloadListener{
        fun isFinished()
    }
}
