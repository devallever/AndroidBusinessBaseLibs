package app.allever.android.lib.media.picker

import android.os.Parcel
import android.os.Parcelable
import app.allever.android.lib.media.core.model.MediaType

/**
 * 媒体选择器配置
 *
 * @property types 支持的媒体类型集合（决定 Tab 数量）
 * @property maxSelect 最大选择数量，默认 9
 * @property showPreview 是否显示预览功能（点击进入预览页）
 */
data class MediaPickerConfig(
    val types: Set<MediaType.Type> = setOf(MediaType.Type.IMAGE, MediaType.Type.VIDEO, MediaType.Type.AUDIO),
    val maxSelect: Int = 9,
    val showPreview: Boolean = true,
) : Parcelable {

    /** 是否仅单类型（只有一个 Tab） */
    val isSingleType: Boolean get() = types.size == 1

    /** 是否包含图片 */
    val hasImage: Boolean get() = types.contains(MediaType.Type.IMAGE)

    /** 是否包含视频 */
    val hasVideo: Boolean get() = types.contains(MediaType.Type.VIDEO)

    /** 是否包含音频 */
    val hasAudio: Boolean get() = types.contains(MediaType.Type.AUDIO)

    constructor(parcel: Parcel) : this(
        readTypes(parcel),
        parcel.readInt(),
        parcel.readByte().toInt() != 0,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val ordinals = types.map { it.ordinal }.toIntArray()
        parcel.writeInt(ordinals.size)
        parcel.writeIntArray(ordinals)
        parcel.writeInt(maxSelect)
        parcel.writeByte((if (showPreview) 1 else 0).toByte())
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MediaPickerConfig> {
        override fun createFromParcel(parcel: Parcel) = MediaPickerConfig(parcel)
        override fun newArray(size: Int) = arrayOfNulls<MediaPickerConfig>(size)

        const val KEY_CONFIG = "media_picker_config"
        const val KEY_RESULT = "media_picker_result"

        private fun readTypes(parcel: Parcel): Set<MediaType.Type> {
            val size = parcel.readInt()
            val ordinals = IntArray(size)
            parcel.readIntArray(ordinals)
            val result = mutableSetOf<MediaType.Type>()
            for (ordinal in ordinals) {
                MediaType.Type.values().getOrNull(ordinal)?.let { result.add(it) }
            }
            return result
        }
    }
}
