package com.alsg.bakericon

import app.allever.lib.billing.BillingHelper
import app.allever.lib.billing.BillingV6
import com.light.icon.adcore.AdCore
import com.allever.lib.base.app.App
import com.allever.lib.base.function.network.ApiService
import com.allever.lib.base.function.network.internal.HttpConfig
import com.allever.lib.base.helper.CoroutineHelper
import com.alsg.bakericon.db.DBRepo
import com.alsg.bakericon.local.LocalRepo
import com.alsg.bakericon.network.response.BaseResponse
import com.alsg.bakericon.util.MMKVHelper
import com.privacy.mob.AdMobEngine
import kotlinx.coroutines.launch
import org.litepal.LitePal

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class MyApp : App() {
    override fun init() {
//        ImageLoader.init(this, GlideLoader, ImageLoader.Builder.create())
        MMKVHelper.init(this)
        HttpConfig.baseUrl(Constant.BASE_URL)
            .baseResponseClass(BaseResponse::class.java)
            .init(ApiService)
        LocalRepo.loadAll()
        LitePal.initialize(this)
        BillingHelper.init(this, BillingV6())
        CoroutineHelper.DEFAULT.launch {
            DBRepo.clearOutDateDownload()
        }
        AdCore.init(this, AdMobEngine()) {

        }
    }
}