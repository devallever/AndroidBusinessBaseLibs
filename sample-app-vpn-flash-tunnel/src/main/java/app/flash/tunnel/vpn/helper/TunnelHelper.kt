package app.flash.tunnel.vpn.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.RemoteException
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import app.flash.tunnel.vpn.Constants
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.data.FTResponse
import app.flash.tunnel.vpn.data.NodeItem
import app.flash.tunnel.vpn.helper.ad.AdHelper
import app.flash.tunnel.vpn.lib.common.ext.loadCircle
import app.flash.tunnel.vpn.lib.common.ext.toLiveData
import app.flash.tunnel.vpn.lib.common.util.AssetsManager
import app.flash.tunnel.vpn.lib.common.util.GsonManager
import app.flash.tunnel.vpn.lib.common.util.StoreManager
import app.flash.tunnel.vpn.lib.common.util.log
import app.flash.tunnel.vpn.lib.common.util.runInIoDispatcher
import app.flash.tunnel.vpn.lib.common.util.runInMainDispatcher
import app.flash.tunnel.vpn.lib.common.util.toJson
import app.flash.tunnel.vpn.page.LoadingActivity
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object TunnelHelper : ShadowsocksConnection.Callback {
    private const val NODE_NOT_FETCH = EventHelper.ServerSource.NON_FETCH_NODE
    private const val NODE_FIREBASE = EventHelper.ServerSource.FIREBASE
    private const val NODE_API = EventHelper.ServerSource.API
    private const val NODE_LOCAL = EventHelper.ServerSource.LOCAL
    var sourceType = NODE_NOT_FETCH
        private set

    //for smartMode
    private var mBestNode: NodeItem? = null

    //for none smart mode
    private var currentItem: NodeItem? = null
        get() {
            nodeListLiveData.value?.let { list ->
                for (i in 0 until list.size) {
                    val it = list[i]
                    if (it.entity?.id == DataStore.profileId) {
//                    log("return currentItem same id")
                        return it
                    }
                }
            }
//            log("return currentItem null: DataStore.selectedId = ${DataStore.selectedProxy}")
            return null
        }

    fun getConnectedNodeItem(): NodeItem? {
        if (isServiceConnected()) {
            if (isSmartMode()) {
                return getFastestNode()
            }
            return currentItem
        }
        return null
    }

    fun getSelectedNodeItem(): NodeItem? {
        if (!isSmartMode()) {
            return currentItem
        }

        return getFastestNode()
    }

    private val mDbProxyIpSet by lazy {
        mutableSetOf<String>()
    }

    private val mDbProxyIpIdMap by lazy {
        mutableMapOf<String, Long>()
    }

    private val mConnection by lazy {
        ShadowsocksConnection()
    }

    private val _nodeListLiveData by lazy {
        MutableLiveData<MutableList<NodeItem>>(mutableListOf())
    }
    val nodeListLiveData = _nodeListLiveData.toLiveData()
    val serviceStateLiveData by lazy {
        DataStore.serverStateLiveData
    }
    private val _remainTimeLiveData by lazy {
        MutableLiveData(0L)
    }
    val remainTimeLiveData = _remainTimeLiveData.toLiveData()
    val appendTimeLiveData by lazy {
        MutableLiveData(false)
    }
    val alreadyAppendLiveData by lazy {
        MutableLiveData(false)
    }
    private val _currentNodeLiveData by lazy {
        MutableLiveData(currentItem)
    }
    val currentNodeLiveData = _currentNodeLiveData.toLiveData()
    private val _speedLiveData by lazy {
        MutableLiveData(TrafficStats())
    }
    val speedLiveData = _speedLiveData.toLiveData()

    private val _connectFailFlow = MutableStateFlow(false)
    val connectFailFlow = _connectFailFlow.asStateFlow()

    private var mFirebaseResponse: FTResponse? = null
    private var mApiResponse: FTResponse? = null

    private val mFirebaseNodeList by lazy {
        mutableListOf<NodeItem>()
    }
    private val mApiNodeList by lazy {
        mutableListOf<NodeItem>()
    }
    private val mLocalNodeList by lazy {
        mutableListOf<NodeItem>()
    }

    var connectAutoStop = false

//    fun isNodeReady(): Boolean {
//        val allEmpty =
//            mFirebaseNodeList.isEmpty() && mLocalNodeList.isEmpty()
//        return !allEmpty
//    }

    fun dataReady() = nodeListLiveData.value?.isNotEmpty() == true

    fun init(context: Context) {
        runInIoDispatcher {
            getAllProxyIpFromDb()
            checkUsedLocalCache()
            fetchNodeList()
        }
        DataStore.registerSelectedProxyChange {
            _currentNodeLiveData.postValue(currentItem)
        }

        remainTimeLiveData.observeForever {
//            log("onReceiveTimer: forever: ${TimeUtil.formatTimeStampToHMS(it)}")
            if (it < 0L) {
                EventHelper.logConnectEnd()
            }
            if (TunnelApp.alreadyInBackground) {
                if (it < 0L) {
                    connectAutoStop = true
                    log("update connectAutoStop = true")
                }
            }
        }

        mConnection.connect(TunnelApp.context, this)
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) =
        changeState(state, msg)

    override fun trafficUpdated(profileId: Long, stats: TrafficStats) {
        _speedLiveData.postValue(stats)
    }

    override fun onTimerUpdate(time: Long) = _remainTimeLiveData.postValue(time)

    override fun onServiceConnected(service: IShadowsocksService) = changeState(
        try {
            BaseService.State.values()[service.state]
        } catch (_: RemoteException) {
            BaseService.State.Idle
        }
    )

    override fun onServiceDisconnected() = changeState(BaseService.State.Idle)
    override fun onBinderDied() {
        mConnection.disconnect(TunnelApp.context)
        mConnection.connect(TunnelApp.context, this)
    }


    private fun changeState(
        state: BaseService.State,
        msg: String? = null,
    ) {
        DataStore.serviceState = state

        if (state == BaseService.State.Connected) {
            Core.cancelAutoNotification()
            val usedTime = System.currentTimeMillis() - EventHelper.ssTimeStart
            EventHelper.logConnectSuccess(usedTime)
        }

        log("changeState: ${state.name}, msg: ${msg ?: "null"}")

        if (state == BaseService.State.Stopped && msg != null) {
            runInMainDispatcher { _connectFailFlow.emit(true) }
        }

    }

    fun isServiceConnected() = BaseService.State.Connected == DataStore.serviceState

    fun isServiceConnecting() = BaseService.State.Connecting == DataStore.serviceState

    fun isServiceStopped() = BaseService.State.Stopped == DataStore.serviceState

    fun launchLoading(
        context: Context,
        type: Int
    ) {
        LoadingActivity.launch(context, type)
    }

    fun adaptSdk33Notification(activity: Activity) {
        // sdk 33 notification
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }
    }

    fun loadRegionsFlag(imageView: ImageView, countryCode: String) {
        if (countryCode.isNotEmpty()) {
            imageView.loadCircle("${Constants.ASSETS_ROOT}flags/${countryCode}.png")
            return
        }
        imageView.loadCircle(R.drawable.ic_default_flag)
    }

    fun appendConnectTime() {
        appendTimeLiveData.postValue(true)
        mConnection.appendConnectTime()
    }

    fun resetConnectTime() = mConnection.resetConnectTime()

    fun getConnectDuration() = mConnection.getConnectDuration()

    suspend fun fetchNodeList(success: () -> Unit = {}, fail: () -> Unit = {}) {
        //firebase
        runInIoDispatcher {
            //firebase
//            val firebaseStartTime = System.currentTimeMillis()
//            log("Fetch Firebase Config")
//            FirebaseHelper.fetchConfig {
//                val usedTime = System.currentTimeMillis() - EventHelper.launchTimeStart
//                if (it == null) {
//                    fail.invoke()
//                    EventHelper.configResultValue = EventHelper.ConfigResultValue.FAIL
//                    EventHelper.logFetchConfig(usedTime, EventHelper.ResultValue.FAIL, EventHelper.ConfigTypeValue.FIREBASE)
//                    return@fetchConfig
//                }
//                EventHelper.configResultValue = EventHelper.ConfigResultValue.SUCCESS
//                EventHelper.logFetchConfig(usedTime, EventHelper.ResultValue.SUCCESS, EventHelper.ConfigTypeValue.FIREBASE)
//                runInIoDispatcher {
//                    val scene = LogScene.FIREBASE_REMOTE_CONFIG
//                    val usedTime = System.currentTimeMillis() - firebaseStartTime
//                    log("Fetch Firebase Config use time: $usedTime")
//                    val responseByte = it
////                val debugByte = getDebugString("response_config_origin_firebase.json")
//                    mFirebaseResponse = parseResponseObj(responseByte, scene)
//                    if (mFirebaseResponse == null) {
//                        checkUsedLocalCache()
//                        return@runInIoDispatcher
//                    }
//
//                    if (mApiResponse == null) {
//                        updateResponseCache(mFirebaseResponse, scene)
//                    }
//
//                    mFirebaseNodeList.clear()
//                    mFirebaseNodeList.addAll(parseNode(mFirebaseResponse!!))
//
//                    if (mFirebaseNodeList.isNotEmpty() && mApiNodeList.isEmpty()) {
//                        updateCurrentNodeItem(mFirebaseNodeList)
//                        updateDefaultProxy(mFirebaseNodeList)
//                        postNodeList(mFirebaseNodeList, NODE_FIREBASE)
//                    }
//                    if (mApiResponse == null) {
//                        parseAdUnit(mFirebaseResponse!!)
//                    }
//
//                    checkUsedLocalCache()
//                }
//            }

            //api
            val scene = LogScene.API
            log("Fetch Api Config")
            val apiStartTime = System.currentTimeMillis()
        val debugString =
            AssetsManager.readFile2String(TunnelApp.context, "response_config_origin.json")
            val responseObj = GsonManager.toObj(debugString, FTResponse::class.java)
            val usedTimeApi = System.currentTimeMillis() - apiStartTime
            log("Fetch Api Config use time: $usedTimeApi")

            mApiResponse = responseObj

            val usedTime = System.currentTimeMillis() - EventHelper.launchTimeStart
            if (mApiResponse == null) {
                fail.invoke()
                EventHelper.configResultValue = EventHelper.ConfigResultValue.FAIL
                EventHelper.logFetchConfig(usedTime, EventHelper.ResultValue.FAIL, EventHelper.ConfigTypeValue.API)
                return@runInIoDispatcher
            }

            mApiResponse?.let {
                EventHelper.configResultValue = EventHelper.ConfigResultValue.SUCCESS
                EventHelper.logFetchConfig(usedTime, EventHelper.ResultValue.SUCCESS, EventHelper.ConfigTypeValue.API)

                updateResponseCache(it, scene)
                mApiNodeList.clear()
                mApiNodeList.addAll(parseNode(it))
                if (mApiNodeList.isNotEmpty()) {
                    updateCurrentNodeItem(mApiNodeList)
                    postNodeList(mApiNodeList, NODE_API)
                    updateDefaultProxy(mApiNodeList)
                }
                parseAdUnit(it)
            }
        }

        checkUsedLocalCache()
    }

    private suspend fun checkUsedLocalCache() {
        //mFirebaseResponse == null
        val allResponseIsNull = mFirebaseResponse == null && mApiResponse == null
        if (!allResponseIsNull) {
            return
        }

        //use local response
        getLocalResponse()?.let {
            mLocalNodeList.clear()
            val nodeList = parseNode(it)
            nodeList.let {
                mLocalNodeList.addAll(it)

            }
            postNodeList(mLocalNodeList, NODE_LOCAL)
            updateDefaultProxy(mLocalNodeList)

            parseAdUnit(it)
        }
    }

    private val mNodeListForFindBestNode by lazy {
        mutableListOf<NodeItem>()
    }

    @Synchronized
    private fun postNodeList(nodeList: MutableList<NodeItem>, nodeApiFrom: Int) {
        log("postNodeList: $nodeApiFrom")
        sourceType = nodeApiFrom
        mNodeListForFindBestNode.clear()
        mNodeListForFindBestNode.addAll(nodeList)
        log("postNodeList: size = ${nodeList.size}")
        _nodeListLiveData.postValue(nodeList)
    }

    suspend fun getDebugString(file: String): String {
        return AssetsManager.readFile2String(TunnelApp.context, file)
    }

    private fun parseResponseObj(
        contentJson: String?,
        scene: String = LogScene.DEFAULT
    ): FTResponse? {
        log("parseResponseObj: $scene")
        return GsonManager.toObj(contentJson ?: "", FTResponse::class.java)
    }

    @Synchronized
    private fun parseAdUnit(responseObject: FTResponse) =
        AdHelper.updateAdUnit(responseObject.aConfig)

    private suspend fun parseNode(responseObject: FTResponse): MutableList<NodeItem> {
        val nodeList = mutableListOf<NodeItem>()
        if (responseObject.servers == null) {
            return nodeList
        }
        if (responseObject.servers?.isEmpty() == true) {
            return nodeList
        }
        responseObject.servers?.map { serverNode ->
//            log("node: ${GsonUtil.toJson(serverNode)}")

            val nodeItem = NodeItem()
            nodeItem.nn = serverNode.nName
            nodeItem.cc = serverNode.cCode
            nodeItem.cn = serverNode.cName
            nodeItem.weight = serverNode.weight
            serverNode.au?.let {
                nodeItem.adIdUnit = serverNode.au
            }
            //check exist from db
            if (mDbProxyIpSet.contains(serverNode.address)) {
                //update node
                val proxyId = mDbProxyIpIdMap[serverNode.address]
                val proxy = ProfileManager.getProfile(proxyId ?: -1)
                proxy?.let {
                    it.apply {
                        method = serverNode.method
                        name = serverNode.nName
                        password = serverNode.pwd
                        remotePort = serverNode.port
                        host = serverNode.address
                    }
                    ProfileManager.updateProfile(it)
                    nodeItem.entity = proxy
                }
            } else {
                //create node
                val ssBean = Profile().apply {
                    host = serverNode.address
                    password = serverNode.pwd
                    remotePort = serverNode.port
                    method = serverNode.method
                    name = serverNode.nName

                }
                val proxy = ProfileManager.createProfile(ssBean)
                nodeItem.entity = proxy
            }

            nodeList.add(nodeItem)
        }

        return nodeList
    }

    @Synchronized
    private fun updateCurrentNodeItem(nodeList: MutableList<NodeItem>) {
        for (i in 0 until nodeList.size) {
            val it = nodeList[i]
            if (it.entity?.id == DataStore.profileId) {
                currentItem = it
                return
            }
        }
    }

    @Synchronized
    private fun updateDefaultProxy(nodeList: MutableList<NodeItem>) {
        if (DataStore.profileId > 0) {
            return
        }

        if (isSmartMode()) {
            updateSmartModeItem("first set")
        } else {
            if (nodeList.isNotEmpty()) {
                DataStore.updateSelectProxy(nodeList[0].entity?.id ?: 0)
            }
        }
    }

    private fun getAllProxyIpFromDb() {
        for (i in 0 until ProfileManager.getAllProfiles().size) {
            val proxy = ProfileManager.getAllProfiles()[i]
            proxy.host.let {
                mDbProxyIpSet.add(it)
                mDbProxyIpIdMap[it] = proxy.id
            }
        }
    }

    fun updateMode(isSmart: Boolean, scene: String = LogScene.DEFAULT) {
        log("updateMode: isSmart = ${isSmart}, $scene")
        StoreManager.putBoolean(Constants.KEY_SMART_MODE, isSmart)
    }

    /**
     * first use firebase
     */
    private fun updateResponseCache(response: FTResponse?, scene: String = LogScene.DEFAULT) {
        response ?: return
        val json = response.toJson()
        log("updateResponseCache: ${scene} -> responseJson = $json")
        StoreManager.putString(Constants.KEY_LOCAL_RESPONSE, json)
    }

    fun hasLocalConfigCache(): Boolean {
        val hasCache = getLocalResponse() != null
        log("hasLocalConfigCache: $hasCache")
        return hasCache
    }

    private fun getLocalResponse(): FTResponse? {
        try {
            val json = StoreManager.getString(Constants.KEY_LOCAL_RESPONSE)
            log("getLocalResponseCache = $json")
            return GsonManager.toObj(json, FTResponse::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isSmartMode() = StoreManager.getBoolean(Constants.KEY_SMART_MODE, true)

    fun getFastestNode(): NodeItem? {
        if (mBestNode == null && nodeListLiveData.value?.isNotEmpty() == true) {
            updateSmartModeItem("mBestNode is null")
        }
        return mBestNode
    }

    fun updateSmartModeItem(scene: String = LogScene.DEFAULT) {
        log("updateSmartModeItem: $scene")
        if (mFirebaseNodeList.isEmpty()) {
            mBestNode = null
            log("updateSmartModeItem: $scene, list is empty")
            return
        }

        mBestNode = mFirebaseNodeList.random()
        DataStore.updateSelectProxy(mBestNode?.entity?.id ?: 0)

        log("updateSmartModeItem: ${mBestNode?.nn}:${mBestNode?.entity?.host}  $scene")
    }


}