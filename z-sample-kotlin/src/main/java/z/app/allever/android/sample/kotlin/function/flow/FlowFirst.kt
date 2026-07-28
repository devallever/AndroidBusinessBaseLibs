package z.app.allever.android.sample.kotlin.function.flow

import z.app.allever.android.sample.kotlin.function.base.log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis


/**
 * 挂起函数可以异步的返回单个值，但是该如何异步返回多个计算好的值呢？这正是 Kotlin 流（Flow）的用武之地。
 * 使用 List 结果类型，意味着我们只能一次返回所有值。 我们可以使用 Flow 类型：
 *
 *
 *
 */
fun main() {

//    val stateFlow = MutableStateFlow("")
//    stateFlow.value = "hello"
//    stateFlow.value = "world"
//
//    val shareFlow = MutableSharedFlow<String>()
//    shareFlow.tryEmit("hello")
//    shareFlow.tryEmit("world")

    runBlocking {
//        launch {
//            for (i in 1..3) {
//                delay(1000)
//                log("I'm not blocked $i")
//            }
//        }

        //Flow 是一种类似于序列的冷流 — 这段 flow 构建器中的代码直到流被收集的时候才运行。
        commonFlow().collect {
            log("commonFlow1 = $it")
        }

        delay(500)
        commonFlow().collect {
            log("commonFlow2 = $it")
        }

        delay(500)
        //取消流
        withTimeoutOrNull(2500) {
            commonFlow().collect {
                log("commonFlow3 = $it")
            }
        }

        //asFlow快速构建
        delay(500)
        commonFlowWithAsFlow().collect {
            log("commonFlowWithAsFlow = $it")
        }

        /**
         * 转换操作符: transform/map/filter
         */
        //map转换操作符: 一个请求中的流可以使用 map 操作符映射出结果，即使执行一个长时间的请求操作也可以使用挂起函数来实现
        commonFlow()
            .map { request ->
                //int -> String
                performRequest(request)
            }
            .collect { response ->
                log(response)
            }
        //使用 transform 操作符，我们可以 发射 任意值任意次
        commonFlow()
            .transform { request ->
                emit("正在请求 $request")
                emit(performRequest(request))
            }
            .collect {
                log(it)
            }

        /**
         *  限长操作符: take/
         *  限长过渡操作符（例如 take）在流触及相应限制的时候会将它的执行取消。
         *  协程中的取消操作总是通过抛出异常来执行，这样所有的资源管理函数（如 try {...} finally {...} 块）
         *  会在取消的情况下正常运行：
         */
        //take: 只获取前n个
        commonFlow()
            .take(2)
            .map {
                performRequest(it)
            }
            .collect {
                log(it)
            }

        /**
         * 末端流操作符：collect/toList/toSet...
         * 末端操作符是在流上用于启动流收集的挂起函数。 collect 是最基础的末端操作符，但是还有另外一些更方便使用的末端操作符：

        转化为各种集合，例如 toList 与 toSet。
        获取第一个（first）值与确保流发射单个（single）值的操作符。
        使用 reduce 与 fold 将流规约到单个值。
         */
        //reduce:求和（末端操作符）
        //从第一个元素开始累加值，并将 [操作] 应用于当前累加器值和每个元素
        val reduceResult = (1..5).asFlow()
            .reduce { a, b ->
                a + b
            }
        log("reduceResult = $reduceResult")


        /***
         * 连续流
         */
        commonFlow()
            .filter {
                log("filter $it")
                true
            }
            .map {
                log("map $it")
                "mapResult $it"
            }
            .collect {
                log(it)
            }

        /***
         * 流的上下文
         * 流的收集总是在调用协程的上下文中发生
         * 所以默认的，flow { ... } 构建器中的代码运行在相应流的收集器提供的上下文中。
         * 然而，长时间运行的消耗 CPU 的代码也许需要在 Dispatchers.Default 上下文中执行，
         * 并且更新 UI 的代码也许需要在 Dispatchers.Main 中执行。通常，withContext 用于在 Kotlin 协程中改变代码的上下文，
         * 但是 flow {...} 构建器中的代码必须遵循上下文保存属性，并且不允许从其他上下文中发射（emit）。
         */
//        commonFlowInDefaultContext()
//            .collect {
//                log("commonFlowInDefaultContext $it")
//            }

        //flowOn 操作符: 该函数用于更改流发射的上下文。
        commonFlowWithFlowOn()
            .collect {
                log("commonFlowWithFlowOn $it")
            }


        var methodDuration = measureTimeMillis {
            commonFlow()
                .collect {
                    delay(300)
                    log("methodDuration $it")
                }
        }
        //1800ms
        log("methodDuration = $methodDuration")

        /**
         * 缓冲：buffer/
         * 从收集流所花费的时间来看，将流的不同部分运行在不同的协程中将会很有帮助，特别是当涉及到长时间运行的异步操作时。
         *
         */
        //buffer操作符：缓冲发射项，无需等待,使用 buffer 操作符来并发运行这个 流中发射元素的代码以及收集的代码，
        // 而不是顺序运行它们
        methodDuration = measureTimeMillis {
            commonFlow()
                .buffer()
                .collect {
                    delay(300)
                    log("methodDuration with buffer $it")
                }
        }
        //1200ms
        log("methodDuration with buffer() = $methodDuration")

        /**
         * 合并：conflate/
         * 当流代表部分操作结果或操作状态更新时，可能没有必要处理每个值，而是只处理最新的那个
         */
        // conflate 操作符可以用于跳过中间值，合并发射项，不对每个值进行处理，
        methodDuration = measureTimeMillis {
            commonFlow(0)
                .conflate() //
                .collect {
                    delay(300) // 假装我们花费 300 毫秒来处理它
                    log("methodDuration with conflate $it")
                }
        }
        log("methodDuration with conflate() = $methodDuration")

        /**
         * 处理最新值
         * 当发射器和收集器都很慢的时候，合并是加快处理速度的一种方式。它通过删除发射值来实现。
         * 另一种方式是取消缓慢的收集器，并在每次发射新值的时候重新启动它。有一组与 xxx 操作符执行相同基本逻辑的 xxxLatest 操作符，
         * 但是在新值产生的时候取消执行其块中的代码。
         */
        methodDuration = measureTimeMillis {
            commonFlow(100)
                .collectLatest {
                    delay(300) // 假装我们花费 300 毫秒来处理它
                    log("methodDuration with collectLatest $it")
                }
        }
        log("methodDuration with collectLatest = $methodDuration")

        /**
         * 组合多个流：zip
         *
         */
        var flowInt = (1..3).asFlow()
        var flowString = flowOf("allever", "winchen", "devallever")
        flowInt.zip(flowString) { p1, p2 ->
            "$p1: $p2"
        }
            .collect {
                log("flowInt -> flowString = $it")
            }


//        launch {
//            stateFlow.collect {
//                log("stateFlow = $it")
////            stateFlow = allever
//            }
//        }
//
//
//        launch {
//            shareFlow.collect {
//                log("shareFlow = $it")
//            }
//        }
    }
}


/**
 *
 * Flow 是一种类似于序列的冷流 — 这段 flow 构建器中的代码直到流被收集的时候才运行。
 *
 * 注意使用 Flow 的代码与先前示例的下述区别：
 * 名为 flow 的 Flow 类型构建器函数。
 * flow { ... } 构建块中的代码可以挂起。
 * 流使用 emit 函数 发射 值。
 * 流使用 collect 函数 收集 值。
 */
fun commonFlow(delay: Long = 300): Flow<Int> = flow {
    log("开始发送流------------------------")

    try {
        for (i in 1..3) {
            delay(delay)
//    Thread.sleep(1000)
            log("正在发送 $i")
            emit(i)
        }
    } catch (e: Exception) {
        log("抛异常")
        e.printStackTrace()
    }

}

/**
 * 流构建器: asFlow()
 */
fun commonFlowWithAsFlow() = run { (1..3).asFlow() }

fun commonFlowInDefaultContext() = flow<Int> {
    withContext(Dispatchers.Default) {
        log("commonFlowInDefaultContext 开始发送流------------------------")

        try {
            for (i in 1..3) {
                delay(300)
//    Thread.sleep(1000)
                log("正在发送 $i")
                emit(i)
            }
        } catch (e: Exception) {
            log("抛异常")
            e.printStackTrace()
        }
    }
}

fun commonFlowWithFlowOn() = flow<Int> {
    log("commonFlowWithFlowOn 开始发送流------------------------")

    try {
        for (i in 1..3) {
            delay(300)
//    Thread.sleep(1000)
            log("正在发送 $i")
            emit(i)
        }
    } catch (e: Exception) {
        log("抛异常")
        e.printStackTrace()
    }
}.flowOn(Dispatchers.IO)

suspend fun performRequest(request: Int): String {
    delay(1000) // 模仿长时间运行的异步工作
    return "response $request"
}