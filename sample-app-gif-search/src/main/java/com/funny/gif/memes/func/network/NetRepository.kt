package com.funny.gif.memes.func.network

import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.logE
import com.funny.gif.memes.bean.SearchResponse
import com.funny.gif.memes.bean.TrendingResponse
import com.funny.gif.memes.func.network.response.BannerData
import com.funny.gif.memes.func.network.response.PageData
import com.funny.gif.memes.func.network.response.giffun.*
import com.funny.gif.memes.giffun.AuthUtil
import com.funny.gif.memes.util.Utility

object NetRepository {

    private val apiService by lazy {
        ServiceCreator.create(Api::class.java)
    }

    private val gifFunApiService by lazy {
        GifFunServiceCreator.create(GifFunApi::class.java)
    }

    suspend fun initGifFun(token: String, userId: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<InitResponse> {
        val param = listOf(userId, token)
        return getGifFunData(failureTask, "初始化成功") {
            gifFunApiService.init(param[0], param[1], AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        }
    }

    suspend fun getUserInfo(token: String, userId: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<UserInfoResponse> {
        val param = listOf(Utility.getDeviceSerial(), token)
        return getGifFunData(failureTask, "获取用户信息成功") {
            gifFunApiService.getUserInfo2(userId, userId, token, AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        }
    }

    suspend fun fetchUserFeed(token: String, userId: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<FetchUserFeedsResponse> {
        val param = listOf(token, userId)
        return getGifFunData(failureTask, "获取用户信息成功") {
            gifFunApiService.fetchUserFeeds(userId, userId, token, AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        }
    }

    suspend fun fetchWorld(token: String, userId: String, lastFeed: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<FetchWorldResponse> {
        val param = listOf(userId, token)
        return getGifFunData(failureTask, "获取Gif列表成功") {
            if (lastFeed.isEmpty() || lastFeed == "0") {
                gifFunApiService.fetchWorld(param[0], param[1], AuthUtil.getServerVerifyCode(*param.toTypedArray()))
            } else {
                gifFunApiService.fetchWorldWithLastFeed(param[0], param[1], AuthUtil.getServerVerifyCode(*param.toTypedArray()), lastFeed)
            }
        }
    }

    suspend fun search(keyword: String, token: String, userId: String, lastFeed: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<SearchGifFunResponse> {
        val param = listOf(token, keyword)
        return getGifFunData(failureTask, "搜索Gif成功") {
            if (lastFeed.isEmpty() || lastFeed == "0") {
                gifFunApiService.search(keyword, userId, token, AuthUtil.getServerVerifyCode(*param.toTypedArray()))
            } else {
                gifFunApiService.searchWithLastFeed(keyword, userId, token, lastFeed, AuthUtil.getServerVerifyCode(*param.toTypedArray()))
            }
        }
    }

    suspend fun getAuthorizedUrl(token: String, userId: String, feedId: String, url: String,  failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<AuthorizeUrlResponse> {
        val param = listOf(feedId, userId, token)
        return getGifFunData(failureTask, "获取GifUrl成功") {
            gifFunApiService.getAuthorizeUrl(userId, token, feedId, url, AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        }
    }

    suspend fun fetchVCode(phone: String, failureTask: (errorMsg: String) -> Unit = {}): BaseResponse<VCodeResponse> {
        val param = listOf(Utility.deviceName, phone, Utility.getDeviceSerial())
        return getGifFunData(failureTask, "获取验证码成功") {
            gifFunApiService.fetchVCode(number = param[1], params = AuthUtil.getServerVerifyCode(*param.toTypedArray()))
        }
    }

    suspend fun login(phone: String, code: String, failureTask: (errorMsg: String) -> Unit = {}) : BaseResponse<LoginResponse> {
        return getGifFunData(failureTask, "登录成功") {
            gifFunApiService.login(phone, code)
        }
    }

    suspend fun register(phone: String, code: String, nickname: String, failureTask: (errorMsg: String) -> Unit = {}) : BaseResponse<RegisterResponse> {
        return getGifFunData(failureTask, "注册成功") {
            gifFunApiService.register(phone, code, nickname)
        }
    }

    suspend fun getTrendList(
        pageNum: Int,
        count: Int,
        failureTask: (errorMsg: String) -> Unit = {}
    ): BaseResponse<TrendingResponse> {
        return getGiphyData(failureTask, "获取Gif列表成功") {
            apiService.trendingGif(offset = pageNum.toString(), limit = count.toString())
        }
    }

    suspend fun search(
        keyword: String,
        pageNum: Int,
        count: Int,
        failureTask: (errorMsg: String) -> Unit = {}
    ): BaseResponse<SearchResponse> {
        return getGiphyData(failureTask, "搜索Gif成功") {
            apiService.searchGif(q = keyword, offset = pageNum.toString(), limit = count.toString())
        }
    }


    suspend fun <T> getGifFunData(
        failureTask: (errorMsg: String) -> Unit,
        successMsg: String = "获取数据成功",
        block: suspend () ->  GifFunResponse
    ): BaseResponse<T> {
        val response = BaseResponse<T>()
        try {
            val data = block()
            response.errorCode = data.status
            response.errorMsg = data.msg
            if (data.status == 0) {
                response.data = data as T
                log(successMsg)
            } else {
                failureTask("${data.javaClass.simpleName}: ${data.status} -> ${data.msg}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            response.errorCode = -1
            response.errorMsg = e.message ?: ""
            failureTask(response.errorMsg)
        }
        return response
    }

    suspend fun <T> getGiphyData(
        failureTask: (errorMsg: String) -> Unit,
        successMsg: String = "获取数据成功",
        block: suspend () -> T
    ): BaseResponse<T> {
        val response = BaseResponse<T>()
        try {
            val data = block()
            response.data = data
            response.errorMsg = ""
            response.errorCode = 0
            log(successMsg)
        } catch (e: Exception) {
            e.printStackTrace()
            response.errorMsg = e.message ?: ""
            failureTask(response.errorMsg)
        }
        return response
    }

    suspend fun getHomePageList(pageNum: Int, failureTask: (errorMsg: String) -> Unit): PageData? {
        return getData(failureTask, "获取首页文章列表成功") {
            apiService.getHomePageList(pageNum)
        }
    }

    suspend fun getProjectPageList(
        cid: Int,
        pageNum: Int,
        failureTask: (errorMsg: String) -> Unit
    ): PageData? {
        return getData(failureTask, "获取项目文章列表成功") {
            apiService.getProjectPageList(cid, pageNum)
        }
    }

    suspend fun getBanner(failureTask: (errorMsg: String) -> Unit): List<BannerData>? {
        return getData(failureTask, "获取首页banner成功") {
            apiService.getBanner()
        }
    }

    suspend fun getProjectSortData(failureTask: (errorMsg: String) -> Unit): List<ProjectSortData>? {
        return getData(failureTask, "获取项目分类成功") {
            apiService.getProjectSort()
        }
    }

    private suspend fun <T> getData(
        failureTask: (errorMsg: String) -> Unit,
        successMsg: String = "获取数据成功",
        block: suspend () -> BaseResponse<T>?
    ): T? {
        return try {
            val baseResponse = block()
            val result = getResponseData(baseResponse) {
                failureTask(it)
            }
            if (result != null) {
                log(successMsg)
            }
            return result
        } catch (e: Exception) {
            //失败
            e.printStackTrace()
            failureTask("${e.message}")
            null
        }
    }

    suspend fun getSortData3(failureTask: (errorMsg: String) -> Unit): List<ProjectSortData>? {
        return try {
            val baseResponse = apiService.getSortData3()
            val result = getResponseData(baseResponse) {
                failureTask(it)
            }
            if (result != null) {
                log("获取分类成功")
            }
            return result
        } catch (e: Exception) {
            //失败
            e.printStackTrace()
            failureTask("${e.message}")
            null
        }
    }

    private fun <T> getResponseData(baseResponse: BaseResponse<T>?): T? {
        baseResponse?.data ?: return null
        return if (baseResponse.errorCode != 0) {
            //成功-数据异常
            logE(baseResponse.errorMsg)
            null
        } else {
            //成功
            baseResponse.data
        }
    }

    private fun <T> getResponseData(
        baseResponse: BaseResponse<T>?,
        failureTask: (errorMsg: String) -> Unit
    ): T? {
        baseResponse?.data ?: return null
        return if (baseResponse.errorCode != 0) {
            //成功-数据异常
//            loge(baseResponse.errorMsg)
            failureTask(baseResponse.errorMsg)
            null
        } else {
            //成功
            baseResponse.data
        }
    }
}