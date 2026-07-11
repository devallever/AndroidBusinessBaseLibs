package app.allever.android.sample.im.business

import android.annotation.SuppressLint
import app.allever.android.lib.common.BaseFragment
import app.allever.android.lib.common.FragmentActivity
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.adapter.TextDetailClickAdapter
import app.allever.android.lib.common.adapter.bean.TextDetailClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.CoroutineHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.im.IMConfig
import app.allever.android.sample.im.http.response.BaseResponse
import app.allever.android.sample.im.http.response.UserInfoData
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OnlineUserListFragment: ListFragment<FragmentListBinding, BaseViewModel, TextDetailClickItem>() {
    override fun getAdapter(): BaseQuickAdapter<TextDetailClickItem, *> = TextDetailClickAdapter()
    @SuppressLint("NotifyDataSetChanged")
    override fun getList(): MutableList<TextDetailClickItem> = mutableListOf()

    override fun init() {
        super.init()
        CoroutineHelper.IO.launch {
            val response = NetCore.get<BaseResponse<List<UserInfoData>>>("/api/user/onlineList")
            if (response.isSuccess() && response.data != null) {
                val list = mutableListOf<TextDetailClickItem>()
                response.data.forEach {user ->
                    list.add(TextDetailClickItem(user.username) {
                        if (user.username == IMConfig.getLoginUser()) {
                            toast("不能私聊自己")
                        } else {
                            toast("私聊：${user.username}")
                            FragmentActivity.start<PrivateChatFragment>(user.username) {
                                it.putString("username", user.username)
                            }

                        }
                    })
                }
                launch(Dispatchers.Main) {
                    mAdapter?.data?.clear()
                    mAdapter?.setList(list)
                }
            }
        }
    }
}