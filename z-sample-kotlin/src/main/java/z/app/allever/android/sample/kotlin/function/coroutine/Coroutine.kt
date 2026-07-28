package z.app.allever.android.sample.kotlin.function.coroutine
import kotlinx.coroutines.*

/***
 * Coroutine 协程- 线程框架 - 非阻塞式挂起
 * 单线程模式下模拟多线程编程的效果；
 * 调度过程不需要操作系统参与，使得协程并发效率出奇的高
 */
fun main() {
    globalScopeLaunch()

    runBlockingCoroutine()

    multiCoroutine()

    coroutineScopeCoroutine()

    suspendFun()

    cancelCoroutine()

    coroutineResultWithAsync()

    coroutineResultWithAsync2()

    withContextCoroutine()
}

private fun globalScopeLaunch() {
    //1.最简单的创建协程: 使用GlobalScope.launch()创建协程作用域
    //GlobalScope.launch()创建的是顶层协程, 当程序结束(运行完Main()方法)时协程也会跟着一起结束
    GlobalScope.launch {
        //协程作用域
        println("codes run in coroutine scope")
        //delay()是一个非阻塞式的挂起函数, 只会挂起当前协程，不会影响其他协成运行
        //delay()只能在协程作用域或挂起函数中使用
        delay(1500)
        println("codes run in coroutine scope finished")
    }
    //阻塞当前线程
    Thread.sleep(1000)
}

fun runBlockingCoroutine() {
    //2.使用runBlocking()创建线程作用域
    //runBlocking()使协程中所有代码和子协程都运行完了再结束程序，执行完之前会阻塞当前线程
    //runBlocking()通常只应该使用在测试环境,由于性能问题
    runBlocking {
        println("codes run in coroutine scope")
        delay(1000)
        println("codes run in coroutine scope finished")
    }
}

fun multiCoroutine() {
    //3.使用launch()创建子协程
    //子协程特点：外层协程结束后所有子协程也会一同结束
    //launch()只能在协程作用域中才能使用
    runBlocking {
        //多个子协程在同一线程中并发运行
        launch {
            println("launch 1")
            delay(1000)
            println("launch 1 finished")
        }

        launch {
            println("launch 2")
            delay(1000)
            println("launch 2 finished")
        }
    }
}

// 4.使用coroutineScope()创建子作用域(非子协程)
// coroutineScope()是一个挂起函数，继承外部协程作用域，并创建一个子作用域
// coroutineScope()外部必须是协程作用域
// coroutineScope()使作用域内所有代码和子协程全部执行完之前阻塞当前协程
// coroutineScope()只能在协程作用域或挂起函数中使用
fun coroutineScopeCoroutine() {

    //Suspend function 'coroutineScope' should be called only from a coroutine or another suspend function
    //挂起功能'coroutineScope'应该仅从协程或其他挂起功能调用
    runBlocking {
        coroutineScope {
            launch {
                for (i in 0 until 10) {
                    println(i)
                    delay(1000)
                }
            }
        }
        println("coroutineScope finished")
    }
    println("runBlocking finished")
}

fun suspendFun() {
    val startTime = System.currentTimeMillis()
    runBlocking {
        repeat(10) {
            launch {
                printDot()
            }
        }
    }
    val endTime = System.currentTimeMillis()
    println(endTime - startTime)
}

//suspend 将将函数声明为挂起函数，挂起函数之间可以相互调用
//挂起函数非协程作用域, 无法使用launch()创建协程
suspend fun printDot() = coroutineScope {
    launch {
        println(".")
        delay(1000)
    }

}

//GlobalScope.launch()不常用
fun cancelCoroutine() {
    val job = GlobalScope.launch {

    }
    job.cancel()
}

//5.CoroutineScope()方法创建CoroutineScope对象
//使用CoroutineScope的launch()方法创建协程
fun standardCreateCoroutine() {
    val job = Job()
    val scope = CoroutineScope(job)
    scope.launch {

    }
    //cancel()会取消同一作用域内的所有协程
    job.cancel()
}

//6. 使用async()创建子协程
//async()只能用在协程作用域
//async()返回Deferred对象
//调用DeferredObj.await()方法获取协程返回值
//运行原理：调用async()后，代码块中的代码会立即执行。当调用await()时，如果代码块还没执行完，
//await()会阻塞当前协程，知道可以获得async()的执行结果
fun coroutineResultWithAsync() {
    runBlocking {
        val start = System.currentTimeMillis()
        val result1 = async {
            delay(1000)
            5 + 5
        }.await()
        println(result1)
        val result2 = async {
            delay(1000)
            4 + 6
        }.await()
        println("result = ${result1 + result2}")
        val end = System.currentTimeMillis()
        println("cost ${end - start}")
    }
}


fun coroutineResultWithAsync2() {
    runBlocking {
        val start = System.currentTimeMillis()
        val deferred1 = async {
            delay(1000)
            5 + 5
        }
        val deferred2 = async {
            delay(1000)
            4 + 6
        }
        println("result = ${deferred1.await() + deferred2.await()}")
        val end = System.currentTimeMillis()
        println("cost ${end - start}")
    }
}


//7.使用withContext()创建子协程
//withContext()传入一个线程参数： 通过线程参数给协程指定一个具体的运行线程
//线程参数：
// Dispatchers.Default: 低并发的线程策略(默认)，适用于计算密集型，开启过高的并发反而会可能影响任务运行效率
// Dispatchers.IO: 高并发的线程策略，适用于执行代码大多数时间需要阻塞和等待，例如网络请求，为了支持更高的并发数量
// Dispatchers.Main: 不会开启子线程，而是在Android的主线程中执行代码, 只能在Android中使用
fun withContextCoroutine() {
    runBlocking {
        val result = withContext(Dispatchers.Default) {
            5 + 5
        }
    }
}
