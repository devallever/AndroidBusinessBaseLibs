package app.android.gp.ai.translator.ui

import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppMvpActivity
import app.android.gp.ai.translator.databinding.ADrawerMainBinding
import app.android.gp.ai.translator.ui.mvp.presenter.MainPresenter
import app.android.gp.ai.translator.ui.mvp.view.MainView
import app.woejt.wwzdndgl.lib.util.ActivityCollector
import com.allever.android.lib.admob.AdManager
import com.google.android.material.navigation.NavigationView

class HomePage : AppMvpActivity<MainView, MainPresenter>(), MainView,
    NavigationView.OnNavigationItemSelectedListener{

    private lateinit var mBinding: ADrawerMainBinding

    override fun getContentView(): Any {
        mBinding = ADrawerMainBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        addStatusBar(mBinding.rootLayout, findViewById(R.id.top_bar))

        val ivLeft = findViewById<ImageView>(R.id.iv_left)
        ivLeft.setImageResource(R.drawable.ic_menu)
        ivLeft.setOnClickListener {
            mBinding.drawerLayout.openDrawer(Gravity.LEFT)
        }
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.app_name)
        val ivRight = findViewById<ImageView>(R.id.iv_right)
        ivRight.clearColorFilter()
        ivRight.visibility = View.GONE
        ivRight.setOnClickListener {

        }
        mBinding.navigationView.setNavigationItemSelectedListener(this)

        AdManager.loadNativeAd(mBinding.bannerContainer, "Home")
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
                    ActivityCollector.startActivity(this, HistoryPage::class.java)
                }
                R.id.nav_word -> {
                    ActivityCollector.startActivity(this, WordPage::class.java)
                }
//                R.id.nav_backup -> {
//                    ActivityCollector.startActivity(this, BackupRestoreActivity::class.java)
//                }
                R.id.nav_guide -> {
                    ActivityCollector.startActivity(this, GuidePage::class.java)
                }
                R.id.nav_setting -> {
                    ActivityCollector.startActivity(this, SettingPage::class.java)
                }
                R.id.nav_about -> {
                    ActivityCollector.startActivity(
                        this,
                        app.android.gp.ai.translator.ui.AboutPage::class.java
                    )
                }
            }
            mBinding.drawerLayout.closeDrawers()
            item.isChecked = false
        }, 300)

        return true
    }

//    override fun onDestroy() {
//        if (SettingHelper.getForegroundServiceSwitch()) {
//            //启动一个前台服务
//            TranslationService.start(this)
//        } else {
//            TranslationService.stop(this)
//        }
//        super.onDestroy()
//    }

    override fun onBackPressed() {

        if (mBinding.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mBinding.drawerLayout.closeDrawers()
            return
        }

        checkExit()
    }
}