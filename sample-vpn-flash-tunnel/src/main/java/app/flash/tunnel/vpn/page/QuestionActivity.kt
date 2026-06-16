package app.flash.tunnel.vpn.page

import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.flash.tunnel.vpn.data.QaItem
import app.flash.tunnel.vpn.databinding.ActivityQuestionBinding
import app.flash.tunnel.vpn.page.adapter.QuestionAdapter
import app.flash.tunnel.vpn.page.viewmodel.FAQViewModel

class QuestionActivity : BaseActivity<ActivityQuestionBinding>() {
    private val mViewModel by viewModels<FAQViewModel>()

    private val adapter = QuestionAdapter(mutableListOf())

    private fun fetchList() {
        val qaList = mutableListOf<QaItem>()
        List(mViewModel.questions.size) { index ->
            qaList.add(QaItem(mViewModel.questions[index], mViewModel.answers[index], false))
        }
        adapter.data.clear()
        adapter.data.addAll(qaList)
        adapter.notifyDataSetChanged()
    }

    override fun inflate() = ActivityQuestionBinding.inflate(layoutInflater)

    override fun init() {
        fixStatusBar(mBinding.topBar)
        mBinding.ivClose.setOnClickListener {
            finish()
        }

        mBinding.rvFaq.layoutManager = LinearLayoutManager(this@QuestionActivity)
        mBinding.rvFaq.adapter = adapter

        fetchList()
    }
}