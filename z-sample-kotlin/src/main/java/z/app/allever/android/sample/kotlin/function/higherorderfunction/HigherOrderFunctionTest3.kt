package z.app.allever.android.sample.kotlin.function.higherorderfunction

fun main() {
    c(::b)
    c(::a)
    val funA = fun () {
        log("调用了 funA 函数")
    }
    c(funA)

    val funB = {
        log("调用了 funB 函数")
    }
    c(funB)

    val funC = fun(params: String) {
        log(params)
    }
    d("调用了 funC 函数", funC)

    d("调用了Lambda") {
        println(it)
    }

    e { parA, parB ->
        log(parA)
        log(parB)
    }

    f(fun(a: Int, b: Int) : Int{
        log("$a + $b")
        return a + b
    })

    f(fun(a: Int, b: Int) : Int{
        log("$a + $b")
        return a + b
    })

    //Lambda语法  { s: String, i: Int -> 函数体 }
    //化简1： 高阶函数写成Lambda
    f({a: Int, b: Int ->
        log("$a - $b")
        a - b
    })


    //化简2： Lambda作为最后一个参数放到括号外
    f() {a: Int, b: Int ->
        log("$a * $b")
        a * b
    }

    //化简3：只有一个参数， 省略括号
    f {a: Int, b: Int ->
        log("$a / $b")
        a / b
    }

    //化简4：省略参数类型
    f {a, b ->
        log("$a / $b")
        a / b
    }

    //化简5：只有一个参数， 省略参数
    g {
        log("$it*$it")
        it * it
    }

}

fun a() {
    log("调用了 a 函数")
}

fun b() {
    log("调用了 b 函数")
}

fun c(block: () -> Unit) {
    block()
}

fun d(msg: String, block: (String) -> Unit) {
    block(msg)
}

fun e(block: (parA: String, parB: String) -> Unit) {
//    block(parA, parB)
    block("我是parA", "我是parB")
}


fun f(block: (Int, Int) -> Int) {
    val result = block(1, 1)
    log("计算结果 = $result")
}

fun g(block: (Int) -> Int) {
    val result = block(5)
    log("计算结果 = $result")
}

fun higherFun1(funName: () -> Unit) {

}

private fun log(msg: String) {
    println(msg)
}