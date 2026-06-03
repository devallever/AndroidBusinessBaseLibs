package app.allever.android.lib.media.core.model

/**
 * MediaStore 列名常量集中管理
 * 避免硬编码散落各处
 */
object MediaStoreColumn {
    // 公共列
    const val ID = "_ID"
    const val MEDIA_TYPE = "media_type"
    const val DATA = "_data"
    const val DATE_ADDED = "date_added"
    const val DATE_MODIFIED = "date_modified"
    const val DATE_TAKEN = "datetaken"
    const val SIZE = "_size"
    const val MIME_TYPE = "mime_type"
    const val DISPLAY_NAME = "_display_name"
    const val BUCKET_ID = "bucket_id"
    const val BUCKET_DISPLAY_NAME = "bucket_display_name"

    // 图片独有列
    const val WIDTH = "width"
    const val HEIGHT = "height"
    const val ORIENTATION = "orientation"

    // 视频独有列
    const val DURATION = "duration"

    // 音频独有列
    const val TITLE = "title"
    const val ARTIST = "artist"
    const val ALBUM = "album"
    const val ALBUM_ID = "album_id"
}
