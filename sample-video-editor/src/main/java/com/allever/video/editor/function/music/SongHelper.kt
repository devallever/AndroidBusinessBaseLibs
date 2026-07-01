package com.allever.video.editor.function.music

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import com.android.absbase.App
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.FileUtils
import com.allever.video.editor.R
import com.allever.video.editor.function.DataManager
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class SongInfo() : Parcelable {
    var id: Long = -1 //id标识
    var title: String = "" // 显示名称
    var fileName: String = "" // 文件名称
    var path: String = "" // 音乐文件的路径
    var duration: Long = 0 // 媒体播放总时间
    var durationStr: String = ""
    var albums: String = "" // 专辑
    var artist: String = "" // 艺术家
    var size: Long = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString() ?: ""
        fileName = parcel.readString() ?: ""
        path = parcel.readString() ?: ""
        duration = parcel.readLong()
        durationStr = parcel.readString() ?: ""
        albums = parcel.readString() ?: ""
        artist = parcel.readString() ?: ""
        size = parcel.readLong()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest?.writeLong(id)
        dest?.writeString(title)
        dest?.writeString(fileName)
        dest?.writeString(path)
        dest?.writeLong(duration)
        dest?.writeString(durationStr)
        dest?.writeString(albums)
        dest?.writeString(artist)
        dest?.writeLong(size)
    }

    fun clone(): SongInfo {
        val songInfo = SongInfo()
        songInfo.id = id
        songInfo.title = title
        songInfo.fileName = fileName
        songInfo.path = path
        songInfo.duration = duration
        songInfo.durationStr = durationStr
        songInfo.albums = albums
        songInfo.artist = artist
        songInfo.size = size
        return songInfo
    }

    override fun toString(): String {
        return "id: $id, title: $title, fileName: $fileName, " +
                "path: $path, duration: $duration, durationStr: $durationStr, " +
                "albums: $albums, artist: $artist, size: $size"
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SongInfo> {
        override fun createFromParcel(parcel: Parcel): SongInfo? {
            return try {
                SongInfo(parcel)
            } catch (e: Exception) {
                null
            }
        }

        override fun newArray(size: Int): Array<SongInfo?> {
            return arrayOfNulls(size)
        }

        fun createFromPath(path: String): SongInfo? {
            val duration = SongHelper.getDuration(path)
            if (duration == 0.toLong()) {
                //过滤损坏的音频文件或时长为0的音频
                return null
            }

            val mediaMetadataRetriever = MediaMetadataRetriever()
            var albums = ""
            var artist = ""
            var title: String? = null
            try {
                //读取到格式损坏的音频会崩掉
                mediaMetadataRetriever.setDataSource(path)
                title = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                albums = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
                artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                mediaMetadataRetriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
                mediaMetadataRetriever.release()
                return null
            }

            val file = File(path)
            val filename = file.name
            val name = filename.split(".")[0]
            val songInfo = SongInfo()
            songInfo.id = -1 //id标识
            songInfo.title = title ?: name.capitalize()// 显示名称
            songInfo.fileName = filename // 文件名称
            songInfo.path = path // 音乐文件的路径
            songInfo.duration = duration// 媒体播放总时间
            songInfo.durationStr = SongHelper.formatTime(songInfo.duration)
            songInfo.albums = albums
            songInfo.artist = artist
            songInfo.size = file.length()
            return songInfo
        }

        /**
         * get the media file info by path
         * @param filePath
         * @return
         */
        fun createFromPath(context: Context, filePath: String): SongInfo? {
            var cursor: Cursor? = null
            try {
                var path = filePath
                /* check a exit file */
                val file = File(path)
                if (!file.exists()) {
                    return null
                }
                /* create the query URI, where, selectionArgs */
                var uri: Uri? = null
                var where: String? = null
                var selectionArgs: Array<String>?
                if (path.startsWith("content://media/")) {
                    /* content type path */
                    uri = Uri.parse(path)
                    where = null
                    selectionArgs = null
                } else {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    where = MediaStore.MediaColumns.DATA + "=?"
                    selectionArgs = arrayOf(path)
                }
                /* query */
                cursor = context.contentResolver.query(uri, null, where, selectionArgs, null)
                return if (cursor == null || cursor.count == 0) {
                    null
                } else {
                    cursor.moveToFirst()
                    getInfoFromCursor(cursor)
                }
            } catch (e: Exception) {
                return null
            } finally {
                cursor?.close()
            }
        }

        /**
         * get the media info beans from cursor
         * @param cursor
         * @return
         */
        @SuppressLint("Range")
        private fun getInfoFromCursor(cursor: Cursor): SongInfo {
            val mediaEntity = SongInfo()
            val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))//音乐id
            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))//音乐标题
            val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
            val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))//艺术家
            val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))//专辑
//                    val albumid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))//专辑id
            val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))//时长
            val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))//文件大小
            val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))//文件路径
            val isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))//是否为音乐

//            if (!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
//                continue
//            }
            mediaEntity.id = id
            mediaEntity.title = title
            mediaEntity.path = url
            mediaEntity.size = size
            mediaEntity.fileName = displayName
            mediaEntity.duration = duration
            mediaEntity.albums = album
            mediaEntity.durationStr = SongHelper.formatTime(mediaEntity.duration)
            mediaEntity.artist = artist
            mediaEntity.path = url
            return mediaEntity
        }
    }
}


object SongHelper {
    interface OnSongSearchListener {
        /**
         * 开始搜索
         */
        fun searchStart()

        /**
         * 扫描过程中
         */
        fun progress(path: String)

        /**
         * 扫描到一个
         */
        fun search(songInfo: SongInfo)

        /**
         * 扫描结束
         */
        fun searchEnd(songInfos: List<SongInfo>)
    }

    private val SONG_SAVE_PATH: String
    //先不处理.ogg, .wav
    private val SONG_SUFFIXS = arrayOf(".mp3", ".aac")//, ".ogg", ".wav"
    private val songFilter = FileFilter { file ->
        if (file.isDirectory) {
            true
        } else {
            var isSong = false
            for (suffix in SONG_SUFFIXS) {
                isSong = file.absolutePath.endsWith(suffix, ignoreCase = true)
                if (isSong) {
                    break
                }
            }
            isSong
        }
    }
    private var stopSearching = true
    private var songInfos = LinkedList<SongInfo>()
    private var songInfosMaps = ConcurrentHashMap<String, SongInfo>()

    private fun searchDir(outSongInfo: LinkedList<SongInfo>, dir: File, listener: OnSongSearchListener? = null) {
        if (stopSearching) {
            return
        }
        val files = dir.listFiles(songFilter) ?: return
        for (file in files) {
            if (stopSearching) {
                break
            }
            val absolutePath = file.absolutePath
            listener?.progress(absolutePath)
            if (file.isDirectory) {
                //过滤掉内置音乐和下载的音乐，会缓存到这个目录
                if (file.absolutePath == DataManager.EXTERNAL_CACHE_DIR) {
                    continue
                }
                searchDir(outSongInfo, file, listener)
            } else {
                val songInfo = SongInfo.createFromPath(absolutePath) ?: continue
                if (!songInfosMaps.containsKey(songInfo.path)) {
                    songInfosMaps[songInfo.path] = songInfo
                    outSongInfo.add(songInfo)
                    listener?.search(songInfo)
                }
            }
        }
    }

    init {
        val MEDIA_TEMP_FOLDER_PATH = FileUtils.getExternalCacheDir(App.getContext(), "song")
        SONG_SAVE_PATH = File(MEDIA_TEMP_FOLDER_PATH, "songinfo").absolutePath
    }

    private fun read(listener: OnSongSearchListener? = null): List<SongInfo> {
        var bos: BufferedInputStream? = null
        val songInfos = LinkedList<SongInfo>()
        try {
            bos = BufferedInputStream(FileInputStream(SONG_SAVE_PATH))
            val bytes = ByteArray(bos.available())
            bos.read(bytes)
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val outP = parcel.readParcelableArray(SongInfo::class.java.classLoader)
            outP?.map {
                if (it is SongInfo) {
                    val file = File(it.path)
                    if (file.exists()) {
                        songInfos.add(it)
                        listener?.search(it)
                    }
                }
            }
            parcel.recycle()
        } catch (e: Exception) {
            DLog.printStackTrace(e)
        } finally {
            bos?.close()
        }
        return songInfos
    }

    private fun save(songInfos: List<SongInfo>) {

        var bos: BufferedOutputStream? = null
        try {
            bos = BufferedOutputStream(FileOutputStream(SONG_SAVE_PATH))
            val parcel = Parcel.obtain()
            parcel.writeParcelableArray(songInfos.toTypedArray(), 0)
            bos.write(parcel.marshall())
            parcel.recycle()
            bos.flush()
        } catch (e: Exception) {

        } finally {
            bos?.close()
        }
    }

    fun startGlobalSearch(rescan: Boolean, listener: OnSongSearchListener? = null) {
        stopSearching = false
        val sis = if (!rescan) {
            read(listener)
        } else {
            listener?.searchStart()
            val externalStorageDirectory = Environment.getExternalStorageDirectory()
            val outSongInfo = LinkedList<SongInfo>()
            searchDir(outSongInfo, externalStorageDirectory, listener)
            save(outSongInfo)
            outSongInfo
        }
        sis.map {
            if (!this@SongHelper.songInfosMaps.containsKey(it.path)) {
                this@SongHelper.songInfosMaps[it.path] = it
                this@SongHelper.songInfos.add(it)
            }
        }
        listener?.searchEnd(this@SongHelper.songInfos)
    }

    fun startGlobalSearchAsync(rescan: Boolean, listener: OnSongSearchListener? = null) {
        val thread = Thread {
            startGlobalSearch(rescan, listener)
        }.start()
    }

    fun stopGlobalSearchAsync() {
        stopSearching = true
    }

    @SuppressLint("Range")
    fun getAllSongInfo(
        context: Context,
        rescan: Boolean = false,
        listener: OnSongSearchListener? = null
    ): List<SongInfo> {
        songInfosMaps.clear()
        val systemSongInfos = ArrayList<SongInfo>()
        var cursor: Cursor? = null
        try {
//            val selection = MediaStore.Audio.Media.DURATION + ">=30000"
            val selection = null
            cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                selection, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val mediaEntity = SongInfo()
                    val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))//音乐id
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))//音乐标题
                    val displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                    val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))//艺术家
                    val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))//专辑
//                    val albumid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))//专辑id
                    val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))//时长
                    val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))//文件大小
                    val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))//文件路径
                    val isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC))//是否为音乐

//                    if (!checkIsMusic(mediaEntity.duration, mediaEntity.size)) {
//                        continue
//                    }
                    //过滤损坏的音频文件或时长为0的音频
                    if (duration <= 0) {
                        continue
                    }

                    mediaEntity.id = id
                    mediaEntity.title = title
                    mediaEntity.path = url
                    mediaEntity.size = size
                    mediaEntity.fileName = displayName
                    mediaEntity.duration = duration
                    mediaEntity.albums = album
                    mediaEntity.durationStr = formatTime(mediaEntity.duration)
                    mediaEntity.artist = artist
                    mediaEntity.path = url
                    var isSong = false
                    for (suffix in SONG_SUFFIXS) {
                        isSong = displayName.endsWith(suffix, ignoreCase = true)
                        if (isSong) {
                            break
                        }
                    }
                    if (!songInfosMaps.containsKey(url) && isSong) {
                        songInfosMaps[url] = mediaEntity
                        systemSongInfos.add(mediaEntity)
                    }
                }
            }
        } catch (e: Exception) {

        } finally {
            cursor?.close()
        }

        startGlobalSearchAsync(rescan, listener)

        return systemSongInfos
    }

    /**
     * 格式化时间,将毫秒转换为分:秒格式
     */
    fun formatTime(time: Long): String {
        var min = (time / (1000 * 60)).toString() + ""
        var sec = (time % (1000 * 60)).toString() + ""
        if (min.length < 2) {
            min = "0" + time / (1000 * 60) + ""
        } else {
            min = (time / (1000 * 60)).toString() + ""
        }
        if (sec.length == 4) {
            sec = "0" + time % (1000 * 60) + ""
        } else if (sec.length == 3) {
            sec = "00" + time % (1000 * 60) + ""
        } else if (sec.length == 2) {
            sec = "000" + time % (1000 * 60) + ""
        } else if (sec.length == 1) {
            sec = "0000" + time % (1000 * 60) + ""
        }
        return min + ":" + sec.trim { it <= ' ' }.substring(0, 2)
    }


    /**
     * 获取默认专辑图片
     */
    fun getDefaultArtwork(context: Context, small: Boolean): Bitmap {
        val opts = BitmapFactory.Options()
        opts.inPreferredConfig = Bitmap.Config.RGB_565
        //        if (small) {  //返回小图片
        //            return BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_edit_music, opts);
        ////            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.ic_launcher), null, opts);
        //        }
        //        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.ic_launcher), null, opts);

        return BitmapFactory.decodeResource(context.resources, R.drawable.icon_edit_music, opts)
    }

    // 获取专辑封面的Uri
    private val albumArtUri = Uri.parse("content://media/external/audio/albumart")

    /**
     * 从文件当中获取专辑封面位图
     */
    private fun getArtworkFromFile(context: Context, songid: Long, albumid: Long): Bitmap? {
        var bm: Bitmap? = null
        if (albumid < 0 && songid < 0) {
            throw IllegalArgumentException("Must specify an album or a song id")
        }
        try {
            val options = BitmapFactory.Options()
            var fd: FileDescriptor? = null
            if (albumid < 0) {
                val uri = Uri.parse(
                    "content://media/external/audio/media/"
                            + songid + "/albumart"
                )
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    fd = pfd.fileDescriptor
                }
            } else {
                val uri = ContentUris.withAppendedId(albumArtUri, albumid)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                if (pfd != null) {
                    fd = pfd.fileDescriptor
                }
            }
            options.inSampleSize = 1
            // 只进行大小判断
            options.inJustDecodeBounds = true
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options)
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false
            options.inDither = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return bm
    }

    /**
     * 获取专辑封面位图对象
     */
    fun getArtwork(
        context: Context, song_id: Long, album_id: Long,
        allowdefalut: Boolean, small: Boolean
    ): Bitmap? {
        if (album_id < 0) {
            if (song_id < 0) {
                val bm = getArtworkFromFile(context, song_id, -1)
                if (bm != null) {
                    return bm
                }
            }
            return if (allowdefalut) {
                getDefaultArtwork(context, small)
            } else null
        }
        val res = context.contentResolver
        val uri = ContentUris.withAppendedId(albumArtUri, album_id)
        if (uri != null) {
            var `in`: InputStream? = null
            try {
                `in` = res.openInputStream(uri)
                val options = BitmapFactory.Options()
                //先制定原始大小
                options.inSampleSize = 1
                //只进行大小判断
                options.inJustDecodeBounds = true
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(`in`, null, options)
                /** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例  */
                /** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合  */
                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40)
                } else {
                    options.inSampleSize = computeSampleSize(options, 600)
                }
                // 我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false
                options.inDither = false
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                `in` = res.openInputStream(uri)
                return BitmapFactory.decodeStream(`in`, null, options)
            } catch (e: FileNotFoundException) {
                var bm = getArtworkFromFile(context, song_id, album_id)
                if (bm != null) {
                    if (bm.config == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false)
                        if (bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small)
                        }
                    }
                } else if (allowdefalut) {
                    bm = getDefaultArtwork(context, small)
                }
                return bm
            } finally {
                try {
                    `in`?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return null
    }

    /**
     * 对图片进行合适的缩放
     */
    fun computeSampleSize(options: BitmapFactory.Options, target: Int): Int {
        val w = options.outWidth
        val h = options.outHeight
        val candidateW = w / target
        val candidateH = h / target
        var candidate = Math.max(candidateW, candidateH)
        if (candidate == 0) {
            return 1
        }
        if (candidate > 1) {
            if (w > target && w / candidate < target) {
                candidate -= 1
            }
        }
        if (candidate > 1) {
            if (h > target && h / candidate < target) {
                candidate -= 1
            }
        }
        return candidate
    }


    @JvmStatic
    fun getDuration(path: String): Long {
        var mediaPlayer: MediaPlayer? = null
        var duration = 0
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            duration = mediaPlayer.duration
        } catch (e: Exception) {
        } finally {
            try {
                mediaPlayer?.release()
            } catch (e: Exception) {
            }
        }
        return duration.toLong()
    }

}