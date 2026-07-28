package z.app.allever.android.sample.kotlin.function.higherorderfunction

/**
 * 高阶函数：接收函数类型的参数 或者 函数返回值是函数类型
 * 函数类型：编程语言中有整形，布尔型等等，Kotlin中增加了函数类型
 * 高阶函数基本语法规则： (String, Int) -> Unit
 * 最简单的高阶函数：() -> Unit
 * 高阶函数原理：Kotlin编译器将高阶函数语法转换成Java支持的语法结构，函数类型(Lambda)转换成Function接口，并实现invoke()方法
 * 在invoke()方法中实现Lambda代码块的逻辑
 * Lambda原理：Lambda表达式转换成Function匿名内部类实现，会造成额外的内存和性能开销
 */
fun main() {
//    test1()

    test2()

    test3()

    test4()
}


fun test1() {
    aHigherFunc {
        println("run in higher order function")
    }
}

/**
 * 高阶函数：接收函数类型的参数
 */
fun aHigherFunc(task: () -> Unit) {
    //运行传入代码块(Lambda)
    task()
}

fun test2() {
    val a = 100
    val b = 80
    //::plus 是一种函数引用的写法, 将plus()函数作为参数传递给num1AndNum2()
    //一般不用这种方式，因为比较复杂
    val resultPlus = num1AndNum2(a, b, ::plus)
    val resultMinus = num1AndNum2(a, b, ::minus)
    println("resultPlus = $resultPlus")
    println("resultMinus = $resultMinus")
}

fun test3() {
    val a = 100
    val b = 80

    //使用Lambda表达式是最常见也是最普遍的高阶函数调用方式
    val resultPlus = num1AndNum2(a, b) {i: Int, i2: Int ->
        return@num1AndNum2 i + i2
    }
    //Lambda最后一行作为返回值
    val resultMinus = num1AndNum2(a, b) {i, i2 ->
        i - i2
    }
    println("resultPlus = $resultPlus")
    println("resultMinus = $resultMinus")
}

fun num1AndNum2(num1: Int, num2: Int, operator: (Int, Int) -> Int): Int {
    return operator(num1, num2)
}

/***
 * inline: 内联函数，
 * 1.将Lambda表达式的代码块替换到函数类型参数调用的地方
 * 2.将内联函数的代码替换到函数调用的地方
 * 因此通过替换代码的方式消除Lambda的性能和内存开销
 * 内联函数和非内联函数的区别：
 * 1. 内联函数没有真正的参数属性，因为编译时进行代码替换
 * 2. 内联函数的参数只允许传递给另一个内联函数
 * 3. 内联函数引用的Lambda的表达式中可以使用return返回进行函数返回，非内联函数只能局部返回return@xxx
 *
 *
 */
inline fun num1AndNum3(num1: Int, num2: Int, operator: (Int, Int) -> Int): Int {
    return operator(num1, num2)
}

/***
 * inline会将所有引用的Lambda表达式进行全部内联
 * noinline 用来排除内联
 */
inline fun inlineTest(block1: () -> Unit, noinline block2: () -> Unit) {

}


fun plus(a: Int, b: Int): Int {
    return a + b
}

fun minus(a: Int, b: Int): Int {
    return a - b
}

fun test4() {
    val fruitList = listOf("Apple", "Banana", "Orange")
    val result = StringBuilder().build {
        fruitList.map {
            append(it).append("\n")
        }
    }
    println(result)
}

/***
 * 高阶函数完整语法结构： ClassName.() 表示这个函数类型是定义在哪个类当中
 * 创建apply()函数一样的语法特点，使Lambda拥有某个对象的上下文
 */
fun StringBuilder.build(block: StringBuilder.() -> Unit): StringBuilder {
    block()
    return this
}

fun <T> T.build(block: T.() -> Unit): T {
    block()
    return this
}