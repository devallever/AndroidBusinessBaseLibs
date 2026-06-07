package app.allever.android.lib.imageloader.core.transformation

import android.graphics.Bitmap

/**
 * 变换组合器
 *
 * 将多个 Transformation 按顺序组合为一个
 * 执行顺序即添加顺序：bitmap → t1 → t2 → ... → result
 *
 * 用法：
 *   val combined = ComposeTransform(RoundedCorners(16f), BlurTransformation(25))
 *   // 等价于先圆角再模糊
 */
class ComposeTransform(private val transformations: List<Transformation>) : Transformation {

    constructor(vararg ts: Transformation) : this(ts.toList())

    init {
        require(transformations.isNotEmpty()) { "至少需要一个 Transformation" }
    }

    override fun transform(source: Bitmap): Bitmap {
        return transformations.fold(source) { current, transformation ->
            transformation.transform(current)
        }
    }

    override fun key(): String = transformations.joinToString("+") { it.key() }
}

/** 操作符重载：支持使用 + 组合变换 */
operator fun Transformation.plus(other: Transformation): Transformation =
    ComposeTransform(this, other)
