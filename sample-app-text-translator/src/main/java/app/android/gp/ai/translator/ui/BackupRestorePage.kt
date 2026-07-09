package app.android.gp.ai.translator.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpActivity
import app.android.gp.ai.translator.ui.mvp.presenter.BackupRestorePresenter
import app.android.gp.ai.translator.ui.mvp.view.BackupRestoreView

class BackupRestorePage : AppMvpActivity<BackupRestoreView, BackupRestorePresenter>(),
    BackupRestoreView,
    View.OnClickListener {

    private lateinit var mBtnBackup: Button
    private lateinit var mBtnRestore: Button
    private lateinit var mBtnDelBackup: Button

    override fun getContentView(): Any = R.layout.a_backup_restore

    override fun initView() {
        addStatusBar(findViewById(R.id.rootLayout), findViewById(R.id.top_bar))
        findViewById<View>(R.id.iv_left).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.tt_backup_restore)
        mBtnBackup = findViewById(R.id.btnBackup)
        mBtnBackup.setOnClickListener(this)
        mBtnRestore = findViewById(R.id.btnRestore)
        mBtnRestore.setOnClickListener(this)
        mBtnDelBackup = findViewById(R.id.btnDeleteBackup)
        mBtnDelBackup.setOnClickListener(this)
    }

    override fun initData() {
    }

    override fun createPresenter(): BackupRestorePresenter = BackupRestorePresenter()

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_left -> {
                finish()
            }
            R.id.btnBackup -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.tt_warm_tips)
                    .setMessage(R.string.tt_backup_tips)
                    .setPositiveButton(
                        R.string.tt_backup
                    ) { dialog, which ->
                        mBtnBackup.isClickable = false
                        mPresenter?.backup(this, Runnable {
                            mBtnBackup.isClickable = true
                        })
                        dialog.dismiss()
                    }
                    .setNegativeButton(
                        R.string.tt_cancle
                    ) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
            R.id.btnRestore -> {
                mBtnRestore.isClickable = false
                mPresenter?.restore(this, Runnable {
                    mBtnRestore.isClickable = true
                })
            }

            R.id.btnDeleteBackup -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.tt_warm_tips)
                    .setMessage(R.string.tt_del_backup_tips)
                    .setPositiveButton(
                        R.string.tt_del_backup
                    ) { dialog, which ->
                        mBtnDelBackup.isClickable = false
                        mPresenter?.delBackup(this, Runnable {
                            mBtnDelBackup.isClickable = true
                        })
                        dialog.dismiss()
                    }
                    .setNegativeButton(
                        R.string.tt_cancle
                    ) { dialog, which ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        setResult(Activity.RESULT_OK)
    }

    companion object {
        val RC_RESULT = 0X01
        fun start(activity: Activity) {
            val intent = Intent(activity, BackupRestorePage::class.java)
            activity.startActivityForResult(intent, RC_RESULT)
        }
    }

}