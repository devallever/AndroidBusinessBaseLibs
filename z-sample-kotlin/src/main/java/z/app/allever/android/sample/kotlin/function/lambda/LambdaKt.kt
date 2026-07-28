package z.app.allever.android.sample.kotlin.function.lambda

import android.view.View

fun main() {
    //lambda 语法
    /***
     * { s: String, i: Int -> 函数体 }
     * 简写 { s, i -> 函数体 }
     */

    val lambda = Lamda()
    //无参-标准写法
    lambda.setOnClickListener({ })
    //无参-化简写法
    lambda.setOnClickListener {  }

    //带参-不使用Lambda
    lambda.setOnLongClickListener(object : Lamda.OnLongClickListener {
        override fun onClick(view: View?) {

        }
    })
    //带参-标准写法
    lambda.setOnLongClickListener({v: View ->

    })

    //带参-化简写法1- Lambda是最后参数时，提取到括号外
    lambda.setOnLongClickListener() {v: View ->

    }

    //带参-化简写法2 - Lambda参数是唯一参数时，省略括号
    lambda.setOnLongClickListener {v: View ->

    }

    //带参-化简写法3 - Lambda简写，省略类型
    lambda.setOnLongClickListener {

    }

    //带参-化简写法4 - Lambda表达式只有一个参数时，省略参数，用it代替
    lambda.setOnLongClickListener {

    }

    //带参-最终版化简写法
    lambda.setOnLongClickListener {

    }
}