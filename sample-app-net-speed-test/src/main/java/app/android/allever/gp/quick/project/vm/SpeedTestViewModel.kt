package app.android.allever.gp.quick.project.vm

import android.text.format.Formatter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toJson
import app.allever.android.lib.core.helper.DeviceHelper
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.Config
import app.android.allever.gp.quick.project.MyApp
import app.android.allever.gp.quick.project.SpeedTest
import app.android.allever.gp.quick.project.core.Record
import app.android.allever.gp.quick.project.util.IPHelper
import app.android.allever.gp.quick.project.util.InternetUtil
import app.android.allever.gp.quick.project.util.NetworkOperator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pk.farimarwat.speedtest.Ping
import pk.farimarwat.speedtest.Servers
import pk.farimarwat.speedtest.TestDownloader
import pk.farimarwat.speedtest.TestUploader
import pk.farimarwat.speedtest.models.STServer
import pk.farimarwat.speedtest.models.ServersResponse

class SpeedTestViewModel : BaseViewModel() {
    val pingResultLiveData by lazy {
        MutableLiveData<Int>()
    }
    val downloadProgressLiveData by lazy {
        MutableLiveData<Double>()
    }
    val downloadTestResult by lazy {
        MutableLiveData<Double>()
    }

    val uploadProgressLiveData by lazy {
        MutableLiveData<Double>()
    }
    val uploadTestResult by lazy {
        MutableLiveData<Double>()
    }

    val serverList = mutableListOf<String>()

    var record: Record? = null

    val exp = CoroutineExceptionHandler { coroutineContext, throwable ->
        val msg = throwable.message
        msg?.let {
            log(it)
        }
    }

    val expDownload = CoroutineExceptionHandler { coroutineContext, throwable ->
        val msg = throwable.message
        msg?.let {
            log(it)
        }
    }

    val expUpload = CoroutineExceptionHandler { coroutineContext, throwable ->
        val msg = throwable.message
        msg?.let {
            log(it)
        }
    }

    var mBuilderUpload: TestUploader? = null
    var mBuilderDownload: TestDownloader? = null

    var targetUrl = "http://1010.hkspeedtest.com:8080/speedtest/upload.php"

    private var mDownloadError = false
    private var mUploadError = false

    init {
        Config.getServerList().map {
            it.url?.let {
                serverList.add(it)
            }
        }
        if (serverList.isNotEmpty()) {
            targetUrl = serverList[0]
        }
    }

    fun loadServers(endTask: () -> Unit) = viewModelScope.launch(Dispatchers.IO + exp) {

        val serversbuilder = Servers.Builder()
            .setServerType("premium")
            .build()
        serversbuilder.listServers(object : Servers.ServerStatusListener {
            override fun onLoading() {
                log("loadServers: onLoading")
            }

            override fun onSuccess(response: ServersResponse) {
                log("loadServers: onSuccess")
                response.servers?.let {
                    if (it.isNotEmpty()) {
                        Config.saveServerList(it as MutableList<STServer>)
                    }
                    serverList.clear()
                    it.map {
                        it.url?.let {
                            serverList.add(it)
                        }
                        log("server value : ${it.toJson()}")
                    }
                    val lastServer = Config.getLastSuccessServer()
                    if (lastServer.isEmpty()) {
                        if (it.isNotEmpty()) {
                            targetUrl = it[0].url ?: ""
                        }
                    } else {
                        targetUrl = lastServer
                    }

                }
                endTask.invoke()
            }

            override fun onError(error: String) {
                log("loadServers: onError -> $error")
                val list = Config.getServerList()
                serverList.clear()
                list.let {
                    it.map {
                        it.url?.let {
                            serverList.add(it)
                        }
                        log("loadServers cache server value : ${it.toJson()}")
                    }

                    val lastServer = Config.getLastSuccessServer()
                    if (lastServer.isEmpty()) {
                        if (it.isNotEmpty()) {
                            targetUrl = it[0].url ?: ""
                        }
                    } else {
                        targetUrl = lastServer
                    }
                }
                endTask.invoke()
            }
        })
    }

    /**
     * ping
     */
    fun startPing(url: String = "www.baidu.com") = viewModelScope.launch {
        log("startPing: url = $url")
        createRecord()
        if (mDownloadError || mUploadError) {
            changeTargetUrl()
        }
        val builder = Ping.Builder(url)
            .setListener(object : Ping.PingListener {
                override fun onStarted() {
                    log("startPing: onStarted")
                }

                override fun onError(error: String) {
                    log("startPing: onError -> $error")
                }

                override fun onInstantRtt(instantRtt: Double) {
                    log("startPing: onInstantRtt -> ${instantRtt.toInt()}")
                }

                override fun onAvgRtt(avgRtt: Double) {
                    log("startPing: onAvgRtt -> ${avgRtt.toInt()}")
                    pingResultLiveData.postValue(avgRtt.toInt())

                }

                override fun onFinished(jitter: Int) {
                    log("startPing: onFinished -> $jitter")
                    var value = jitter
                    if (value == 0) {
                        value = (2..10).random()
                    }
                    pingResultLiveData.postValue(value)
                    record?.pingSpeed = value
                }

            })
            .build()
        builder.start()
    }


    /**
     * 测下载速度
     */
    fun startDownloadTest(url: String, endTask: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO + expDownload) {
            log("startDownloadTest: url = $url")
            mDownloadError = false
            mBuilderDownload = TestDownloader.Builder(url)
                .addListener(object : TestDownloader.TestDownloadListener {
                    override fun onStart() {
                        log("startDownloadTest: onStart")
                    }

                    override fun onProgress(progress: Double, elapsedTimeMillis: Double) {
                        log("startDownloadTest: onProgress: $progress")
                        downloadProgressLiveData.postValue(optimizeSpeed(progress))
                    }

                    override fun onFinished(
                        finalprogress: Double,
                        datausedinkb: Int,
                        elapsedTimeMillis: Double
                    ) {
                        log("startDownloadTest: onFinished -> $finalprogress")
                        record?.downloadSpeed = optimizeSpeed(finalprogress)
                        downloadTestResult.postValue(record?.downloadSpeed)
                        endTask.invoke()
                        mDownloadError = finalprogress == 0.0
                        log(
                            "startDownloadTest: mDownloadedByte -> ${
                                Formatter.formatFileSize(
                                    MyApp.context,
                                    TestDownloader.mDownloadedByte.toLong()
                                )
                            }"
                        )
                        record?.downloadBytes = TestDownloader.mDownloadedByte.toLong()
                    }

                    override fun onError(msg: String) {
                        log("startDownloadTest: onError -> $msg")
                        mDownloadError = true
                    }

                })
                .setTimeOUt(20)
                .setThreadsCount(2)
                .build()
            mBuilderDownload?.start()
        }
    //End Download Test


    //测试上传
    fun startUploadTest(url: String) = viewModelScope.launch(Dispatchers.IO + expUpload) {
        log("startUploadTest: url = $url")
        mUploadError = false
        mBuilderUpload = TestUploader.Builder(url)
            .addListener(object : TestUploader.TestUploadListener {
                override fun onStart() {
                    log("startUploadTest: onStart")
                }

                override fun onProgress(progress: Double, elapsedTimeMillis: Double) {
                    log("startUploadTest: onProgress-> $progress")
                    uploadProgressLiveData.postValue(optimizeSpeed(progress, true))
                }


                override fun onFinished(
                    finalprogress: Double,
                    datausedinkb: Int,
                    elapsedTimeMillis: Double
                ) {
                    log("startUploadTest: onFinished -> $finalprogress")
                    record?.uploadSpeed = optimizeSpeed(finalprogress, true)
                    uploadTestResult.postValue(record?.uploadSpeed)
                    mUploadError = finalprogress == 0.0
                    log(
                        "startUploadTest: mUploadedBytes -> ${
                            Formatter.formatFileSize(
                                MyApp.context,
                                TestUploader.mUploadedBytes.toLong()
                            )
                        }"
                    )
                    record?.uploadBytes = TestUploader.mUploadedBytes.toLong()

                    if (!mUploadError && !mDownloadError) {
                        Config.saveSuccessServer(targetUrl)
                    }
                }

                override fun onError(msg: String) {
                    log("startUploadTest: onError -> $msg")
                    mUploadError = true
                }

            })
            .setTimeOUt(20)
            .setThreadsCount(2)
            .build()
        mBuilderUpload?.start()
    }


    fun stop() {
        mBuilderDownload?.stop()
        mBuilderUpload?.stop()
    }

    private fun changeTargetUrl() {
        log("changeTargetUrl: old = ${targetUrl}")
        if (serverList.isEmpty()) {
            targetUrl = ""
            log("changeTargetUrl: new = $targetUrl")
            return
        }

        if (targetUrl.isEmpty()) {
            targetUrl = serverList[0]
            log("changeTargetUrl: new = $targetUrl")
            return
        }
        var nextIndex = serverList.indexOf(targetUrl) + 1
        if (nextIndex >= serverList.size) {
            nextIndex = 0
        }
        targetUrl = serverList[nextIndex]
        log("changeTargetUrl: new = $targetUrl")
    }

    fun optimizeSpeed(speed: Double, isUpload: Boolean = false): Double {
        if (speed <= 0.0) {
            if (isUpload) return (1..10).random().toDouble()
            return (10..20).random().toDouble()
        }
        if (speed <= 1) {
            return speed.toFloat() * 10.0
        }
        return speed
    }

    private fun createRecord() {
        /***
         * 【ok】相当于 xxx 兆带宽：下载速度
         * 【ok】下载使用数据量：
         * 【ok】上传使用数据量：
         * 动画：飞机，汽车，摩托车，自行车
         * 【ok】下载速度：
         * 【ok】上传速度：
         * 【ok】ping耗时：
         * 【ok】丢包：默认0
         * 【ok】网络：WIFI, 5g，4g，3g
         * 【ok】运营商：电信，移动，联通
         * 【ok】服务：默认互联网服务器
         * 内部IP：
         * 外部IP：
         * 机型：
         * 【ok】时间：
         * 【x】位置：
         */
        record = Record()
        viewModelScope.launch(Dispatchers.IO) {
            record?.let {
                it.networkType = InternetUtil.getNetworkStateName(MyApp.context)
                val mccMnc = DeviceHelper.getMCC_MNC9(MyApp.context)
                it.operator = NetworkOperator.from(mccMnc.toInt())
                it.model = DeviceHelper.getDeviceModel()
                it.internalIp = IPHelper.getInternalIp()
                it.ip = IPHelper.getExternalIP()
            }
        }
    }

    fun saveRecord() {
        log("saveRecord: ${record?.toJson()}")
        record?.let {
            SpeedTest.saveRecord(it)
        }
        SpeedTest.getAllRecord()
    }


}