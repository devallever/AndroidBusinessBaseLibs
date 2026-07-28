package z.app.allever.android.sample.kotlin.function.coroutine

import kotlinx.coroutines.*
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val mCoroutineJob = Job()
private val mGlobalCoroutineScope = CoroutineScope(mCoroutineJob)

fun main() {
//    globalCoroutineLaunchTest()
//    runBlockingTest()
//    launchCoroutineTest()
//    runBlocking {
//        createCoroutineWithCoroutineScopeTest()
//    }
//    createCoroutineWithCoroutineScopeTest2()
//    mGlobalCoroutineScope.launch {
//        delay(1000)
//        println("run on mGlobalCoroutineScope 1")
//    }
//    mGlobalCoroutineScope.launch {
//        delay(1500)
//        println("run on mGlobalCoroutineScope 2")
//    }
//    mCoroutineJob.cancel()
//    Thread.sleep(2000)

    asyncForResultTest()
    asyncForResultTest2()
    withContextCoroutineTest()
    mGlobalCoroutineScope.launch {
        requestTest()
    }
    Thread.sleep(1000)
    println("run on main")
}

fun globalCoroutineLaunchTest() {
    val job = GlobalScope.launch {
        println("run on global scope launch")
        delay(200)
        println("run on global scope launch finish")
    }
}

fun runBlockingTest() {
    //阻塞当前线程
    runBlocking {
        delay(1000)
        println("run on runBlocking")
    }
}

fun launchCoroutineTest() {
    runBlocking {
        launch {
            println("run on launch 1")
            suspendFunTest()
            noneSuspendFunTest()
        }

        launch {
            println("run on launch 2")
            suspendFunTest()
            noneSuspendFunTest()
        }
    }
}

fun noneSuspendFunTest() {
    //deley(1000)
    println("cant run on suspend fun")
}

suspend fun suspendFunTest() {
    delay(1000)
    println("run on suspend fun")
}

suspend fun createCoroutineWithCoroutineScopeTest()  = coroutineScope {
    launch {
        println("run on coroutineScope")
    }
}

fun createCoroutineWithCoroutineScopeTest2() {
    runBlocking {
        coroutineScope {
            delay(1000)
            println("run on coroutineScope 1")
        }

        coroutineScope {
            delay(1000)
            println("run on coroutineScope 2")
        }
        println("run on outer coroutine")
    }
}

fun asyncForResultTest() {
    println("run on asyncForResultTest")
    runBlocking {
        val start = System.currentTimeMillis()
        val result1 = async {
            println("run on async 1")
            delay(1000)
            5 + 5
        }.await()

        val result2 = async {
            println("run on async 2")
            delay(1000)
            4 + 6
        }.await()
        val result = result1 + result2
        println("result = $result")
        val end = System.currentTimeMillis()
        println("用时 = ${end - start}")
    }
}

fun asyncForResultTest2() {
    println("run on asyncForResultTest2")
    runBlocking {
        val start = System.currentTimeMillis()
        val result1 = async {
            println("run on async 1")
            delay(1000)
            5 + 5
        }

        val result2 = async {
            println("run on async 2")
            delay(1000)
            4 + 6
        }
        val result = result1.await() + result2.await()
        println("result = $result")
        val end = System.currentTimeMillis()
        println("用时 = ${end - start}")
    }
}

fun withContextCoroutineTest() {
    mGlobalCoroutineScope.launch {
        val result = withContext(Dispatchers.Default) {
            println("run on withContext")
            1 + 1
        }
        println("result = $result")
    }
}


suspend fun requestTest() {
    netCallBackTest("url", object : NetCallback {
        override fun onSuccess() {

        }
        override fun onFail() {

        }
    })

    val result = netCallBackTestWithSuspendCoroutine("url")
    println("net result = $result")
}

fun netCallBackTest(address: String, callback: NetCallback) {
    callback.onSuccess()
}

suspend fun netCallBackTestWithSuspendCoroutine(url: String): String {
    return suspendCoroutine { continuation ->
        netCallBackTest(url, object : NetCallback {
            override fun onSuccess() {
                continuation.resume("request success")
            }

            override fun onFail() {
                continuation.resumeWithException(RuntimeException("request fail"))
            }
        })
    }

}

interface NetCallback {
    fun onSuccess()
    fun onFail()
}