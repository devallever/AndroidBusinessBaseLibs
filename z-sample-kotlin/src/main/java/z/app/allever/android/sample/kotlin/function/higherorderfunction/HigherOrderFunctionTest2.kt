package z.app.allever.android.sample.kotlin.function.higherorderfunction

fun main() {
    //传递函数
    simpleHigherFunTest1(::baseFun)

    //传递Lambda
    val lambda = { }
    simpleHigherFunTest1(lambda)
    simpleHigherFunTest1({})
    simpleHigherFunTest1 {  }

    //lambda参数方式传入
    val lambdaResult = {
        1 + 1
    }
    simpleHigherFunTest2(lambdaResult)
    simpleHigherFunTest2 {
        1 + 1
    }

    higherFunTest1(1, 2, { a: Int, b: Int ->
        a + b
    })

    higherFunTest1(1, 2) {a, b ->
        a + b
    }

    inlineHigherFunTest {
        val result = 1 + 1
    }
}

fun baseFun() {

}

fun simpleHigherFunTest1(block: () -> Unit) {
    //执行Lambda代码块
    block()
}

fun simpleHigherFunTest2(block: () -> Int) {
    //执行Lambda代码块
    block()
}

fun higherFunTest1(a: Int, b: Int, block: (a: Int, b: Int) -> Int) {
    block(a, b)
}

inline fun inlineHigherFunTest(block: () -> Unit) {
    block()
}

inline fun inlineHigherFunTest2(block: () -> Unit, noinline block2: () -> Unit) {

}