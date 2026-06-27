package com.clean.wood

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.clean.wood.data.AdManager
import com.clean.wood.databinding.WoodActivitySplashBinding
import com.clean.wood.ui.PermissionResultContract
import com.clean.wood.ui.dialog.CheckPermissionDialog
import com.clean.wood.utils.Constant
import com.clean.wood.utils.PollingTask
import com.clean.wood.utils.log
import com.clean.wood.vm.SplashViewModel
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_FROM_BG = "fromBg"
        fun start(context: Context) {
            context.startActivity(Intent(context, SplashActivity::class.java).apply {
                putExtra(EXTRA_FROM_BG, true)
            })
        }
    }

    private val mViewModel by viewModels<SplashViewModel>()
    private lateinit var mBinding: WoodActivitySplashBinding

    private val mPermissionLauncher =
        registerForActivityResult(PermissionResultContract()) { result ->
            if (result) {
                handleAgreePermission()
            }
        }

    private val mPermissionDialog by lazy {
        CheckPermissionDialog(this).apply {
            callback = object : CheckPermissionDialog.Callback {
                override fun onClickGo() {
                    mViewModel.clickAgreePermission = true
                    requestPermission(this@SplashActivity)
                }

                override fun onClickClose() {
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = WoodActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        enableEdgeToEdge()

        mViewModel.fromBg = intent.getBooleanExtra(EXTRA_FROM_BG, false)

        onBackPressedDispatcher.addCallback {

        }

        initObserver()

        mViewModel.startProgress()

        mViewModel.checkAd()

        waitingAd(check = {
            AdManager.ins.isAdReadyNext(Constant.AdPosition.SplashInter)
        }, next = {
            mViewModel.finishProgress()
        }, timeOut = {
            //=> waiting progress 100
        })

    }

    override fun onStart() {
        super.onStart()
        //fix for go setting agree permission, start splash twice
        if (mViewModel.clickAgreePermission) {
            WoodApp.alreadyInBackground = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (mViewModel.fromBg) {
            mViewModel.cancelProgress()
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObserver() {
        mViewModel.progressLiveData.observe(this) {
            mBinding.progressBar.progress = it
            mBinding.tvProgress.text = "${it}%"

            if (it == 100) {
                //for fix launch Splash twice
                if (!mViewModel.fromBg) {
                    WoodApp.alreadyInBackground = false
                }

                lifecycleScope.launch {
                    AdManager.ins.showInterAd(Constant.AdPosition.SplashInter)

                    val hasPermission = checkPermission(this@SplashActivity)
                    if (hasPermission) {
                        handleAgreePermission()
                    } else {
                        mPermissionDialog.show()
                    }
                }
            }
        }
    }

    private fun handleAgreePermission() {
        mPermissionDialog.dismiss()
        if (mViewModel.fromBg) {
            finish()
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun checkPermission(context: Context): Boolean {
        val hasPermission: Boolean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED || Environment.isExternalStorageManager()
            log("checkPermission: hasPermission = $hasPermission")
        } else {
            hasPermission =
                PermissionX.isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return hasPermission
    }

    private fun requestPermission(context: FragmentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mPermissionLauncher.launch(null)
        } else {
            PermissionX.init(context)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        handleAgreePermission()
                    }
                }
        }
    }

    private fun waitingAd(
        check: () -> Boolean,
        next: () -> Unit,
        timeOut: () -> Unit,
        scene: String = this::class.simpleName ?: "default"
    ) {
        lifecycleScope.launch {
            delay(Constant.ANIMATION_MIN_DURATION)
            val interval = 500L
            val retryCount =
                (Constant.ANIMATION_MAX_DURATION - Constant.ANIMATION_MIN_DURATION) / interval
            PollingTask(
                this@SplashActivity,
                interval,
                mTaskName = scene,
                mMaxRetry = retryCount.toInt(),
                mCondition = {
                    check()
                },
                mExecute = {
                    next.invoke()
                },
                mOnFail = {
                    timeOut.invoke()
                }).start()
        }
    }
}