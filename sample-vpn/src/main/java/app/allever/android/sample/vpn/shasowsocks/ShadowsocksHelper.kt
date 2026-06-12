package app.allever.android.sample.vpn.shasowsocks

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.RemoteException
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.AssetsHelper
import app.allever.android.lib.core.helper.CoroutineHelper
import app.allever.android.lib.core.helper.GsonHelper
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.aidl.TrafficStats
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.DataStore
import com.github.shadowsocks.utils.log
import io.github.studycwq.extension.asLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object ShadowsocksHelper : ShadowsocksConnection.Callback {

    //for none smart mode
    private var currentItem: NodeItem? = null

    fun getConnectedNodeItem(): NodeItem? {
        if (isServiceConnected()) {
            return currentItem
        }
        return null
    }

    fun getSelectedNodeItem(): NodeItem? {
        return currentItem
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
    val nodeListLiveData = _nodeListLiveData.asLiveData()
    val serviceStateLiveData by lazy {
        DataStore.serverStateLiveData
    }
    private val _remainTimeLiveData by lazy {
        MutableLiveData(0L)
    }
    val remainTimeLiveData = _remainTimeLiveData.asLiveData()
    val appendTimeLiveData by lazy {
        MutableLiveData(false)
    }
    val alreadyAppendLiveData by lazy {
        MutableLiveData(false)
    }
    private val _currentNodeLiveData by lazy {
        MutableLiveData(currentItem)
    }
    val currentNodeLiveData = _currentNodeLiveData.asLiveData()
    private val _speedLiveData by lazy {
        MutableLiveData(TrafficStats())
    }
    val speedLiveData = _speedLiveData.asLiveData()

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
    
    fun dataReady() = nodeListLiveData.value?.isNotEmpty() == true

    fun init() {
        if (mApiNodeList.isNotEmpty()) {

        }
        CoroutineHelper.IO.launch {
            getAllProxyIpFromDb()
            fetchNodeList()
        }

        DataStore.registerSelectedProxyChange {
            _currentNodeLiveData.postValue(currentItem)
        }

        remainTimeLiveData.observeForever {
            if (App.alreadyInBackground) {
                if (it < 0L) {
                    connectAutoStop = true
                    log("update connectAutoStop = true")
                }
            }
        }

        mConnection.connect(App.context, this)
    }

    suspend fun fetchNodeList(success: () -> Unit = {}, fail: () -> Unit = {}) {
        //firebase
        CoroutineHelper.IO.launch {
            val debugString =
                AssetsHelper.getJson(App.context, "response_config_origin.json")
            log("localJson = $debugString")
            val responseObj = GsonHelper.fromJson(debugString, FTResponse::class.java)

            mApiResponse = responseObj

            if (mApiResponse == null) {
                fail.invoke()
                return@launch
            }

            responseObj?.let {
                it.servers?.map {
                    log("serverJson = ${it.toJson()}")
                }
            }

            mApiResponse?.let {
                mApiNodeList.clear()
                mApiNodeList.addAll(parseNode(it))
                mApiNodeList.map {
                    log("nodeItemJson = ${it.toJson()}")
                }
                if (mApiNodeList.isNotEmpty()) {
                    switchNode()
                    postNodeList(mApiNodeList)
                }
            }
        }

    }

    fun switchNode() {
        var needStart = false
        if (isServiceConnected()) {
            Core.stopService()
            needStart = true
        }

        log("mApiNodeList.size: ${mApiNodeList.size}")
        val nextItem = mApiNodeList.random()
        if (nextItem == null) {
            log("nextItem == null")
            return
        }
        currentItem = nextItem
        log("switchNode: ${currentItem?.nn}")
        _currentNodeLiveData.postValue(currentItem)

        DataStore.updateSelectProxy(currentItem?.entity?.id ?: 0)

        if (needStart) {
            CoroutineHelper.IO.launch {
                delay(2000)
                getSelectedNodeItem()?.entity?.let {
                    if (needStart) {
                        Core.startService()
                    }
                }
            }
        }
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
        mConnection.disconnect(App.context)
        mConnection.connect(App.context, this)
    }


    private fun changeState(
        state: BaseService.State,
        msg: String? = null,
    ) {
        DataStore.serviceState = state

        if (state == BaseService.State.Connected) {
            Core.cancelAutoNotification()
        }

        log("changeState: ${state.name}, msg: ${msg ?: "null"}")

        if (state == BaseService.State.Stopped && msg != null) {
            CoroutineHelper.MAIN.launch {
                _connectFailFlow.emit(true)
            }
        }

    }

    fun isServiceConnected() = BaseService.State.Connected == DataStore.serviceState

    fun isServiceConnecting() = BaseService.State.Connecting == DataStore.serviceState

    fun isServiceStopped() = BaseService.State.Stopped == DataStore.serviceState

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



    fun appendConnectTime() {
        appendTimeLiveData.postValue(true)
        mConnection.appendConnectTime()
    }

    fun resetConnectTime() = mConnection.resetConnectTime()

    fun getConnectDuration() = mConnection.getConnectDuration()



    private suspend fun checkUsedLocalCache() {
        //mFirebaseResponse == null
        val allResponseIsNull = mFirebaseResponse == null && mApiResponse == null
        if (!allResponseIsNull) {
            return
        }
    }

    private val mNodeListForFindBestNode by lazy {
        mutableListOf<NodeItem>()
    }

    @Synchronized
    private fun postNodeList(nodeList: MutableList<NodeItem>) {
        mNodeListForFindBestNode.clear()
        mNodeListForFindBestNode.addAll(nodeList)
        log("postNodeList: size = ${nodeList.size}")
        _nodeListLiveData.postValue(nodeList)
    }


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

        if (nodeList.isNotEmpty()) {

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

}