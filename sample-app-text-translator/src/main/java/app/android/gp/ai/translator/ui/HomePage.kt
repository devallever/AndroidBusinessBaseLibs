package app.android.gp.ai.translator.ui

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import app.allever.android.lib.core.helper.ActivityHelper
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpActivity
import app.android.gp.ai.translator.databinding.ADrawerMainBinding
import app.android.gp.ai.translator.ui.mvp.presenter.MainPresenter
import app.android.gp.ai.translator.ui.mvp.view.MainView
import com.google.android.material.navigation.NavigationView

class HomePage : AppMvpActivity<MainView, MainPresenter>(), MainView,
    NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mBinding: ADrawerMainBinding

    override fun isSupportSwipeBack(): Boolean {
        return false
    }

    override fun getContentView(): Any {
        mBinding = ADrawerMainBinding.inflate(layoutInflater)
        return mBinding.root
    }

    @SuppressLint("WrongConstant")
    override fun initView() {
        addStatusBar(mBinding.rootLayout, findViewById(R.id.top_bar))

        val ivLeft = findViewById<ImageView>(R.id.iv_left)
        ivLeft.setImageResource(R.drawable.tt_ic_menu)
        ivLeft.setOnClickListener {
            mBinding.drawerLayout.openDrawer(Gravity.START)
        }
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.tt_app_name)
        val ivRight = findViewById<ImageView>(R.id.iv_right)
        ivRight.clearColorFilter()
        ivRight.visibility = View.GONE
        ivRight.setOnClickListener {

        }
        mBinding.navigationView.setNavigationItemSelectedListener(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mBinding.drawerLayout.isDrawerOpen(Gravity.START)) {
                    mBinding.drawerLayout.closeDrawers()
                    return
                }

                checkExit()
            }

        })

    }

    override fun initData() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, TranslationFragmentPage.newInstance())
        transaction.commit()
    }

    override fun createPresenter(): MainPresenter = MainPresenter()

    override fun updateResult(result: String) {
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        mBinding.drawerLayout.closeDrawer(GravityCompat.START)
        item.isCheckable = true
        item.isChecked = true
        mBinding.drawerLayout.postDelayed({
            when (item.itemId) {
                R.id.nav_history -> {
                    ActivityHelper.startActivity<HistoryPage>()
                }
                R.id.nav_word -> {
                    ActivityHelper.startActivity<WordPage>()
                }
                R.id.nav_backup -> {
                    ActivityHelper.startActivity<BackupRestorePage>()
                }
                R.id.nav_guide -> {
                    ActivityHelper.startActivity<GuidePage>()
                }
                R.id.nav_setting -> {
                    ActivityHelper.startActivity<SettingPage>()
                }
                R.id.nav_about -> {
                    ActivityHelper.startActivity<AboutPage>()
                }
            }
            mBinding.drawerLayout.closeDrawers()
            item.isChecked = false
        }, 300)

        return true
    }

}