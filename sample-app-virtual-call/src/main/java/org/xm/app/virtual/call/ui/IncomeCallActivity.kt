package org.xm.app.virtual.call.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.TextView
import com.allever.app.virtual.call.R
import app.allever.android.lib.core.app.App
import com.bumptech.glide.Glide
import org.xm.app.virtual.call.app.BaseActivity
import org.xm.app.virtual.call.app.Global
import org.xm.app.virtual.call.function.SettingHelper
import org.xm.app.virtual.call.service.VirtualCallService
import org.xm.app.virtual.call.ui.mvp.presenter.IncomeCallPresenter
import org.xm.app.virtual.call.ui.mvp.view.IncomeCallView
import org.xm.app.virtual.call.util.SystemUtils
import java.util.*

class IncomeCallActivity : BaseActivity<IncomeCallView, IncomeCallPresenter>(),
    IncomeCallView,
    View.OnClickListener {

    private lateinit var mInComeCallContainer: View
    private lateinit var mCommunicateContainer: View

    private lateinit var mBtnReject: View
    private lateinit var mBtnAccept: View
    private lateinit var mBtnMessage: View

    private lateinit var mChronometer: Chronometer

    private lateinit var mTvCommunicate: TextView
    private lateinit var mIvAvatar: ImageView
    private lateinit var mIvAvatarAccept: ImageView

    private lateinit var mShakeAnimator: ObjectAnimator

    override fun getContentView(): Any = R.layout.vc_activity_in_come_call

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
//        setShowWhenLocked(true)
        super.onCreate(savedInstanceState)
    }

    override fun initView() {
        mPresenter.initInComeCall()

        mShakeAnimator = createShakeAnimator(findViewById<View>(R.id.in_come_iv_accept), true)
        mShakeAnimator.start()

        mInComeCallContainer = findViewById(R.id.in_come_container)
        mCommunicateContainer = findViewById(R.id.communicate_container)

        mBtnReject = findViewById<View>(R.id.in_come_iv_reject)
        mBtnReject.setOnClickListener(this)
        mBtnAccept = findViewById<View>(R.id.in_come_iv_accept)
        mBtnAccept.setOnClickListener(this)
        mBtnMessage = findViewById<View>(R.id.in_come_iv_send_message)
        mBtnMessage.setOnClickListener(this)

        mChronometer = findViewById(R.id.in_come_tv_time)

        mTvCommunicate = findViewById(R.id.in_come_tv_communicate_to)

        findViewById<TextView>(R.id.in_come_tv_local).text = SettingHelper.getLocal()

        var contact = SettingHelper.getContact()
        var phone = SettingHelper.getPhone()

        try {
            if (SettingHelper.getRandomContact() && !SettingHelper.getRepeat() && Global.contactList.isNotEmpty()) {
                val random = Random()
                val randomContact = Global.contactList[random.nextInt(Global.contactList.size)]
                contact = randomContact.name
                phone = randomContact.phone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        findViewById<TextView>(R.id.in_come_tv_phone_number).text = phone
        findViewById<TextView>(R.id.in_come_tv_name).text = contact


        findViewById<View>(R.id.in_come_bg).setBackgroundResource(
            Global.wallPagerItemMap[SettingHelper.getWallPagerTitle()]?.resId
                ?: R.drawable.vc_default_bg
        )

        if (!SettingHelper.getRandomContact()) {
            mIvAvatar = findViewById(R.id.ivAvatar)
            mIvAvatarAccept = findViewById(R.id.ivAvatarAccept)
            loadAvatar(mIvAvatar)
            loadAvatar(mIvAvatarAccept)
        }

        mPresenter.play()
    }

    override fun initData() {}

    override fun createPresenter(): IncomeCallPresenter =
        IncomeCallPresenter()

    override fun onDestroy() {
        mPresenter.stop()
        mPresenter.destroy()
        super.onDestroy()
        mShakeAnimator.cancel()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.in_come_iv_reject -> {
                mChronometer.stop()
                if (SettingHelper.getRepeat()) {
                    if (Global.leftRepeatCount > 0) {
                        VirtualCallService.start(this, true)
                        Global.leftRepeatCount--
                    }
                }

                finish()
            }
            R.id.in_come_iv_accept -> {
                mShakeAnimator.cancel()
                mPresenter.stop()

                mBtnMessage.visibility = View.GONE
                mBtnAccept.visibility = View.GONE
                mBtnReject.visibility = View.VISIBLE

                mInComeCallContainer.visibility = View.GONE
                mCommunicateContainer.visibility = View.VISIBLE

                mTvCommunicate.text = "与 ${SettingHelper.getContact()} 通话中"

                mChronometer.base = SystemClock.elapsedRealtime()
                mChronometer.start()

                Global.leftRepeatCount = 0

            }
            R.id.in_come_iv_send_message -> {
                Global.leftRepeatCount = 0
                finish()
            }
        }
    }

    override fun onBackPressed() {}

    private fun loadAvatar(imageView: ImageView) {
        val filePath = SettingHelper.getAvatarPath()
        if (filePath.isNotEmpty()) {
            Glide.with(App.context).load(filePath).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.vc_ic_contact)
        }
    }

    private fun createShakeAnimator(target: View, loop: Boolean = false): ObjectAnimator {

        val objectAnimator = ObjectAnimator.ofFloat(
            target,
            "rotation",
            0f,
            5f,
            0f,
            -10f,
            0f,
            15f,
            0f,
            -10f,
            0f,
            15f,
            0f,
            -10f,
            0f,
            13f,
            0f,
            -5f,
            0f,
            3f,
            0f,
            0f,
            0f,
            0f,
            0f,
            0f
        )
        objectAnimator.duration = 4000
        objectAnimator.interpolator = AnticipateOvershootInterpolator()
        objectAnimator.startDelay = 1000
        var repeatCount = 0
        if (loop) {
            repeatCount = ValueAnimator.INFINITE
        }
        objectAnimator.repeatCount = repeatCount

        return objectAnimator
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, IncomeCallActivity::class.java)
            SystemUtils.wakeUpAndUnlock(context)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}