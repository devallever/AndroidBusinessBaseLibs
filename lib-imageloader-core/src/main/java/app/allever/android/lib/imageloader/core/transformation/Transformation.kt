package app.allever.android.lib.imageloader.core.transformation

import android.graphics.Bitmap

/**
 * 图片变换接口 - 函数式接口
 *
 * 输入一个 Bitmap，输出一个经过变换的新 Bitmap
 * 多个 Transformation 可以通过 ComposeTransform 组合使用
 *
 * 注意：实现方负责回收输入 Bitmap（如果不再需要）
 */
fun interface Transformation {

    /**
     * 对源 Bitmap 执行变换操作
     * @param source 原始 Bitmap
     * @return 变换后的新 Bitmap
     */
    fun transform(source: Bitmap): Bitmap

    /**
     * 缓存 Key 片段
     * 相同变换应返回相同 key，用于缓存命中判断
     */
    fun key(): String = this::class.java.simpleName
}
