package app.funnyappptech.android.fake.call.ui

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
import androidx.activity.OnBackPressedCallback
import app.funnyappptech.android.fake.call.R
import app.funnyappptech.android.fake.call.app.FakeCall
import app.funnyappptech.android.fake.call.base.BasePage
import app.funnyappptech.android.fake.call.databinding.VcActivityInComeCallBinding
import app.funnyappptech.android.fake.call.function.SettingStore
import app.funnyappptech.android.fake.call.service.VirtualCallService
import app.funnyappptech.android.fake.call.viewmodel.IncomeCallViewModel
import app.funnyappptech.android.fake.call.util.SystemUtils
import coil.load
import java.util.*

class IncomeCallPage : BasePage<VcActivityInComeCallBinding, IncomeCallViewModel>(),
    View.OnClickListener {



    private lateinit var mChronometer: Chronometer


    private lateinit var mShakeAnimator: ObjectAnimator
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
//        setShowWhenLocked(true)
        super.onCreate(savedInstanceState)
    }

    override fun inflate(): VcActivityInComeCallBinding {
        return VcActivityInComeCallBinding.inflate(layoutInflater)
    }

    override fun init() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
        mViewModel.initInComeCall()

        mShakeAnimator = createShakeAnimator(findViewById<View>(R.id.in_come_iv_accept), true)
        mShakeAnimator.start()

        mBinding.inComeIvReject.setOnClickListener(this)
        mBinding.inComeIvAccept.setOnClickListener(this)
        mBinding.inComeIvSendMessage.setOnClickListener(this)

        mChronometer = findViewById(R.id.in_come_tv_time)

        mBinding.inComeTvLocal.text = SettingStore.getLocal()

        var contact = SettingStore.getContact()
        var phone = SettingStore.getPhone()

        try {
            if (SettingStore.getRandomContact() && !SettingStore.getRepeat() && FakeCall.contactList.isNotEmpty()) {
                val random = Random()
                val randomContact = FakeCall.contactList[random.nextInt(FakeCall.contactList.size)]
                contact = randomContact.name
                phone = randomContact.phone
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mBinding.inComeTvPhoneNumber.text = phone
        mBinding.inComeTvName.text = contact


        findViewById<View>(R.id.in_come_bg).setBackgroundResource(
            FakeCall.wallPagerItemMap[SettingStore.getWallPagerTitle()]?.resId
                ?: R.drawable.vc_default_bg
        )

        if (!SettingStore.getRandomContact()) {
            loadAvatar(mBinding.ivAvatar)
            loadAvatar(mBinding.ivAvatarAccept)
        }

        mViewModel.play()
    }

    override fun onDestroy() {
        mViewModel.stop()
        mViewModel.destroy()
        super.onDestroy()
        mShakeAnimator.cancel()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.in_come_iv_reject -> {
                mChronometer.stop()
                if (SettingStore.getRepeat()) {
                    if (FakeCall.leftRepeatCount > 0) {
                        VirtualCallService.start(this, true)
                        FakeCall.leftRepeatCount--
                    }
                }

                finish()
            }
            R.id.in_come_iv_accept -> {
                mShakeAnimator.cancel()
                mViewModel.stop()

                mBinding.inComeIvSendMessage.visibility = View.GONE
                mBinding.inComeIvAccept.visibility = View.GONE
                mBinding.inComeIvReject.visibility = View.VISIBLE

                mBinding.inComeContainer.visibility = View.GONE
                mBinding.communicateContainer.visibility = View.VISIBLE

                mBinding.inComeTvCommunicateTo.text = "与 ${SettingStore.getContact()} 通话中"

                mChronometer.base = SystemClock.elapsedRealtime()
                mChronometer.start()

                FakeCall.leftRepeatCount = 0

            }
            R.id.in_come_iv_send_message -> {
                FakeCall.leftRepeatCount = 0
                finish()
            }
        }
    }

    override fun onBackPressed() {}

    private fun loadAvatar(imageView: ImageView) {
        val filePath = SettingStore.getAvatarPath()
        if (filePath.isNotEmpty()) {
            imageView.load(filePath)
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
            val intent = Intent(context, IncomeCallPage::class.java)
            SystemUtils.wakeUpAndUnlock(context)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}