package z.app.allever.android.sample.kotlin.function.flow

import z.app.allever.android.sample.kotlin.function.base.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


/**
 * Kotlin Flow响应式编程，基础知识入门
 * https://mp.weixin.qq.com/s?__biz=MzA5MzI3NjE2MA==&mid=2650269681&idx=1&sn=dbc3ea08e3eecb324dfa46b2e6bd46d3&chksm=8863169ebf149f8869cdb5b07ddbad691c8ec09e741892b7f2010cd8a931e8f17a3dbcba8cd6&scene=178&cur_album_id=1455589563531214850#rd
 */
fun main() {
    runBlocking {
        launch {
            clodFlow().collect {
                log("收到：$it")
            }
        }
    }
}

fun clodFlow() = flow<String> {
    for (i in 0..2) {
        delay(1000)
        emit("数据来了 $i")
    }
}