package app.allever.android.sample.audiovideo.android

/**
 * SurfaceView/TextureView 视频缩放模式
 *
 * 用于控制视频在 SurfaceView 或 TextureView 中的显示方式。
 * 注意：此枚举仅对 SurfaceView 和 TextureView 生效，
 * PlayerView 有自己独立的缩放控制机制。
 */
enum class VideoScaleMode {
    /**
     * 保持宽高比，完整显示视频内容
     *
     * 特点：
     * - 视频完全可见，不会被裁剪
     * - 可能出现黑边（Letterbox/Pillarbox）
     * - 适用于需要看到完整视频内容的场景
     *
     * 示例：
     * - 容器 16:9 + 视频 4:3 → 左右黑边
     * - 容器 4:3 + 视频 16:9 → 上下黑边
     */
    FIT_CENTER,

    /**
     * 保持宽高比，填满整个容器
     *
     * 特点：
     * - 无黑边，完全填充容器
     * - 可能裁剪视频边缘内容
     * - 适用于背景视频或可接受裁剪的场景
     *
     * 示例：
     * - 容器 16:9 + 视频 4:3 → 裁剪上下部分
     * - 容器 4:3 + 视频 16:9 → 裁剪左右部分
     */
    CROP_CENTER,

    /**
     * 拉伸以填满整个容器
     *
     * 特点：
     * - 无黑边，完全填充容器
     * - 不裁剪任何内容
     * - 但可能导致视频变形（失真）
     * - 一般不推荐使用，除非有特殊需求
     *
     * 适用场景：
     * - 某些特殊视觉效果需求
     * - 视频与容器比例相近时影响较小
     */
    STRETCH
}