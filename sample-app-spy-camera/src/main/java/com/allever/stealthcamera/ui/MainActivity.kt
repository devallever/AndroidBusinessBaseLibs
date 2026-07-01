package com.allever.stealthcamera.ui

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.ext.toast

import com.allever.stealthcamera.FloatWindowService
import org.xm.stealth.camera.R
import com.allever.stealthcamera.function.permission.FloatWindowManager
import com.allever.stealthcamera.function.permission.rom.RomUtils
import com.allever.stealthcamera.function.permission.rom.VivoUtils
import com.allever.stealthcamera.utils.CameraUtil

class MainActivity : AbstractActivity() {

    private var mIvCam: ImageView? = null
    private var mIvSetting: ImageView? = null
    private var mIvPic: ImageView? = null
    private var mIvGenCam: ImageView? = null

    private var mPrevClickBackTime: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        initView()
    }

    private fun initData() {}

    private fun initView() {
        mIvCam = findViewById(R.id.id_main_iv_camera)
        mIvSetting = findViewById(R.id.id_main_iv_settings)
        mIvPic = findViewById(R.id.id_main_iv_pictures)
        mIvGenCam = findViewById(R.id.id_main_iv_general_camera)

        if (FloatWindowService.mService == null) {
            mIvCam!!.setImageResource(R.drawable.ic_camera_off)
        } else {
            mIvCam!!.setImageResource(R.drawable.ic_camera_on)
        }

        mIvCam?.setOnClickListener {
            if (FloatWindowManager.applyOrShowFloatWindow(this@MainActivity)) {
                val floatIntent = Intent(this@MainActivity, FloatWindowService::class.java)
                if (FloatWindowService.mService == null) {
                    startService(floatIntent)
                    mIvCam?.setImageResource(R.drawable.ic_camera_on)
                } else {
                    stopService(floatIntent)
                    mIvCam?.setImageResource(R.drawable.ic_camera_off)
                }
            } else {
                showSettingDialog()
            }
        }

        mIvSetting?.setOnClickListener {
            //设置界面
            val intent = Intent(this@MainActivity, SettingActivity::class.java)
            startActivity(intent)
        }

        mIvPic?.setOnClickListener {
            val intent = Intent(this@MainActivity, PictureActivity::class.java)
            startActivity(intent)
        }

        mIvGenCam?.setOnClickListener(View.OnClickListener {
            if (!CameraUtil.checkCameraHardware(this@MainActivity)) {
                return@OnClickListener
            }
            if (FloatWindowService.mService != null) {
                //停止预览
                val floatIntent = Intent(this@MainActivity, FloatWindowService::class.java)
                stopService(floatIntent)
                mIvCam?.setImageResource(R.drawable.ic_camera_off)
            }

            val intent = Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(intent)
        })


    }

    private fun showSettingDialog() {
        AlertDialog.Builder(this)
                .setTitle(R.string.string_dialog_title)
                .setMessage(R.string.string_dialog_message)
                .setPositiveButton(R.string.string_dialog_setting_button) { dialogInterface, i ->
                    if (RomUtils.checkIsVivoRom()) {
                        VivoUtils.applyOppoPermission(this@MainActivity)
                    } else {
                        FloatWindowManager.applyPermission(this@MainActivity)
                    }
                }
                .setNegativeButton(R.string.string_dialog_cancel_button) { dialogInterface, i -> }
                .show()
    }

    override fun onBackPressed() {
        val currentTime = System.currentTimeMillis()
        if (mPrevClickBackTime == -1L || currentTime - mPrevClickBackTime > 3000) {
            mPrevClickBackTime = currentTime
            toast("Press again to exit")
            return
        }
        super.onBackPressed()
    }
}
