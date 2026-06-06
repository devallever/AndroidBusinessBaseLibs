package app.allever.android.lib.media.core.model

import android.net.Uri
import android.os.Parcelable
import android.os.Parcel

/**
 * 媒体资源基类，使用 sealed class 保证类型安全
 * 每种媒体类型有独立的属性字段，避免冗余字段和 int 类型判断
 */
sealed class MediaItem : Parcelable {
    abstract val id: Long
    abstract val uri: Uri
    abstract val path: String          // 文件绝对路径（Android 10+ 分区存储下可能为空字符串）
    abstract val name: String          // 文件名
    abstract val dateAdded: Long       // 添加时间戳（秒）
    abstract val size: Long            // 文件大小（字节）
    abstract val mimeType: String      // MIME 类型

    /**
     * 图片资源
     */
    data class Image(
        override val id: Long,
        override val uri: Uri,
        override val path: String,
        override val name: String,
        override val dateAdded: Long,
        override val size: Long,
        override val mimeType: String,
        val width: Int,                // 图片宽度（像素）
        val height: Int,               // 图片高度（像素）
        val orientation: Int,          // 旋转角度（EXIF）
    ) : MediaItem() {
        constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            uri = parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
            path = parcel.readString() ?: "",
            name = parcel.readString() ?: "",
            dateAdded = parcel.readLong(),
            size = parcel.readLong(),
            mimeType = parcel.readString() ?: "",
            width = parcel.readInt(),
            height = parcel.readInt(),
            orientation = parcel.readInt(),
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            // 先写入类型标签（基类负责）
            parcel.writeString(typeLabel())
            // 再写入自身字段
            parcel.apply {
                writeLong(id)
                writeParcelable(uri, flags)
                writeString(path)
                writeString(name)
                writeLong(dateAdded)
                writeLong(size)
                writeString(mimeType)
                writeInt(width)
                writeInt(height)
                writeInt(orientation)
            }
        }

        override fun describeContents(): Int = 0
    }

    /**
     * 视频资源
     */
    data class Video(
        override val id: Long,
        override val uri: Uri,
        override val path: String,
        override val name: String,
        override val dateAdded: Long,
        override val size: Long,
        override val mimeType: String,
        val duration: Long,            // 时长（毫秒）
        val width: Int,                // 视频宽度
        val height: Int,               // 视频高度
    ) : MediaItem() {


        companion object {
            fun newDefault( path: String, name: String): Video {
                return Video(
                    id = 0,
                    uri = Uri.EMPTY,
                    path = path,
                    name = name,
                    dateAdded = 0,
                    size = 0,
                    mimeType = "",
                    duration = 0,
                    width = 0,
                    height = 0,
                )
            }
        }
        constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            uri = parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
            path = parcel.readString() ?: "",
            name = parcel.readString() ?: "",
            dateAdded = parcel.readLong(),
            size = parcel.readLong(),
            mimeType = parcel.readString() ?: "",
            duration = parcel.readLong(),
            width = parcel.readInt(),
            height = parcel.readInt(),
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(typeLabel())
            parcel.apply {
                writeLong(id)
                writeParcelable(uri, flags)
                writeString(path)
                writeString(name)
                writeLong(dateAdded)
                writeLong(size)
                writeString(mimeType)
                writeLong(duration)
                writeInt(width)
                writeInt(height)
            }
        }

        override fun describeContents(): Int = 0
    }

    /**
     * 音频资源
     */
    data class Audio(
        override val id: Long,
        override val uri: Uri,
        override val path: String,
        override val name: String,
        override val dateAdded: Long,
        override val size: Long,
        override val mimeType: String,
        val duration: Long,            // 时长（毫秒）
        val title: String,             // 音频标题
        val artist: String,            // 艺术家
        val album: String,             // 专辑名
        val albumId: Long,             // 专辑 ID（用于获取封面）
    ) : MediaItem() {
        constructor(parcel: Parcel) : this(
            id = parcel.readLong(),
            uri = parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
            path = parcel.readString() ?: "",
            name = parcel.readString() ?: "",
            dateAdded = parcel.readLong(),
            size = parcel.readLong(),
            mimeType = parcel.readString() ?: "",
            duration = parcel.readLong(),
            title = parcel.readString() ?: "",
            artist = parcel.readString() ?: "",
            album = parcel.readString() ?: "",
            albumId = parcel.readLong(),
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(typeLabel())
            parcel.apply {
                writeLong(id)
                writeParcelable(uri, flags)
                writeString(path)
                writeString(name)
                writeLong(dateAdded)
                writeLong(size)
                writeString(mimeType)
                writeLong(duration)
                writeString(title)
                writeString(artist)
                writeString(album)
                writeLong(albumId)
            }
        }

        override fun describeContents(): Int = 0
    }

    companion object CREATOR : Parcelable.Creator<MediaItem> {
        override fun createFromParcel(parcel: Parcel): MediaItem {
            // 读取类型标记以区分子类
            val typeName = parcel.readString()
            return when (typeName) {
                "Image" -> Image(parcel)
                "Video" -> Video(parcel)
                "Audio" -> Audio(parcel)
                else -> throw IllegalArgumentException("Unknown MediaItem type: $typeName")
            }
        }

        override fun newArray(size: Int): Array<MediaItem?> = arrayOfNulls(size)
    }

    /**
     * 写入时先写入类型名称用于反序列化区分
     * 各子类在 writeToParcel 开头调用此方法
     */
    protected fun typeLabel(): String = when (this) {
        is Image -> "Image"
        is Video -> "Video"
        is Audio -> "Audio"
    }
}
