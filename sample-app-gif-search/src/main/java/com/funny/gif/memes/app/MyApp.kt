package com.funny.gif.memes.app

//import com.allever.app.gif.search.ad.AdConstants
//import com.allever.app.gif.search.ad.AdFactory
import com.funny.gif.memes.func.maker.GifMakeHelper
import com.funny.gif.memes.func.media.FolderBean
import com.funny.gif.memes.func.media.MediaHelper
import com.funny.gif.memes.func.network.NetRepository
import com.funny.gif.memes.func.store.Store
import com.funny.gif.memes.util.ImageLoader
//import com.allever.lib.ad.chain.AdChainHelper
import com.allever.lib.common.app.App
import com.allever.lib.common.util.log
import com.allever.lib.common.util.loge
import com.xm.lib.BaseApp
import com.xm.lib.base.config.NetConfig
import com.xm.lib.datastroe.DataStore
import com.xm.netmodel.config.HttpConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litepal.LitePal


class MyApp : BaseApp() {
    override fun initThreadPackage() {

        App.context = this

        com.android.absbase.App.setContext(this)

        LitePal.initialize(this)

//        BillingHelper.init(BillingV5())


//        AdChainHelper.init(this, AdConstants.adData, AdFactory())

//        GiphyCoreUI.configure(this, "ENMowe3QQJBeL3fHproYw7C67ignSnuL")

//        RecommendGlobal.init(UMeng.getChannel())

        //初始化网络
        HttpConfig.init(applicationContext, "errorCode", "data")
            .setJsonMsgKeyName("errorMsg").setResponseOk(0)
            .setVerify(true)
            .builder("https://www.wanandroid.com/", 100)



        GlobalScope.launch {
            DataStore.init(App.context)
            val response = NetRepository.initGifFun(Store.getToken(), Store.getUserId().toString()) {
                loge(it)
            }
            response.data?.let {
                val token = it.token
                if (token.isNotEmpty()) {
                    Store.saveToken(it.token)
                    log("初始化成功： ${it.token}")
                }
            }

            val folderInfo = MediaHelper.getAllFolder(this@MyApp, MediaHelper.TYPE_VIDEO)
            folderInfo.add(FolderBean())
            folderInfo.map {
                log(it.dir)

                val mediaItemList = MediaHelper.getVideoMedia(this@MyApp, it.dir, 0)
                mediaItemList.map {
                    log("视频：${it.path}")
                }
            }

            val result = MediaHelper.getImageMedia(App.context, GifMakeHelper.gifDir)
            result.map {
                log("Gif: ${it.path}")
            }
        }

    }


    override fun initLoginStateConfig() = object : NetConfig() {
        override fun setLoginOutCallBack() {
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.onLowMemory()
    }

    override fun onTerminate() {
        super.onTerminate()
        ImageLoader.clearMemoryCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        ImageLoader.onTrimMemroy(level)
    }
}