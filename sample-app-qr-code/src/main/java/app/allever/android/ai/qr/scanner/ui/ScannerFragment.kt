package app.allever.android.ai.qr.scanner.ui

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.ToastUtils
import android.view.*
import android.widget.*
import com.android.absbase.utils.DeviceUtils
import com.google.zxing.*
import com.google.zxing.client.android.*
import com.google.zxing.client.android.camera.CameraManager
import com.google.zxing.client.android.clipboard.ClipboardInterface
import com.google.zxing.client.android.history.HistoryManager
import com.google.zxing.client.android.result.ResultHandler
import com.google.zxing.utils.BitmapUtils
import app.allever.android.ai.qr.scanner.Config
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.core.preview.PreviewResultFragment
import app.allever.android.ai.qr.scanner.core.result.ResultHandlerFactory
import app.allever.android.ai.qr.scanner.ui.widget.LoadingView
import app.allever.android.lib.recommend.ui.RecommendDialog
import app.allever.android.lib.recommend.ui.RecommendListActivity
import app.allever.android.lib.recommend.util.ShakeViewContainer
import app.android.base.lib.util.PermissionHelper
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.callback.RequestCallback
import java.io.IOException
import java.util.*

class ScannerFragment() : BaseFragment(), SurfaceHolder.Callback, CaptureHolder, View.OnClickListener {
    private val TAG = CaptureActivity::class.java.simpleName

    private var MAX_CAMERA_ZOOM = 0
    private var MIN_CAMERA_ZOOM = 0
    private var CAMERA_ZOOM_STEP = 0

    private val DEFAULT_INTENT_RESULT_DURATION_MS = 1500L
    private val BULK_MODE_SCAN_DELAY_MS = 1000L

    private val ZXING_URLS = arrayOf("http://zxing.appspot.com/scan", "zxing://scan/")

    private val HISTORY_REQUEST_CODE = 0x0000bacc

    private val DISPLAYABLE_METADATA_TYPES = EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
            ResultMetadataType.SUGGESTED_PRICE,
            ResultMetadataType.ERROR_CORRECTION_LEVEL,
            ResultMetadataType.POSSIBLE_COUNTRY)

    private var handler: CaptureActivityHandler? = null
    //  private Result savedResultToShow;
    private var savedResultMessage: Message? = null
    private var surfaceView: SurfaceView? = null
    private var flashlight: ImageView? = null
    private var album: ImageView? = null
    private var recommend: ImageView? = null
    private var viewfinderView: ViewfinderView? = null
    private var statusView: TextView? = null
    private var cameraZoomFar: View? = null
    private var cameraZoomNear: View? = null
    private var cameraZoomSeekBar: SeekBar? = null
    private var mLoadingView: LoadingView? = null
    private var lastResult: Result? = null
    private var hasSurface: Boolean = false
    private var copyToClipboard: Boolean = false
    private var source: IntentSource? = null
    private var sourceUrl: String? = null
    private var scanFromWebPageManager: ScanFromWebPageManager? = null
    private var decodeFormats: Collection<BarcodeFormat>? = null
    private var decodeHints: Map<DecodeHintType, *>? = null
    private var characterSet: String? = null
    private var historyManager: HistoryManager? = null
    private lateinit var cameraManager: CameraManager
    private lateinit var inactivityTimer: InactivityTimer
    private lateinit var beepManager: BeepManager
    private lateinit var ambientLightManager: AmbientLightManager
    private var alreadyResume: Boolean = false
    private var alreadyPause: Boolean = true
    private var isInit = false
    private var decodeImging = false
    private var mainToneColor: Int = 0

    private var mHasResultNeedResumeShow = false
    private var mCurrentShowResult = false

    private var currentCameraZoomValue: Int = 0
        set(value) {
            if (!cameraManager.isZoomSupported) {
                field = 0
                return
            }
            if (value > cameraManager.maxZoom ?: 0 || value < 0) {
                return
            }
            field = value
            cameraManager.zoom = value
            cameraZoomSeekBar?.progress = value - MIN_CAMERA_ZOOM
        }

    private var mContainer: View? = null

    var intent: Intent? = null

//    var permissionManager: BasePermissionManager? = null
    var permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_EXTERNAL_STORAGE)
    private val mStorePermissionsList = ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val mCameraPermissions = ArrayList<String>().apply {
        add(Manifest.permission.CAMERA)
    }


    private lateinit var mShakeViewContainer: ShakeViewContainer

    private fun checkStorePermission(): Boolean {
        return PermissionHelper.hasPermissionOrigin(activity, mStorePermissionsList)
    }

    private fun checkCameraPermission(): Boolean {
        return PermissionHelper.hasPermissionOrigin(activity, mCameraPermissions)
    }

    override fun getViewfinderView(): ViewfinderView? {
        return viewfinderView
    }

    override fun getHandler(): Handler? {
        return handler
    }

    override fun getCameraManager(): CameraManager? {
        return cameraManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window = activity?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        inactivityTimer = InactivityTimer(activity)
        beepManager = BeepManager(activity)
        ambientLightManager = AmbientLightManager(activity)
        cameraManager = CameraManager(activity?.application, true)

        val topMargin = resources.getDimensionPixelSize(R.dimen.main_scan_frame_top_margin)
        cameraManager.setFrameTopMargin(topMargin)
        val frameWidth = resources.getDimensionPixelSize(R.dimen.main_scan_frame_width)
        cameraManager.setFrameSize(frameWidth, frameWidth)

        PreferenceManager.setDefaultValues(activity, R.xml.setting_preferences, false)

//        permissionManager = EasyPermissionManager()
    }

    override fun onStart() {
        super.onStart()
        //申请相机权限
//        requestCameraPermission()
        PermissionX.init(this).permissions(mCameraPermissions).request(object : RequestCallback {
            override fun onResult(allGranted: Boolean, grantedList: List<String>, deniedList: List<String>) {
                if (allGranted) {
                    //申请成功
                    mMainHandler.removeMessages(WHAT_RESUME)
                    mMainHandler.removeMessages(WHAT_PAUSE)
                    mMainHandler.sendEmptyMessage(WHAT_RESUME)
                } else {
                    //申请失败
                    //判断是否总是拒绝权限
                    if (PermissionHelper.hasAlwaysDeniedPermissionOrigin(activity, deniedList)) {
                        ToastUtils.show(getString(R.string.tips_no_camera_permission))
                        //弹窗引导用户去设置界面打开权限
                        AlertDialog.Builder(activity)
                            .setTitle("Tips")
                            .setMessage(getString(R.string.tips_no_camera_permission) + " ,go setting")
                            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface?, which: Int) {
                                    dialog?.dismiss()
                                    requireActivity().finish()
                                }
                            })
                            .setPositiveButton("Go", object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                dialog?.dismiss()
                                PermissionHelper.gotoSettingOrigin(requireActivity())
                            }
                        }).show()
                    } else {
                        ToastUtils.show(getString(R.string.tips_no_camera_permission))
                        activity?.finish()
                    }

                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {


        mainToneColor = resources.getColor(R.color.main_tone_color)

        mContainer = inflater.inflate(R.layout.fragment_capture, container, false)
        surfaceView = mContainer?.findViewById<View>(com.google.zxing.client.android.R.id.preview_view) as SurfaceView
        surfaceView?.holder?.addCallback(this)
        viewfinderView = mContainer?.findViewById<View>(R.id.viewfinder_view) as ViewfinderView
        viewfinderView?.setLaserColor(resources.getColor(R.color.main_scan_laser_color), resources.getColor(R.color.main_scan_laser_tail_color))
        viewfinderView?.setFrameBorderCornersColor(mainToneColor)
        viewfinderView?.setMaskColor(resources.getColor(R.color.main_scan_mask))

//        resultView = mContainer?.findViewById<View>(R.id.result_view)
        statusView = mContainer?.findViewById<View>(R.id.status_view) as TextView
        flashlight = mContainer?.findViewById<ImageView>(R.id.btn_flashlight)
        album = mContainer?.findViewById<ImageView>(R.id.btn_album)
        recommend = mContainer?.findViewById<ImageView>(R.id.btnRecommend)
        mShakeViewContainer = ShakeViewContainer(recommend!!)
        mShakeViewContainer.start()
        val btnPremium = mContainer?.findViewById<ImageView>(R.id.btn_premium)
        btnPremium?.setOnClickListener(this)
        if(Config.purchaseSubSize >0){
            btnPremium?.visibility = View.GONE
        }else{
            btnPremium?.visibility = View.VISIBLE
        }
        flashlight?.setOnClickListener(this)
        album?.setOnClickListener(this)
        recommend?.setOnClickListener(this)

        mContainer?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                mContainer?.viewTreeObserver?.removeOnPreDrawListener(this)

                val rect = Rect()
                mContainer?.getGlobalVisibleRect(rect)
                val point = Point(0, rect.top)
                cameraManager.setPreviewOffset(point)
                return false
            }
        })

        cameraZoomFar = mContainer?.findViewById(R.id.camera_zoom_far)
        cameraZoomFar?.setOnClickListener(this)
        cameraZoomNear = mContainer?.findViewById(R.id.camera_zoom_near)
        cameraZoomNear?.setOnClickListener(this)
        cameraZoomSeekBar = mContainer?.findViewById(R.id.camera_zoom_seekbar)
        cameraZoomSeekBar?.max = (MAX_CAMERA_ZOOM - MIN_CAMERA_ZOOM)
        cameraZoomSeekBar?.progress = 0
        cameraZoomSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                val value = progress + MIN_CAMERA_ZOOM
                currentCameraZoomValue = value
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        mLoadingView = mContainer?.findViewById<LoadingView>(R.id.loading_view);

        hasSurface = false
        isInit = true

        updateFinderView()

        return mContainer
    }

    private fun updateFinderView() {
        viewfinderView?.viewTreeObserver?.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val viewfinderView = viewfinderView ?: return false
                viewfinderView.viewTreeObserver?.removeOnPreDrawListener(this)
                val height = viewfinderView.height
                val bottomFunHeight = resources.getDimensionPixelSize(R.dimen.main_scan_frame_bottom_fun_height)
                val frameWidth = resources.getDimensionPixelSize(R.dimen.main_scan_frame_width)
                val top = (height - bottomFunHeight - frameWidth) shr 1
                cameraManager.setFrameTopMargin(top)
                cameraManager.setFrameSize(frameWidth, frameWidth)

                val statusView = statusView
                if (statusView != null) {
                    val layoutParams = statusView.layoutParams as? RelativeLayout.LayoutParams
                    if (layoutParams != null) {
                        var height = statusView.height
                        if (height == 0) {
                            height = DeviceUtils.dip2px(20f)
                        }
                        layoutParams.topMargin = top - height - DeviceUtils.dip2px(30f)
                    }
                }

                return false
            }
        })
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isInit) {
            return
        }
        if (isVisibleToUser) {
//            onResumeImpl()
            mMainHandler.removeMessages(WHAT_RESUME)
            mMainHandler.removeMessages(WHAT_PAUSE)
            mMainHandler.sendEmptyMessage(WHAT_RESUME)

            updateFinderView()
        } else {
//            onPauseAsync()
            mMainHandler.removeMessages(WHAT_RESUME)
            mMainHandler.removeMessages(WHAT_PAUSE)
            mMainHandler.sendEmptyMessage(WHAT_PAUSE)
        }

    }

    override fun onResume() {
        super.onResume()
//        onResumeImpl()
        mMainHandler.removeMessages(WHAT_RESUME)
        mMainHandler.removeMessages(WHAT_PAUSE)
        mMainHandler.sendEmptyMessage(WHAT_RESUME)

    }

    override fun onPause() {
        super.onPause()
//        onPauseAsync()
        mMainHandler.removeMessages(WHAT_RESUME)
        mMainHandler.removeMessages(WHAT_PAUSE)
        mMainHandler.sendEmptyMessage(WHAT_PAUSE)
    }

    val WHAT_RESUME = 100
    val WHAT_PAUSE = 200
    val WHAT_FLAHLIGHT_ANIM = 300
    override fun handleMessage(message: Message): Boolean {
        // 不使用异步，使用异步后从启动页进入会闪一下，设置window.setFormat(PixelFormat.TRANSLUCENT)也不起作用，暂未找到原因
        when (message?.what) {
            WHAT_RESUME -> {
                if (PermissionHelper.hasPermissionOrigin(requireActivity(), mCameraPermissions)) {
                    onResumeImpl()
                }
            }
            WHAT_PAUSE -> onPauseImpl()
            WHAT_FLAHLIGHT_ANIM -> startFlashlightAnim()
        }
        return true
    }

//    private fun onResumeAsync() {
//        // 异步出现相机无法多次自动对焦，而且PreviewCallback不回调
//        TaskRunnable.run({
//            val myLooper = Looper.myLooper()
//            if (myLooper == null) {
//                Looper.prepare()
//            }
//            onResumeImpl()
//        }, 0, TaskRunnable.TYPE_BACKGROUND)
//    }
//
//    private fun onPauseAsync() {
//        TaskRunnable.run({
//            val myLooper = Looper.myLooper()
//            if (myLooper == null) {
//                Looper.prepare()
//            }
//            onPauseImpl()
//        }, 0, TaskRunnable.TYPE_BACKGROUND)
//    }

    private fun onResumeImpl() {
        if (activity == null) {
            return
        }
        if (!userVisibleHint) {
            return
        }
        if (alreadyResume) {
            return
        }
        alreadyResume = true
        alreadyPause = false
        // historyManager must be initialized here to update the history preference
        if (historyManager == null) {
            historyManager = HistoryManager(activity)
        }
        historyManager!!.trimHistory()

        viewfinderView?.setCameraManager(cameraManager)

//        handler = null
//        lastResult = null

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (prefs.getBoolean(PreferencesActivity.KEY_DISABLE_AUTO_ORIENTATION, true)) {
            activity?.requestedOrientation = getCurrentOrientation()
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        checkNeedShowResult()
        if (decodeImging) {
            showResultView(true)
//        showResultView(decodeImging || mCurrentShowResult
        }

        beepManager.updatePrefs()
        ambientLightManager.start(cameraManager) {
            if (it) {
                if (mMainHandler.hasMessages(WHAT_FLAHLIGHT_ANIM)) {
//                    mMainHandler.sendEmptyMessageDelayed(WHAT_FLAHLIGHT_ANIM, 2000)
                } else {
                    mMainHandler.sendEmptyMessage(WHAT_FLAHLIGHT_ANIM)
                }
            } else {
                mMainHandler.removeMessages(WHAT_FLAHLIGHT_ANIM)
            }
        }

        inactivityTimer.onResume()

        copyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, true) && (intent == null || intent!!.getBooleanExtra(Intents.Scan.SAVE_HISTORY, true))

        source = IntentSource.NONE
        sourceUrl = null
        scanFromWebPageManager = null
        decodeFormats = null
        characterSet = null

        if (intent != null) {

            val action = intent!!.action
            val dataString = intent!!.dataString

            if (Intents.Scan.ACTION == action) {

                // Scan the formats the intent requested, and return the result to the calling activity.
                source = IntentSource.NATIVE_APP_INTENT
                decodeFormats = DecodeFormatManager.parseDecodeFormats(intent!!)
                decodeHints = DecodeHintManager.parseDecodeHints(intent!!)

                if (intent!!.hasExtra(Intents.Scan.WIDTH) && intent!!.hasExtra(Intents.Scan.HEIGHT)) {
                    val width = intent!!.getIntExtra(Intents.Scan.WIDTH, 0)
                    val height = intent!!.getIntExtra(Intents.Scan.HEIGHT, 0)
                    if (width > 0 && height > 0) {
                        cameraManager.setManualFramingRect(width, height)
                    }
                }

                if (intent!!.hasExtra(Intents.Scan.CAMERA_ID)) {
                    val cameraId = intent!!.getIntExtra(Intents.Scan.CAMERA_ID, -1)
                    if (cameraId >= 0) {
                        cameraManager.setManualCameraId(cameraId)
                    }
                }

                val customPromptMessage = intent!!.getStringExtra(Intents.Scan.PROMPT_MESSAGE)
                if (customPromptMessage != null) {
                    statusView?.text = customPromptMessage
                }

            } else if (dataString != null &&
                    dataString.contains("http://www.google") &&
                    dataString.contains("/m/products/scan")) {

                // Scan only products and send the result to mobile Product Search.
                source = IntentSource.PRODUCT_SEARCH_LINK
                sourceUrl = dataString
                decodeFormats = DecodeFormatManager.PRODUCT_FORMATS

            } else if (isZXingURL(dataString)) {

                // Scan formats requested in query string (all formats if none specified).
                // If a return URL is specified, send the results there. Otherwise, handle it ourselves.
                source = IntentSource.ZXING_LINK
                sourceUrl = dataString
                val inputUri = Uri.parse(dataString)
                scanFromWebPageManager = ScanFromWebPageManager(inputUri)
                decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri)
                // Allow a sub-set of the hints to be specified by the caller.
                decodeHints = DecodeHintManager.parseDecodeHints(inputUri)

            }

            characterSet = intent!!.getStringExtra(Intents.Scan.CHARACTER_SET)

        }

        val surfaceHolder = surfaceView?.holder
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder)
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
//            surfaceHolder?.addCallback(this)
        }
    }

    fun onPauseImpl() {
        if (alreadyPause) {
            return
        }
        alreadyPause = true
        alreadyResume = false
        if (handler != null) {
            handler!!.quitSynchronously()
            handler = null
        }
        inactivityTimer.onPause()
        ambientLightManager.stop()
        beepManager.close()
        cameraManager.closeDriver()
        //historyManager = null; // Keep for onActivityResult
//        if (!hasSurface) {
//            val surfaceView = mContainer?.findViewById<View>(R.id.preview_view) as SurfaceView
//            val surfaceHolder = surfaceView.holder
//            surfaceHolder.removeCallback(this)
//        }
    }

    private fun getCurrentOrientation(): Int {
        val rotation = activity?.windowManager?.defaultDisplay?.rotation
        return if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            when (rotation) {
                Surface.ROTATION_0, Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            }
        } else {
            when (rotation) {
                Surface.ROTATION_0, Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                else -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            }
        }
    }

    private fun isZXingURL(dataString: String?): Boolean {
        if (dataString == null) {
            return false
        }
        for (url in ZXING_URLS) {
            if (dataString.startsWith(url)) {
                return true
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isInit = false
        surfaceView?.holder?.removeCallback(this)
        mShakeViewContainer.stop()

    }

    override fun onDestroy() {
        inactivityTimer.shutdown()
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_flashlight -> {
                val torchState = !cameraManager.torchState
                cameraManager.setTorch(torchState)
                mFlashStopAnimator = torchState
                setFlashState(torchState)
            }
            R.id.btn_premium -> { }
            R.id.btn_album -> {
                if (checkStorePermission()) {
                    SystemAlbumHelper.start(this)
                } else {
                    PermissionX.init(this).permissions(mStorePermissionsList).request(object: RequestCallback {
                        override fun onResult(
                            allGranted: Boolean,
                            grantedList: MutableList<String>,
                            deniedList: MutableList<String>
                        ) {
                            if (allGranted) {
                                SystemAlbumHelper.start(this)
                            } else {
                                ToastUtils.show(getString(R.string.tips_no_read_storage_permission))
                            }
                        }

                    })
//                    permissionManager?.requestPermission(this,
//                            null,
//                            resources.getString(R.string.tips_ration_read),
//                            RC_PERMISSION_READ_EXTERNAL_STORAGE,
//                            *permissions)
                }
            }
            R.id.btnRecommend -> {
                //跳转RecommendActivity
                startActivity(Intent(activity, RecommendListActivity::class.java))
            }
            R.id.camera_zoom_far -> {
                currentCameraZoomValue -= CAMERA_ZOOM_STEP
            }
            R.id.camera_zoom_near -> {
                currentCameraZoomValue += CAMERA_ZOOM_STEP
            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                if (source == IntentSource.NATIVE_APP_INTENT) {
                    activity?.setResult(Activity.RESULT_CANCELED)
                    activity?.finish()
                    return true
                }
                if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
                    restartPreviewAfterDelay(0L)
                    return true
                }
//                if (resultView?.visibility == View.VISIBLE) {
//                    showResultView(false)
//                    return true
//                }
            }
            KeyEvent.KEYCODE_FOCUS, KeyEvent.KEYCODE_CAMERA ->
                // Handle these events so they don't launch the Camera app
                return true
            // Use volume up/down to turn on light
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
//                cameraManager!!.setTorch(false)
                currentCameraZoomValue -= CAMERA_ZOOM_STEP
                return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
//                cameraManager!!.setTorch(true)
                currentCameraZoomValue += CAMERA_ZOOM_STEP
                return true
            }
        }
        return false
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.addFlags(Intents.FLAG_NEW_DOC)
//        val i = item.itemId
//        if (i == com.google.zxing.client.android.R.id.menu_share) {
//            intent.setClassName(this, ShareActivity::class.java.name)
//            startActivity(intent)
//        } else if (i == com.google.zxing.client.android.R.id.menu_decode_img) {
//            SystemAlbumHelper.start(this)
//        } else if (i == com.google.zxing.client.android.R.id.menu_history) {
//            intent.setClassName(this, HistoryActivity::class.java.name)
//            startActivityForResult(intent, HISTORY_REQUEST_CODE)
//
//        } else if (i == com.google.zxing.client.android.R.id.menu_settings) {
//            intent.setClassName(this, PreferencesActivity::class.java.name)
//            startActivity(intent)
//
//        } else if (i == com.google.zxing.client.android.R.id.menu_help) {
//            intent.setClassName(this, HelpActivity::class.java.name)
//            startActivity(intent)
//
//            //      case R.id.menu_about:
//            //        intent.setClassName(this, AboutActivity.class.getName());
//            //        startActivity(intent);
//            //        break;
//        } else {
//            return super.onOptionsItemSelected(item)
//        }
//        return true
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == HISTORY_REQUEST_CODE && historyManager != null && intent != null) {
            val itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1)
            if (itemNumber >= 0) {
                val historyItem = historyManager!!.buildHistoryItem(itemNumber)
                decodeOrStoreSavedBitmap(null, 0f, historyItem.result)
            }
        } else if (requestCode == SystemAlbumHelper.REQUEST_CODE_IMG_SELECTION) {
            val imagePath = SystemAlbumHelper.handleActivityResult(requireActivity(), requestCode, resultCode, intent)
            if (imagePath != null) {
                //        if (handler == null) {
                //          handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
                //        }
                //        handler.decodeByImagePath(imagePath);
                val qrReader = QRReader()
                decodeImging = true
                mLoadingView?.startLoading()
                qrReader.startAsync(imagePath, object : QRReaderListener {
                    override fun complete(result: Result?) {
                        if (result != null) {
                            val bitmap = qrReader.getThumbnail()
                            val caleFactor = qrReader.getThumbnailScaleFactor()
                            decodeOrStoreSavedBitmap(bitmap, caleFactor, result)
                            bitmap?.recycle()
                        } else {
                            ToastUtils.show("Not recognized")
                            restartPreviewAfterDelay(0)
                        }
                        qrReader.destroy()
                        decodeImging = false
                        post2UiThreadIfNeed {
                            mLoadingView?.stopLoading()
                        }
                    }
                })
            } else {
                ToastUtils.show("img is null")
            }
        }
    }

    var mFlashStopAnimator: Boolean = false
    var mFlashlightAnimator: ValueAnimator? = null
    private fun startFlashlightAnim() {
        if (mFlashlightAnimator != null) {
            return
        }
        if (cameraManager.torchState || mFlashStopAnimator) {
            mFlashlightAnimator?.cancel()
            return
        }
        val animator = ValueAnimator.ofInt(0, 6)
        animator.duration = 1000
        animator.addUpdateListener {
            if (mFlashStopAnimator) {
                return@addUpdateListener
            }
            if ((it.animatedValue as Int).rem(2) == 0) {
                flashlight?.colorFilter = null
            } else {
                flashlight?.setColorFilter(mainToneColor)
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                mFlashlightAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationStart(animation: Animator) {
            }

        })
        mFlashlightAnimator = animator
        animator.start()
    }

    private fun decodeOrStoreSavedBitmap() {
        val msg = savedResultMessage
        if (handler != null && msg != null) {
            handler?.removeMessages(msg.what)
            handler?.sendMessage(msg)
        }
        savedResultMessage = null
    }

    private fun decodeOrStoreSavedBitmap(bitmap: Bitmap?, caleFactor: Float, result: Result?) {
        // Bitmap isn't used yet -- will be used soon
        val msg = Message.obtain(handler, com.google.zxing.client.android.R.id.decode_succeeded, result)
        if (bitmap != null) {
            val bundle = Bundle()
            val byteArray = BitmapUtils.getByteArray(bitmap)
            bundle.putByteArray(DecodeThread.BARCODE_BITMAP, byteArray)
            bundle.putFloat(DecodeThread.BARCODE_SCALED_FACTOR, caleFactor)
            msg.data = bundle
        }
        savedResultMessage = msg
        if (handler != null) {
            decodeOrStoreSavedBitmap()
            savedResultMessage = null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (holder == null) {
            DLog.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!")
        }

        if (!hasSurface) {
            hasSurface = true
            if (alreadyResume) {
                initCamera(holder)
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
        surfaceView?.holder?.removeCallback(this)
        surfaceView?.holder?.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // do nothing
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode   A greyscale bitmap of the camera data which was decoded.
     */
    override fun handleDecode(rawResult: Result, barcode: Bitmap?, scaleFactor: Float) {
        inactivityTimer.onActivity()
        lastResult = rawResult
        val resultHandler = ResultHandlerFactory.makeResultHandler(activity, rawResult)

        val fromLiveScan = barcode != null
        if (fromLiveScan) {
            historyManager?.addHistoryItem(activity, rawResult, resultHandler)
            // Then not from history, so beep/vibrate and we have an image to draw on
            beepManager.playBeepSoundAndVibrate()
            drawResultPoints(barcode, scaleFactor, rawResult)
        }

        when (source) {
            IntentSource.NATIVE_APP_INTENT, IntentSource.PRODUCT_SEARCH_LINK -> handleDecodeExternally(rawResult, resultHandler, barcode)
            IntentSource.ZXING_LINK -> if (scanFromWebPageManager == null || !scanFromWebPageManager!!.isScanFromWebPage()) {
                handleDecodeInternally(rawResult, resultHandler, barcode)
            } else {
                handleDecodeExternally(rawResult, resultHandler, barcode)
            }
            IntentSource.NONE -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                if (fromLiveScan && prefs.getBoolean(PreferencesActivity.KEY_BULK_MODE, false)) {
                    Toast.makeText(activity?.applicationContext,
                            resources.getString(com.google.zxing.client.android.R.string.msg_bulk_mode_scanned) + " (" + rawResult.text + ')'.toString(),
                            Toast.LENGTH_SHORT).show()
                    maybeSetClipboard(resultHandler)
                    // Wait a moment or else it will scan the same barcode continuously about 3 times
                    restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS)
                } else {
                    handleDecodeInternally(rawResult, resultHandler, barcode)
                }
            }

            null -> {}
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode   A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult The decoded results which contains the points to draw.
     */
    private fun drawResultPoints(barcode: Bitmap?, scaleFactor: Float, rawResult: Result) {
        val points = rawResult.resultPoints
        if (points != null && points.size > 0) {
            val canvas = Canvas(barcode!!)
            val paint = Paint()
            paint.color = resources.getColor(com.google.zxing.client.android.R.color.result_points)
            if (points.size == 2) {
                paint.strokeWidth = 4.0f
                drawLine(canvas, paint, points[0], points[1], scaleFactor)
            } else if (points.size == 4 && (rawResult.barcodeFormat == BarcodeFormat.UPC_A || rawResult.barcodeFormat == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(canvas, paint, points[0], points[1], scaleFactor)
                drawLine(canvas, paint, points[2], points[3], scaleFactor)
            } else {
                paint.strokeWidth = 10.0f
                for (point in points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.x, scaleFactor * point.y, paint)
                    }
                }
            }
        }
    }

    private fun drawLine(canvas: Canvas, paint: Paint, a: ResultPoint?, b: ResultPoint?, scaleFactor: Float) {
        if (a != null && b != null) {
            canvas.drawLine(scaleFactor * a.x,
                    scaleFactor * a.y,
                    scaleFactor * b.x,
                    scaleFactor * b.y,
                    paint)
        }
    }

    private var rawResult: Result? = null
    private var barcode: Bitmap? = null
    private fun delayShowResult(rawResult: Result, barcode: Bitmap?) {
        this.rawResult = rawResult
        this.barcode = barcode
        this.mHasResultNeedResumeShow = true
    }

    @Synchronized
    private fun checkNeedShowResult() {
        if (this.mHasResultNeedResumeShow) {
            showResultView(true)
            this.mHasResultNeedResumeShow = false
            val rawResult = this.rawResult
            val barcode = this.barcode
            if (rawResult != null) {
                showResult(rawResult, barcode)
            }
            this.rawResult = null
            this.barcode = null
        } else if (!mCurrentShowResult){
            showResultView(false)
        }
    }

    private fun showResult(rawResult: Result, barcode: Bitmap?) {
        mCurrentShowResult = true
        PreviewResultFragment.show(this, rawResult, barcode, object : PreviewResultFragment.OnPreviewResultListener {
            override fun onCancel() {
                mCurrentShowResult = false
                restartPreviewAfterDelay(0L)
                lastResult = null
            }

            override fun onDismiss() {
//                val context = context
//                if (context != null) {
//                    RateGuide.Builder().setSpecificAction(true).show(context)
//                }
            }

            override fun onShow() {
                showResultView(true)
            }
        })
    }

    // Put up our own UI for how to handle the decoded contents.
    private fun handleDecodeInternally(rawResult: Result, resultHandler: ResultHandler, barcode: Bitmap?) {

        maybeSetClipboard(resultHandler)
        showResultView(true)
        showResult(rawResult, barcode)
    }

    // Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
    private fun handleDecodeExternally(rawResult: Result, resultHandler: ResultHandler, barcode: Bitmap?) {

        if (barcode != null) {
            viewfinderView!!.drawResultBitmap(barcode)
        }

        val resultDurationMS = intent?.getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS,
                DEFAULT_INTENT_RESULT_DURATION_MS) ?: DEFAULT_INTENT_RESULT_DURATION_MS

        if (resultDurationMS > 0) {
            var rawResultString = rawResult.toString()
            if (rawResultString.length > 32) {
                rawResultString = rawResultString.substring(0, 32) + " ..."
            }
            statusView?.text = getString(resultHandler.displayTitle) + " : " + rawResultString
        }

        maybeSetClipboard(resultHandler)

        when (source) {
            IntentSource.NATIVE_APP_INTENT -> {
                // Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
                // the deprecated intent is retired.
                val intent = Intent(intent!!.action)
                intent.addFlags(Intents.FLAG_NEW_DOC)
                intent.putExtra(Intents.Scan.RESULT, rawResult.toString())
                intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.barcodeFormat.toString())
                val rawBytes = rawResult.rawBytes
                if (rawBytes != null && rawBytes.size > 0) {
                    intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes)
                }
                val metadata: Map<ResultMetadataType, Any?>? = rawResult.resultMetadata
                if (metadata != null) {
                    if (metadata.containsKey(ResultMetadataType.UPC_EAN_EXTENSION)) {
                        intent.putExtra(Intents.Scan.RESULT_UPC_EAN_EXTENSION,
                                metadata[ResultMetadataType.UPC_EAN_EXTENSION].toString())
                    }
                    val orientation = metadata[ResultMetadataType.ORIENTATION] as? Number
                    if (orientation != null) {
                        intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.toInt())
                    }
                    val ecLevel = metadata[ResultMetadataType.ERROR_CORRECTION_LEVEL] as? String
                    if (ecLevel != null) {
                        intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel)
                    }
                    val byteSegments = metadata[ResultMetadataType.BYTE_SEGMENTS] as? Iterable<*>
                    if (byteSegments != null) {
                        var i = 0
                        for (byteSegment in byteSegments) {
                            if (byteSegment is ByteArray) {
                                intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment)
                                i++
                            }
                        }
                    }
                }
                sendReplyMessage(com.google.zxing.client.android.R.id.return_scan_result, intent, resultDurationMS)
            }

            IntentSource.PRODUCT_SEARCH_LINK -> {
                // Reformulate the URL which triggered us into a query, so that the request goes to the same
                // TLD as the scan URL.
                val end = sourceUrl!!.lastIndexOf("/scan")
                val productReplyURL = sourceUrl!!.substring(0, end) + "?q=" +
                        resultHandler.displayContents + "&source=zxing"
                sendReplyMessage(com.google.zxing.client.android.R.id.launch_product_query, productReplyURL, resultDurationMS)
            }

            IntentSource.ZXING_LINK -> if (scanFromWebPageManager != null && scanFromWebPageManager!!.isScanFromWebPage()) {
                val linkReplyURL = scanFromWebPageManager!!.buildReplyURL(rawResult, resultHandler)
                scanFromWebPageManager = null
                sendReplyMessage(com.google.zxing.client.android.R.id.launch_product_query, linkReplyURL, resultDurationMS)
            }

            IntentSource.NONE -> {}
            null -> {}
        }
    }

    private fun maybeSetClipboard(resultHandler: ResultHandler) {
        if (copyToClipboard && !resultHandler.areContentsSecure()) {
            ClipboardInterface.setText(resultHandler.displayContents, context)
        }
    }

    private fun sendReplyMessage(id: Int, arg: Any, delayMS: Long) {
        if (handler != null) {
            val message = Message.obtain(handler, id, arg)
            if (delayMS > 0L) {
                handler!!.sendMessageDelayed(message, delayMS)
            } else {
                handler!!.sendMessage(message)
            }
        }
    }

    private fun initCamera(surfaceHolder: SurfaceHolder?) {
        if (surfaceHolder == null) {
            throw IllegalStateException("No SurfaceHolder provided")
        }
        if (cameraManager.isOpen) {
            DLog.w(TAG, "initCamera() while already open -- late SurfaceView callback?")
            return
        }
        try {
            cameraManager.openDriver(surfaceHolder)
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = CaptureActivityHandler(activity, this, decodeFormats, decodeHints, characterSet, !mCurrentShowResult)
            }
        } catch (ioe: IOException) {
            DLog.w(TAG, ioe.message)
            displayFrameworkBugMessageAndExit()
        } catch (e: RuntimeException) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            DLog.w(TAG, "Unexpected error initializing camera", e)
            displayFrameworkBugMessageAndExit()
        }
        decodeOrStoreSavedBitmap()
        resetCameraZoomState(true)
    }

    private fun displayFrameworkBugMessageAndExit() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.app_name))
        builder.setMessage(getString(com.google.zxing.client.android.R.string.msg_camera_framework_bug))
        builder.setPositiveButton(com.google.zxing.client.android.R.string.button_ok, FinishListener(activity))
        builder.setOnCancelListener(FinishListener(activity))
        builder.show()
    }

    override fun restartPreviewAfterDelay(delayMS: Long) {
        if (handler != null) {
            handler!!.sendEmptyMessageDelayed(com.google.zxing.client.android.R.id.restart_preview, delayMS)
        }
        showResultView(false)
    }

    private fun showResultView(show: Boolean) {
        if (post2UiThreadIfNeed {
                    showResultView(show)
                }) {
            return
        }
        if (show) {
            statusView?.visibility = View.GONE
            viewfinderView?.visibility = View.GONE
            flashlight?.visibility = View.GONE
//            album?.visibility = View.GONE
//            resultView?.visibility = View.VISIBLE
        } else {
//            resultView?.visibility = View.GONE
            statusView?.setText(R.string.main_scan_tips)
            statusView?.visibility = View.VISIBLE
            viewfinderView?.visibility = View.VISIBLE
            flashlight?.visibility = View.VISIBLE
//            album?.visibility = View.VISIBLE
        }
        resetCameraZoomState(!show)
        setFlashState(cameraManager.torchState)
    }

    private fun resetCameraZoomState(show: Boolean) {
        if (post2UiThreadIfNeed {
                    resetCameraZoomState(show)
                }) {
            return
        }

        val zoomSupported = cameraManager.isZoomSupported
        if (zoomSupported && show/* && resultView?.visibility != View.VISIBLE*/) {
            cameraZoomSeekBar?.visibility = View.VISIBLE
            cameraZoomFar?.visibility = View.VISIBLE
            cameraZoomNear?.visibility = View.VISIBLE
            MAX_CAMERA_ZOOM = cameraManager.maxZoom ?: 0
            CAMERA_ZOOM_STEP = (MAX_CAMERA_ZOOM - MIN_CAMERA_ZOOM) / 10
            cameraZoomSeekBar?.max = MAX_CAMERA_ZOOM - MIN_CAMERA_ZOOM
            currentCameraZoomValue = 0
        } else {
            cameraZoomSeekBar?.visibility = View.GONE
            cameraZoomFar?.visibility = View.GONE
            cameraZoomNear?.visibility = View.GONE
        }
    }

    private fun setFlashState(torchState: Boolean) {
        if (torchState) {
            flashlight?.setColorFilter(mainToneColor)
        } else {
            flashlight?.colorFilter = null
        }
    }

    private fun post2UiThreadIfNeed(runnable: () -> Unit): Boolean {
        return if (!isMainThread()) {
            mMainHandler.post(runnable)
            true
        } else {
            false
        }
    }

    override fun drawViewfinder() {
        if (post2UiThreadIfNeed {
                    drawViewfinder()
                }) {
            return
        }

        viewfinderView?.drawViewfinder()
    }


//    @AfterPermissionGranted(RC_PERMISSION_CAMERA)
//    fun requestCameraPermission() {
//        if (permissionManager?.hasPermissions(App.getContext(), *permissions) == true) {
//            mMainHandler.removeMessages(WHAT_RESUME)
//            mMainHandler.removeMessages(WHAT_PAUSE)
//            mMainHandler.sendEmptyMessage(WHAT_RESUME)
//        } else {
//            permissionManager?.requestPermission(this,
//                    null,
//                    resources.getString(R.string.tips_ration_camera),
//                    RC_PERMISSION_CAMERA,
//                    *permissions)
//        }
//    }

//    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
//        when(requestCode){
//            RC_PERMISSION_CAMERA ->{
//                //拒绝后判断是否勾选不再询问,是的会提示手动授权
//                if (permissionManager?.isNeverShowRequestPermission(this, perms) == true) {
//                    permissionManager?.jumpToSettingDialog(activity, RC_SETTING, "", "", "", "", { diaDLogInterface, i ->
//                        //cancelClick
//                        diaDLogInterface.dismiss()
//                        ToastUtils.show(getString(R.string.tips_no_camera_permission))
//                        activity?.finish()
//                    }, null)
//                } else {
//                    ToastUtils.show(getString(R.string.tips_no_camera_permission))
//                    activity?.finish()
//                }
//            }
//            RC_PERMISSION_READ_EXTERNAL_STORAGE -> {
//                //拒绝后判断是否勾选不再询问,是的会提示手动授权
//                if (permissionManager?.isNeverShowRequestPermission(this, perms) == true) {
//                    permissionManager?.jumpToSettingDialog(activity, RC_SETTING, "", "", "", "", { diaDLogInterface, i ->
//                        //cancelClick
//                        diaDLogInterface.dismiss()
//                        ToastUtils.show(getString(R.string.tips_no_read_storage_permission))
//                    }, null)
//                } else {
//                    ToastUtils.show(getString(R.string.tips_no_read_storage_permission))
//                }
//            }
//        }
//    }
//
//    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
//    }
//
//    override fun onRationaleDenied(requestCode: Int) {
//        when (requestCode) {
//            RC_PERMISSION_CAMERA -> {
//                ToastUtils.show(getString(R.string.tips_no_camera_permission))
//                activity?.finish()
//            }
//            RC_PERMISSION_READ_EXTERNAL_STORAGE -> ToastUtils.show(getString(R.string.tips_no_read_storage_permission))
//        }
//    }


    companion object {
        const val RC_PERMISSION_CAMERA = 0x01
        const val RC_SETTING = 0x02
        const val RC_PERMISSION_READ_EXTERNAL_STORAGE = 0x03
    }
}
