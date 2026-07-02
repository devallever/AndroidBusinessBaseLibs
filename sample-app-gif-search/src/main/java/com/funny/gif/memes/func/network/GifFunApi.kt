package com.funny.gif.memes.func.network

import com.funny.gif.memes.func.network.response.giffun.*
import com.funny.gif.memes.giffun.GlobalUtil
import com.funny.gif.memes.giffun.NetworkConst
import com.funny.gif.memes.util.Utility
import retrofit2.http.*

interface GifFunApi {


    /**
    verride fun params(): Map<String, String>? {
    val params = HashMap<String, String>()
    params[NetworkConst.CLIENT_VERSION] = GlobalUtil.appVersionCode.toString()
    val appChannel =  GlobalUtil.getApplicationMetaData("APP_CHANNEL")
    if (appChannel != null) {
    params[NetworkConst.CLIENT_CHANNEL] = appChannel
    }
    if (buildAuthParams(params)) {
    params[NetworkConst.DEVICE_NAME] = deviceName
    }
    return params
    }

    override fun headers(builder: Headers.Builder): Headers.Builder {
    buildAuthHeaders(builder, NetworkConst.UID, NetworkConst.TOKEN)
    return super.headers(builder)
    }
     */
    @GET("/init")
    suspend fun init(
        @Query(NetworkConst.UID) userId: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): InitResponse

    @GET("/feeds/world")
    suspend fun fetchWorld(
        @Query(NetworkConst.UID) userId: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): FetchWorldResponse

    @GET("/feeds/world")
    suspend fun fetchWorldWithLastFeed(
        @Query(NetworkConst.UID) userId: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Query(NetworkConst.LAST_FEED) lastFeed: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): FetchWorldResponse

    @FormUrlEncoded
    @POST("/search/feeds")
    suspend fun search(
        @Field(NetworkConst.KEYWORD) keyword: String,
        @Field(NetworkConst.UID) userId: String,
        @Field(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Field(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): SearchGifFunResponse

    @FormUrlEncoded
    @POST("/search/feeds")
    suspend fun searchWithLastFeed(
        @Field(NetworkConst.KEYWORD) keyword: String,
        @Field(NetworkConst.UID) userId: String,
        @Field(NetworkConst.TOKEN) token: String,
        @Field(NetworkConst.LAST_FEED) lastFeed: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Field(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): SearchGifFunResponse

    /**
     *
     */
    @FormUrlEncoded
    @POST("/authorize_url")
    suspend fun getAuthorizeUrl(
        @Field(NetworkConst.UID) userId: String,
        @Field(NetworkConst.TOKEN) token: String,
        @Field(NetworkConst.FEED) lastFeed: String,
        @Field(NetworkConst.URL) url: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Field(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): AuthorizeUrlResponse

    /**
     *
     * builder.add(NetworkConst.HEADER_USER_AGENT, NetworkConst.HEADER_USER_AGENT_VALUE)
    builder.add(NetworkConst.HEADER_APP_VERSION, Utility.appVersion)
    builder.add(NetworkConst.HEADER_APP_SIGN, Utility.appSign)

     * params[NetworkConst.NUMBER] = number
    params[NetworkConst.DEVICE_NAME] = deviceName
    params[NetworkConst.DEVICE_SERIAL] = deviceSerial
     */
    @FormUrlEncoded
    @POST("/login/fetch_verify_code")
    suspend fun fetchVCode(
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
        @Field(NetworkConst.NUMBER) number: String,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.DEVICE_NAME) deviceName : String= Utility.deviceName
    ): VCodeResponse


    /**
    params[NetworkConst.NUMBER] = number
    params[NetworkConst.CODE] = code
    params[NetworkConst.DEVICE_NAME] = deviceName
    params[NetworkConst.DEVICE_SERIAL] = deviceSerial
     */
    @FormUrlEncoded
    @POST("/login/phone")
    suspend fun login(
        @Field(NetworkConst.NUMBER) number: String,
        @Field(NetworkConst.CODE) code: String,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.DEVICE_NAME) deviceName : String= Utility.deviceName
    ): LoginResponse

    /**
    params[NetworkConst.NUMBER] = number
    params[NetworkConst.CODE] = code
    params[NetworkConst.NICKNAME] = nickname
    params[NetworkConst.DEVICE_NAME] = deviceName
    params[NetworkConst.DEVICE_SERIAL] = deviceSerial
     */
    @FormUrlEncoded
    @POST("/register/phone")
    suspend fun register(
        @Field(NetworkConst.NUMBER) number: String,
        @Field(NetworkConst.CODE) code: String,
        @Field(NetworkConst.NICKNAME) nickname: String,
        @Field(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
        @Field(NetworkConst.DEVICE_NAME) deviceName : String= Utility.deviceName
    ): RegisterResponse

    ///user/baseinfo
    @GET("/feeds/user")
    suspend fun fetchUserFeeds(
        @Query(NetworkConst.USER_ID) userId: String,
        @Query(NetworkConst.UID) uid: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
//        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
//        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): FetchUserFeedsResponse

    ///user/baseinfo
    @GET("/user/baseinfo")
    suspend fun getUserInfo2(
        @Query(NetworkConst.USER_ID) userId: String,
        @Query(NetworkConst.UID) uid: String,
        @Query(NetworkConst.TOKEN) token: String,
        @Header(NetworkConst.VERIFY) params: String,
        @Header(NetworkConst.HEADER_USER_AGENT) agent: String = NetworkConst.HEADER_USER_AGENT_VALUE,
        @Header(NetworkConst.HEADER_APP_VERSION ) appVersion: String = Utility.appVersion,
        @Header(NetworkConst.HEADER_APP_SIGN) sign: String = Utility.appSign,
//        @Query(NetworkConst.DEVICE_NAME) deviceName: String = Utility.deviceName,
        @Query(NetworkConst.DEVICE_SERIAL) serial: String = Utility.getDeviceSerial(),
//        @Query(NetworkConst.CLIENT_VERSION) clientVersion: String = GlobalUtil.appVersionCode.toString()
    ): UserInfoResponse

}