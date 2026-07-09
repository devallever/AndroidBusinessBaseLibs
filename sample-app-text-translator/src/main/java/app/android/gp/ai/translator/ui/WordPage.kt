package app.android.gp.ai.translator.ui

import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.android.gp.ai.translator.R
import app.android.gp.ai.translator.app.AppActivity
import app.android.gp.ai.translator.databinding.AWordBinding
import app.woejt.wwzdndgl.lib.app.AbsFragment
import app.woejt.wwzdndgl.lib.util.ActivityCollector
import com.allever.android.lib.admob.AdManager

class WordPage : AppActivity() {

    private lateinit var mFragment: AbsFragment

    private lateinit var mBinding: AWordBinding

    override fun getContentView(): Any {
        mBinding = AWordBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun initView() {
        addStatusBar(mBinding.rootLayout, findViewById(R.id.top_bar))
        val ivLeft = findViewById<ImageView>(R.id.iv_left)
        val ivRight = findViewById<ImageView>(R.id.iv_right)
        findViewById<TextView>(R.id.tv_label).text = getString(R.string.title_words)
        ivLeft.setOnClickListener {
            onKeyDown(0, null)
        }
        ivRight.setImageResource(R.drawable.ic_history)
        ivRight.setOnClickListener {
            ActivityCollector.startActivity(this, HistoryPage::class.java)
        }
        ivRight.visibility = View.VISIBLE

        AdManager.loadNativeAd(mBinding.bannerContainer, "word")
    }

    override fun initData() {
        val transaction = supportFragmentManager.beginTransaction()
        mFragment = WordFragmentPage.newInstance()
        transaction.replace(R.id.fragmentContainer, mFragment)
        transaction.commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val result = mFragment.onKeyDown(keyCode, event)
        if (!result) {
            finish()
        }

        return result
    }
}