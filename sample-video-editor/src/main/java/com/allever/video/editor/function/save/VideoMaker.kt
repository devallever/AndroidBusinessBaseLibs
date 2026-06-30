package com.allever.video.editor.function.save

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.text.TextUtils

import com.android.absbase.App
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.FileUtils
import com.android.absbase.utils.TimeUtils
import com.android.absbase.utils.ZipUtils
import com.android.absbase.utils.thread.ThreadPool
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.function.editor.bean.EffectListBean
import com.allever.video.editor.function.editor.bean.SoundBean
import com.allever.video.editor.function.editor.bean.VideoBean
import com.allever.video.editor.function.media.BitmapUtil
import com.allever.video.editor.function.save.grafika.MoviePlayer
import com.allever.video.editor.ui.dialog.SaveDialog
import com.allever.video.editor.ui.widget.gesture.IContentView
import com.allever.video.editor.utils.FileUtil
import com.allever.video.editor.utils.MediaTypeUtil

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

import java.util.*
import kotlin.math.PI

/**
 */

object VideoMaker {
    private val TAG = VideoMaker::class.java.getSimpleName()
    // https://ffmpeg.org/download.html、
    private val FFMPEG_FILE_NAME = "ffmpeg"
    private var sFfmpegFile: String? = null

    private var sTmpDirPath: String = ""
    private val AUDIO_FILE_SUFFIX = ".aac"
    private val VIDEO_FILE_SUFFIX = ".mp4"
    private val IMG_FILE_SUFFIX = ".jpg"
    private val TEXT_FILE_SUFFIX = ".txt"
    private val PNG_FILE_SUFFIX = ".png"

    private var executing = true
    @JvmStatic
    fun init(context: Context) {
        // 清除上次缓存文件
        val videoCacheDirName = ".video"
        var tmpDir: String? = null
        var dir: String? = null
        try {
            dir = FileUtils.getExternalCacheDir(context, videoCacheDirName, true)
            if (dir != null) {
                FileUtils.delete(File(dir), true)
            }
        } catch (e: Exception) {
        }
        if (tmpDir == null && dir != null) {
            tmpDir = dir
        }
        try {
            dir = FileUtils.getCacheDir(context, videoCacheDirName, true)
            if (dir != null) {
                FileUtils.delete(File(dir), true)
            }
        } catch (e: Exception) {
        }
        if (tmpDir == null && dir != null) {
            tmpDir = dir
        }
        try {
            dir = File(context.cacheDir, videoCacheDirName).absolutePath
            if (dir != null) {
                FileUtils.delete(File(dir), true)
            }
        } catch (e: Exception) {
        }
        if (tmpDir == null && dir != null) {
            tmpDir = dir
        }
        sTmpDirPath = tmpDir!!
        checkFileInit()
        return
    }

    private fun checkFileInit() {
        if (!Build.CPU_ABI.contains("arm")) {
            statisticsFfmpeg("no_arm")
            return
        }
        var fileExist = false
        var fileCanExecute = false
        val context = App.getContext()
        val dirPaths = arrayOf(File(context.filesDir.absoluteFile, ".$FFMPEG_FILE_NAME").absolutePath,
                FileUtils.getExternalCacheDir(context, ".$FFMPEG_FILE_NAME", true))
        for (path in dirPaths) {
            if (path.isNullOrEmpty()) {
                continue
            }
            val dirfp = File(path)
            if (!dirfp.exists()) {
                try {
                    dirfp.mkdirs()
                } catch (e: Exception) {

                }
            }
            val output = File(dirfp, FFMPEG_FILE_NAME)
            sFfmpegFile = output.absolutePath
            fileExist = output.exists()
            fileCanExecute = output.canExecute()
            if (fileExist && fileCanExecute) {
                break
            }
            var `is`: InputStream? = null
            try {
                `is` = context.assets.open(FFMPEG_FILE_NAME)
                val files = ArrayList<String>()
                files.add(FFMPEG_FILE_NAME)
                val filepaths = ZipUtils.unzipFile(`is`, path!!, files)
            } catch (e: IOException) {
                DLog.printStackTrace(e)
                statisticsFfmpeg("checkerr:" + e.message)
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            fileExist = output.exists()
            if (fileExist) {
                fileCanExecute = output.canExecute()
                if (!fileCanExecute) {
                    fileCanExecute = output.setExecutable(true)
                    if (fileCanExecute) {
                        break
                    }
                }
            }
        }
        var msg = "check_${fileExist}_${fileCanExecute}"
//        statisticsFfmpeg(msg)
    }

    /**
     * 图片生成视频
     */
    fun image2Video(effectBean: EffectBean, view: IContentView, dstFile: String?, videoWidth: Int, videoHeight: Int){
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (dstFile == null) {
            return
        }
        createBuilder()
                .add("-loop")
                .add("1")
                .add("-t")
                .add("${effectBean.dstDuration / TimeUtils.TimeConstant.ONE_SEC.toFloat()}")
                .add("-i")
                .add(effectBean.path ?:"")
                .add("-vcodec")
                .add("libx264")
                .add("-y")
                .add(dstFile)
                .run(executing)
    }

    /**
     * 先创建一个基础视频，主副特效都叠在上面
     * @param primaryBeans 主特效
     * @param secondaryBeans 副特效
     * @param dstFile 视频存储地址
     * @param quality 视频质量
     * ./ffmpeg -loop 1 -t 8.721 -i tmp2181314181846199115.tmp -itsoffset 0.000 -loop 1 -t 3.000 -i /storage/emulated/0/t.jpeg -itsoffset 3.000 -i /storage/emulated/0/DCIM/Camera/VID_20181212_172621.mp4 -itsoffset 5.400 -ss 2 -accurate_seek -i /storage/emulated/0/DCIM/Camera/VID_20181212_171919.mp4 -filter_complex "[0:v][1:v]overlay='if(gte(t,0.000),0,NAN)':'if(lt(t,3.000),0,NAN)'[out1];[2:v]scale=439.3125:781.0[S2];[S2]rotate='0.0:ow=439.3125:oh=781.0:c=none'[inner2];[out1][inner2]overlay='if(gte(t,3.000),49.84375,NAN)':'if(lt(t,5.400),0.0,NAN)'[out2];[2:a]volume=1,adelay=3000|3000[outAudio2];[3:v]scale=439.3125:781.0[S3];[S3]rotate='0.0:ow=439.3125:oh=781.0:c=none'[inner3];[out2][inner3]overlay='if(gte(t,5.400),49.84375,NAN)':'if(lt(t,8.721),0.0,NAN)'[out3];[3:a]volume=1,adelay=5400|5400[outAudio3];[outAudio2][outAudio3]amix=inputs=2:duration=0[outAudioFinal]" -map [out3] -map [outAudioFinal] -preset ultrafast -y /storage/emulated/0/output2.mp4
     */
    fun mergeVideo2(primaryBeans: EffectListBean, secondaryBeans: EffectListBean, view: IContentView, dstFile: String?, quality: SaveDialog.Quality):CommandHelper.Result?{
        executing = true
        val videoRect = view.videoRect
        var totalWidth = videoRect.width()
        var totalHeight = videoRect.height()
        //奇数有问题  height not divisible by 2 (1080x781)Error initializing output stream 0:0
        if(totalWidth % 2 != 0){
            totalWidth+=1
        }
        //奇数有问题  height not divisible by 2 (1080x781)Error initializing output stream 0:0
        if(totalHeight % 2 != 0){
            totalHeight+=1
        }
        if (sFfmpegFile.isNullOrEmpty()) {
            return CommandHelper.Result()
        }
        if (dstFile == null) {
            return  CommandHelper.Result()
        }
        //保存临时文件路径 用于删除
        val tempFileArrays = arrayListOf<String>()
        //创建一个基础视频
        val baseFile = createTempFilePath()
        tempFileArrays.add(baseFile)
        //背景底色 黑色
        saveBackgroundImage(Color.BLACK,totalWidth,totalHeight,baseFile)
        val commandBuilder = createBuilder()
        val totalDuration = primaryBeans.totalDuration
        commandBuilder
                .add("-loop")
                .add("1")
                .add("-t")
                .add(buildTimeString(totalDuration.toInt()))
                .add("-i")
                .add(baseFile)
        val inParams = LinkedList<String>()
        val effectParams = LinkedList<String>()
        var videoAliasPosition = 0
        var prevVideoAliasIndex = 0
        var position = 1
        val tempAlias = "[outVideoFinal]"
        var outVideoFinalAlias = ""

        val audioInParams = LinkedList<String>()
        val audioAliasParams = LinkedList<String>()
        val audioParams = LinkedList<String>()
        val outAudioFinalAlias = "[outAudioFinal]"
        var audioPosition = 0

        // 去除无效主特效
        val primaryList = primaryBeans.beans.filter {
            it.state != EffectBean.STATE_DELETE && it.videoTime.dstStartTime <  totalDuration  && it.videoTime.dstEndTime > 0
        } as MutableList<EffectBean>
//        去除无效副特效
        val secondaryList = secondaryBeans.beans.filter {
            it.state != EffectBean.STATE_DELETE && it.videoTime.dstStartTime <  totalDuration  && it.videoTime.dstEndTime > 0
        }
        //添加副特效到主特效后面，先处理主再处理副
        primaryList.addAll(secondaryList)
        primaryList.mapIndexed { index, effectBean ->
            if (effectBean.state == EffectBean.STATE_DELETE) return@mapIndexed
            //合并多个文件，先统一大小
            val dstStartTime = buildTimeString(effectBean.videoTime.dstStartTime.toInt())
            val dstEndTime = buildTimeString(effectBean.videoTime.dstEndTime.toInt())
            val srcStartTime = buildTimeString(effectBean.videoTime.srcStartTime.toInt())
            val dstDurationTime = buildTimeString(effectBean.dstDuration.toInt())
            val srcDuration = buildTimeString(effectBean.srcDuration.toInt())
            val originalPath = effectBean.path ?: ""
            when (effectBean.type) {
                MediaTypeUtil.TYPE_VIDEO -> {
                    prevVideoAliasIndex = videoAliasPosition
                    videoAliasPosition++
                    val effectView = view.getEffectView(effectBean)
                    if (effectBean.state == EffectBean.STATE_VALID && effectView != null) {
                        val rectF = view.getEffectViewRect(effectBean)
                        val videoWidth = rectF.width()
                        val videoHeight = rectF.height()
                        //角度转弧度
                        val angel = effectView.rotation
                        val dscDegrees = Math.toRadians(angel.toDouble())
                        //视频偏移 不能和setpts=PTS-STARTPTS 混用，具体偏移位置之前必须有持续输入流填充，不然偏移位置不正确
                        inParams.add("-itsoffset")
                        inParams.add(dstStartTime)
                        var videoPath:String? = null
                        if(effectBean.duration != effectBean.srcDuration){
                            //需要裁剪
//                            inParams.add("-ss")
//                            inParams.add(srcStartTime)
//                            inParams.add("-t")          //裁剪偏移 -t 音频有问题 ，单独裁剪处理
//                            inParams.add(srcDuration)
//                            inParams.add("-accurate_seek")//剪切时间更加精确
                            //裁剪  ffmpeg -i test.mp4 -ss 0 -t 3 -vcodec copy -acodec copy  ss_test.mp4
                            val cropVideoPath = createTempFilePath(suffix = VIDEO_FILE_SUFFIX)
                            tempFileArrays.add(cropVideoPath)
                            createBuilder()
                                    .add("-ss")
                                    .add(srcStartTime)
                                    .add("-t")
                                    .add(srcDuration)
                                    .add("-accurate_seek")
                                    .add("-i")
                                    .add(originalPath)
                                    .add("-vcodec")
                                    .add("copy")
                                    .add("-acodec")
                                    .add("copy")
                                    .add(cropVideoPath)
                                    .run(executing)
                            inParams.add("-i")
                            inParams.add(cropVideoPath)
                            videoPath = cropVideoPath
                        }else{
                            inParams.add("-i")
                            inParams.add(originalPath)
                            videoPath = originalPath
                        }
                            //循环视频流合成实际播放视频
//                            if(effectBean.srcDuration < effectBean.dstDuration){
//                                // ffmpeg -filter_complex "movie=ss_test.mp4:loop=999,setpts=N/(FRAME_RATE*TB)[out]" -map [out] -t 15 -y offset.mp4
//                                val loopVideoPath = createTempFilePath(suffix = VIDEO_FILE_SUFFIX)
//                                tempFileArrays.add(loopVideoPath)
//                                //由于循环视频合成会自动转角度，所以调整角度
//                                val transpose = when(effectBean.angle){
//                                    90f ->{
//                                        ",transpose=1"//顺转90度
//                                    }
//                                    180f ->{
//                                        ",transpose=2,transpose=2" //逆转180度
//                                    }
//                                    270f ->{
//                                        ",transpose=2" //逆转90度
//                                    }
//                                    else -> ""
//                                }
//                                createBuilder()
//                                        .add("-filter_complex")
//                                        .add("movie=$cropVideoPath:loop=0,setpts=N/(FRAME_RATE*TB)$transpose[out]")
//                                        .add("-map")
//                                        .add("[out]")
//                                        .add("-t")
//                                        .add(buildTimeString(effectBean.dstDuration.toInt()))
//                                        .add("-c:v")
//                                        .add("libx264")
//                                        .add("-preset")
//                                        .add("ultrafast")
//                                        .add(loopVideoPath)
//                                        .run()
//                                inParams.add(loopVideoPath)
//                            }else{
//                                //不需要循环输入流
//                                inParams.add(cropVideoPath)
//                            }
                        //旋转后新的宽度高度
                        val pointF = calculateRotateWidthAndHeight(videoWidth, videoHeight, angel.toDouble())
                        val newWidth = pointF.x
                        val newHeight = pointF.y
                        val translateX = rectF.centerX() - (newWidth) / 2
                        val translateY = rectF.centerY() - (newHeight) / 2
                        //缩放旋转
                        effectParams.add("[$position:v]scale=$videoWidth:$videoHeight[S$videoAliasPosition];[S$videoAliasPosition]rotate='$dscDegrees:ow=$newWidth:oh=$newHeight:c=none'[inner$videoAliasPosition];")
                        //视频叠层平移
                        if (prevVideoAliasIndex == 0) {
                            effectParams.add("[0:v][inner$videoAliasPosition]overlay='if(gte(t,$dstStartTime),$translateX,NAN)':'if(lt(t,$dstEndTime),$translateY,NAN)'[out$videoAliasPosition];")
                        } else {
                            effectParams.add("[out$prevVideoAliasIndex][inner$videoAliasPosition]overlay='if(gte(t,$dstStartTime),$translateX,NAN)':'if(lt(t,$dstEndTime),$translateY,NAN)'[out$videoAliasPosition];")
                        }
                        //处理音频
                        if(effectBean is VideoBean){
                            if(effectBean.hasAudio){
                                // -itsoffset 0.001 -i /storage/emulated/0/1080x1920.mp4
                                // -filter_complex "[1:a]volume=1.0,adelay=0001|0001[outAudio2];[outAudio2]amix=inputs=1:duration=0[outAudioFinal]" -map [outAudioFinal] -y /storage/emulated/0/Android/data/video.editor.videoeditor.videomaker/files/cache/.video/tmp4920798994639373234.aac
                                audioInParams.add("-itsoffset")
                                audioInParams.add("$dstStartTime")
                                audioInParams.add("-i")
                                audioInParams.add(videoPath)
                                val audioAlias ="[outAudio$audioPosition]"
                                //偏移音乐
                                //val volume = if (effectBean.volume == 0) { "0.0" } else { "1.0" }
                                //进度[0-255] ，除以128求缩放倍数
                                val volume = effectBean.volume / 128f
                                audioParams.add("[$audioPosition:a]volume=$volume,adelay=${effectBean.videoTime.dstStartTime}|${effectBean.videoTime.dstStartTime}$audioAlias;")
                                audioAliasParams.add(audioAlias)
                                audioPosition++
                            }
                        }
                        outVideoFinalAlias = "[out$videoAliasPosition]"
                        position++
                    }
                }
                MediaTypeUtil.TYPE_AUDIO -> {
                    //副特效音频
                    audioInParams.add("-itsoffset")
                    audioInParams.add(dstStartTime)
                    val offset = effectBean.videoTime.dstEndTime - totalDuration
                    val cropDuration = if(offset > 0){
                        effectBean.dstDuration - offset
                    }else{
                        effectBean.dstDuration
                    }
                    if(cropDuration != effectBean.duration){
//                        inParams.add("-ss")
//                        inParams.add(srcStartTime)
//                        inParams.add("-t")
//                        inParams.add(buildTimeString(cropDuration.toInt()))
//                        inParams.add("-accurate_seek")
                        val cropAudioPath = if (originalPath.isNotEmpty()) {
                            var suffix = "."
                            val list = originalPath.split(".")
                            suffix += list.last()
                            createTempFilePath(suffix = suffix)
                        } else {
                            createTempFilePath()
                        }
                        tempFileArrays.add(cropAudioPath)
                        createBuilder()
                                .add("-ss")
                                .add(srcStartTime)
                                .add("-t")
                                .add(buildTimeString(cropDuration.toInt()))
                                .add("-accurate_seek")
                                .add("-i")
                                .add(originalPath)
                                .add("-acodec")
                                .add("copy")
                                .add(cropAudioPath)
                                .run(executing)
                        audioInParams.add("-i")
                        audioInParams.add(cropAudioPath)
                    }else{
                        audioInParams.add("-i")
                        audioInParams.add(originalPath)
                    }
                    if(effectBean is SoundBean) {
                        val audioAlias ="[outAudio$audioPosition]"
//                        val volume = if (effectBean.volume == 0) { "0.0" } else { "1.0" }
                        val volume = effectBean.volume / 128f
                        audioParams.add("[$audioPosition:a]volume=$volume,adelay=${effectBean.videoTime.dstStartTime}|${effectBean.videoTime.dstStartTime}$audioAlias;")
                        audioAliasParams.add(audioAlias)
                        audioPosition++
                    }
                }
                MediaTypeUtil.TYPE_GIF -> {
                }
                else -> {
                    prevVideoAliasIndex = videoAliasPosition
                    videoAliasPosition++
                    //图片需要叠层，考虑偏移和时长
                    val pngPath = createTempFilePath(suffix = PNG_FILE_SUFFIX)
                    tempFileArrays.add(pngPath)
                    val bitmap = effectBean.getEffectBitmap(view)
                    bitmap ?: return@mapIndexed
                    //保存当前快照png 透明底
                    BitmapUtil.savePngBitmap(bitmap, pngPath, 100)
                    //合成视频 ffmpeg -loop 1 -t 3 -i input.png -c:v libx264 -preset ultrafast output.mp4
                    inParams.add("-itsoffset")
                    inParams.add(dstStartTime)
//                    -loop 1 -t 3  表示循环3秒输入图像流
                    inParams.add("-loop")
                    inParams.add("1")
                    inParams.add("-t")
                    inParams.add(dstDurationTime)
                    inParams.add("-i")
                    inParams.add(pngPath)
                    if (prevVideoAliasIndex == 0) {
                        effectParams.add("[0:v][$position:v]overlay='if(gte(t,$dstStartTime),0,NAN)':'if(lt(t,$dstEndTime),0,NAN)'[out$videoAliasPosition];")
                    } else {
                        effectParams.add("[out$prevVideoAliasIndex][$position:v]overlay='if(gte(t,$dstStartTime),0,NAN)':'if(lt(t,$dstEndTime),0,NAN)'[out$videoAliasPosition];")
                    }
                    outVideoFinalAlias = "[out$videoAliasPosition]"
                    position++
                }
            }
        }
        if (audioAliasParams.size > 0) {
            //多段音频合成一段（时间最长的为时间轴）
            audioParams.add(audioAliasParams.joinToString(separator = ""))
            audioParams.add("amix=inputs=${audioAliasParams.size}:duration=0$outAudioFinalAlias;")
        }
        //缩放为480P、720P、1080P
        val radio = totalWidth.toFloat() / totalHeight
        var dstWidth = 0
        var dstHeight = 0
        if(radio <= 1){
            dstHeight = Math.min(quality.width,quality.height)
            dstWidth = ( dstHeight * radio).toInt()
        }else if(radio >1){
            dstWidth = Math.max(quality.width,quality.height)
            dstHeight = ( dstWidth / radio).toInt()
        }
        if(dstWidth % 2 != 0){
            dstWidth+=1
        }
        if(dstHeight % 2 != 0){
            dstHeight+=1
        }
//        effectParams.add(outVideoFinalAlias + "scale=${quality.width}:${quality.height}$tempAlias;")
//        effectParams.add(outVideoFinalAlias + "scale=${quality.size}$tempAlias;")
//        effectParams.add(outVideoFinalAlias + "scale=${quality.size}:force_original_aspect_ratio=decrease,pad=${quality.width}:${quality.height}:(ow-iw)/2:(oh-ih)/2$tempAlias;")
//        effectParams.add(outVideoFinalAlias + "scale=${quality.size}:force_original_aspect_ratio=decrease,pad=$dstWidth:$dstHeight:(ow-iw)/2:(oh-ih)/2$tempAlias;")
        effectParams.add(outVideoFinalAlias + "scale=$dstWidth:$dstHeight$tempAlias;")

        outVideoFinalAlias = tempAlias
        val effectParam = effectParams.joinToString(separator = "")
        //去除 ;
        var filterParams = ""
        if(effectParam.isNotEmpty()){
             filterParams = effectParam.substring(0, effectParam.length - 1)
        }
        commandBuilder.add(inParams.joinToString(separator = " "))
        commandBuilder.add("-filter_complex")
        commandBuilder.add(filterParams)
        val tempVideoFile = createTempFilePath(suffix = VIDEO_FILE_SUFFIX)
        tempFileArrays.add(tempVideoFile)
        commandBuilder
                .add("-map")
                .add(outVideoFinalAlias)
                .add(quality.profile)
                .add("-preset")
                .add(quality.preset)
                .add("-crf")
                .add(quality.crf)
                .add("-threads")
                .add("auto")
//                .add("-y")
                .add(tempVideoFile)
                .run(executing)


        val builder = createBuilder().add("-i").add(tempVideoFile)
        //音视频合成
        val tempFinalFile = createTempFilePath(suffix = VIDEO_FILE_SUFFIX)
        tempFileArrays.add(tempFinalFile)

        if (true) {
            //生成音频
            val audioParam = audioParams.joinToString(separator = "")
            val tempAudioFile = if(audioParam.isNotEmpty()){
                val filterParams = audioParam.substring(0, audioParam.length - 1)
                val tempAudioFile = createTempFilePath(suffix = AUDIO_FILE_SUFFIX)
                tempFileArrays.add(tempAudioFile)
                createBuilder()
                        .add(audioInParams.joinToString(separator = " "))
                        .add("-filter_complex")
                        .add(filterParams)
                        .add("-map")
                        .add(outAudioFinalAlias)
                        .add("-y")
                        .add(tempAudioFile)
                        .run(executing)
                tempAudioFile
            } else null

            if (tempAudioFile != null) {
                builder.add("-i").add(tempAudioFile)
            }

        }

        var result =  builder
                .add("-vcodec")
                .add("copy")
                .add("-acodec")
                .add("copy")
                .add(tempFinalFile)
                .run(executing)
        if (executing) {
            try {
                if(FileUtils.isExistFile(tempFinalFile)){
                    //将最终视频移动到目的位置
                    FileUtils.copyFile(tempFinalFile, dstFile, true)
                }
            } catch (e: Exception) {
                result.result = CommandHelper.RESULT_ID_FALID
                result.msgError = e.message
            }
        } else {
            result = CommandHelper.Result(CommandHelper.RESULT_ID_CANCEL)
        }
        // 清除临时文件
        deleteFiles(tempFileArrays)
        return result
    }
    fun stopExecute(){
        executing = false
        CommandHelper.stopExecute()
    }
    /**
     * 计算旋转角度后的宽高度
     */
    private fun calculateRotateWidthAndHeight(videoWidth: Float,videoHeight: Float,angel: Double): PointF{
        var angel1 = Math.abs(angel)
        var angel2 = Math.atan((videoHeight / videoWidth).toDouble())*180 / PI
        var angel3 = angel1 + angel2 - 90
        var angel4 =  90 - angel1
        //转为弧度
         angel1 = angel1 * PI / 180
         angel3 = angel3 * PI / 180
         angel4 = angel4 * PI / 180
        val thirdSide = Math.hypot(videoWidth.toDouble(), videoHeight.toDouble())
        val newHeight = thirdSide * Math.abs(Math.cos(angel3))
        val newWidth = videoHeight * Math.abs(Math.cos(angel4)) + videoWidth * Math.abs(Math.cos(angel1))
        return PointF(newWidth.toFloat(), newHeight.toFloat())
    }

    /**
     * 背景图
     */
    private fun saveBackgroundImage(color: Int,videoWidth: Int,videoHeight: Int,baseFile: String) {
        val bg = Bitmap.createBitmap(videoWidth, videoHeight, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bg)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0f, 0f, videoWidth.toFloat(), videoHeight.toFloat(), paint)
        //合成时间跟底图图片质量有关
        BitmapUtil.saveBitmap(bg, baseFile, 5)
    }


    @JvmStatic
    @JvmOverloads
    fun createTempFilePath(prefix: String? = null, suffix: String? = null, dirPath: String = sTmpDirPath): String {
        var n = Random().nextLong()
        if (n == java.lang.Long.MIN_VALUE) {
            n = 0      // corner case
        } else {
            n = Math.abs(n)
        }
        val name = "${(prefix ?: "tmp")}${n}${(suffix ?: ".tmp")}"
        var dirPath = dirPath
        if (dirPath.isEmpty()) {
            dirPath = App.getContext().cacheDir.absolutePath
        }
        return File(dirPath, name).absolutePath
    }

    /**
     * 裁剪视频
     * @param srcFile 源文件路径
     * @param dstFile 生成后文件路径
     * @param start 裁剪起始时间
     * @param duration 裁剪时长（单位：秒）
     */
    @JvmStatic
    fun clipVideo(srcFile: String?, dstFile: String?, start: Int?, duration: Int?) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (srcFile == null || dstFile == null || start == null || duration == null) {
            return
        }
        val time = System.currentTimeMillis()
        val file = File(dstFile)
        if (file.exists()) {
            file.delete()
        }
        // ffmpeg -ss 0 -t 30 -accurate_seek -i src.mp4 -codec copy -avoid_negative_ts 1 cut.mp4
        createBuilder()
                .add("-ss")
                .add(start.toString())
                .add("-t")
                .add(duration.toString())
                .add("-accurate_seek")
                .add("-i")
                .add(srcFile)
                .add("-codec")
                .add("copy")
                .add("-avoid_negative_ts")
                .add("1")
                .add(dstFile)
                .run(dstFile)
//        DLog.i(TAG, "clipVideo time:" + (System.currentTimeMillis() - time))
    }

//    /**
//     * 已一个视频为基础，制作一个指定分辨率，指定时长的视频
//     * 耗时操作，需放在异步执行
//     * @param srcFile       基础视频保存路径
//     * @param dstFile       目标视频保存路径
//     * @param width         目标视频宽度(单位：像素)
//     * @param height        目标视频高度(单位：像素)
//     * @param srcduring     原视频时长（单位：毫秒）
//     * @param dstduring     目标视频时长（单位：毫秒）
//     */
//    @JvmStatic
//    fun makeEmptyVideo(srcFile: String, dstFile: String, width: Int, height: Int, srcduring: Int, dstduring: Int) {
//        if (sFfmpegFile.isNullOrEmpty()) {
//            return
//        }
//        val time = System.currentTimeMillis()
//        val tmpFile = createTempFilePath(suffix = VIDEO_FILE_SUFFIX);// "$sTmpDirPath/empty$time$VIDEO_FILE_SUFFIX"
//        makeScaleVideo(srcFile, tmpFile, width, height)
//
//        if (DebugUtil.isDebuggable()) {
//
//        }
//        val commandBuilder = createBuilder()
//        val count = dstduring / srcduring + 1
//        val builder = StringBuilder()
//        for (i in 0 until count) {
//            builder.append("file '").append(tmpFile).append("'\n")
//        }
//        val concatFile = createTempFilePath()// "$sTmpDirPath/concat$time.txt"
//        FileUtil.saveStringToFile(builder.toString(), concatFile)
//
//        commandBuilder
//                .add("-safe")
//                .add("0")
//                .add("-f")
//                .add("concat")
//                .add("-i")
//                .add(concatFile)
//                .add("-c")
//                .add("copy")
//                .add("-preset")
//                .add("ultrafast")
//                .add(dstFile)
//                .run(dstFile)
//
//        FileUtils.delete(File(tmpFile))
//        FileUtils.delete(File(concatFile))
////        DLog.i(TAG, "emptyvideo time:" + (System.currentTimeMillis() - time))
//    }

//    @JvmStatic
//    fun makeScaleVideo(srcFile: String, dstFile: String, width: Int, height: Int) {
//        if (sFfmpegFile.isNullOrEmpty()) {
//            return
//        }
//        val commandBuilder = createBuilder()
//                .add("-i")
//                .add(srcFile)
//                .add("-crf")
//                .add("27")
//                .add("-s")
//                .add(width.toString() + "x" + height)
//                //        .add("-an");
//                .add("-movflags")
//                .add("faststart")
//                .add("-preset")
//                .add("ultrafast")
//                .add(dstFile)
//                .run(dstFile)
//    }

    /**
     * 图片+多视频合并，外部需要先将图片合成为bitmap(顺序播放时，需要先把各视频的第一帧先合成到bitamp中)，方法内部将多视频合并在图片上
     * 耗时方法，必须在异步线程执行
     * @param bg                视频合成在此bitmap中，bitmap大小需为实际合并后的大小
     * @param cover             顶部遮罩图，可为空
     * @param playTogether      是否同时播放
     * @param combineBeanList   需要合成的视频列表数据
     * @param dstFile           文件保存路径
     */
    @JvmStatic
    fun mergeVideo(bg: Bitmap?, cover: Bitmap?, playTogether: Boolean?,
                   combineBeanList: MutableList<CombineBean>?, dstFile: String?,
                   highDefinitionBg: Boolean?) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (bg == null|| playTogether == null || combineBeanList == null
                || dstFile == null || highDefinitionBg == null) {
            return
        }
        val time = System.currentTimeMillis()
        val tmpFileList = ArrayList<String>()

        val imgFile = createTempFilePath()//"$sTmpDirPath/img$time$IMG_FILE_SUFFIX"
        tmpFileList.add(imgFile)
        // 合成时间跟底图图片质量有关
        if (highDefinitionBg) {
            BitmapUtil.saveBitmap(bg, imgFile, 100)
        } else {
            BitmapUtil.saveBitmap(bg, imgFile, 5)
        }
        BitmapUtil.saveImgSizeExif(imgFile, bg.width, bg.height)
        val bgBean = CombineBean()
        bgBean.type = CombineBean.TYPE_IMG
        bgBean.file = imgFile
        mergeVideo(bgBean, cover, playTogether, combineBeanList, dstFile)
        // 清除临时文件
        deleteFiles(tmpFileList)

//        DLog.i(TAG, "mergeVideo time:" + (System.currentTimeMillis() - time))
    }

    /**
     * 图片+多视频合并，外部需要先将图片合成为bitmap(顺序播放时，需要先把各视频的第一帧先合成到bitamp中)，方法内部将多视频合并在图片上
     * 耗时方法，必须在异步线程执行
     * @param bean            背景bean，可为图片或视频，大小需为实际合并后的大小
     * @param cover             顶部遮罩图，可为空
     * @param playTogether      是否同时播放
     * @param combineBeanList   需要合成的视频列表数据
     * @param dstFile           文件保存路径
     */
    @JvmStatic
    fun mergeVideo(bean: CombineBean?, cover: Bitmap?, playTogether: Boolean?,
                   combineBeanList: MutableList<CombineBean>?, dstFile: String?) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (bean == null || playTogether == null || combineBeanList == null
                || dstFile == null) {
            return
        }
        val time = System.currentTimeMillis()
        val tmpFileList = ArrayList<String>()
        val commandBuilder = createBuilder()
        //        commandBuilder.add("-loglevel");
        //        commandBuilder.add("quiet");

        // 顺序播放时，需要把时间长的放在前面，否则短的播放完后，长的也停止播放
        if (playTogether && combineBeanList.size > 1) {
            combineBeanList.sortWith(Comparator { o1, o2 -> o2.srcDuring.compareTo(o1.srcDuring) })
        }

        if (playTogether && bean.type == CombineBean.TYPE_IMG) {
            commandBuilder.add("-loop", "1")
        }
        commandBuilder.add("-i", bean.file)

        // 合并后视频时长
        var during = 0
        // 配置输入视频延时
        for (input in combineBeanList) {
            if (input.srcDuring + input.startOffset > during) {
                during = input.srcDuring + input.startOffset
            }
            if (!playTogether) {
                // 顺序播放，配置视频开始播放延时
                val offsetTime = input.startOffset
                if (offsetTime > 0) {
                    commandBuilder.add("-itsoffset", buildTimeString(offsetTime))
                }
            }
            commandBuilder.add("-i", input.file)
        }
        if (bean.srcDuring > during) {
            during = bean.srcDuring
        }
        // 如果有顶部遮罩
        var coverFile: String? = null
        if (cover != null) {
            coverFile = createTempFilePath()//"$sTmpDirPath/img_cover$time$IMG_FILE_SUFFIX"
            tmpFileList.add(coverFile)
            commandBuilder.add("-i")
            BitmapUtil.savePngBitmap(cover, coverFile, 90)
            //            BitmapUtil.saveImgSizeExif(coverFile, cover.getWidth(), cover.getHeight());
            commandBuilder.add(coverFile)
        }
        if (playTogether) {
            // 视频合成滤镜
            commandBuilder.add("-filter_complex")
                    .add(buildVideoFilter(coverFile, playTogether, combineBeanList, false, "[0]", bean.type == CombineBean.TYPE_IMG))
                    .add("-c:v")
                    .add("libx264")
                    .add("-preset")
                    .add("ultrafast")
                    .add("-t")
                    .add(buildTimeString(during))
                    .add(dstFile)
                    .run(dstFile)
        } else {
            var bgChannel = "[0]"
            if (combineBeanList.size > 1 && bean.type == CombineBean.TYPE_IMG) {
                for (i in 1 until combineBeanList.size) {
                    val input = combineBeanList[i]
                    commandBuilder.add("-i", input.file)
                }
                bgChannel = "[bg" + (combineBeanList.size - 1) + "]"
            }
            // 顺序播放，先要将视频无声合成，再分别将声音单独合成后，再与视频一起合成
            val slientVideoPath = createTempFilePath()//"$sTmpDirPath/sv$time$VIDEO_FILE_SUFFIX"
            tmpFileList.add(slientVideoPath)
            // 合成无声音视频
            commandBuilder.add("-filter_complex")
            val filter = buildVideoFilter(coverFile, playTogether, combineBeanList, true, bgChannel, bean.type == CombineBean.TYPE_IMG)
            if (combineBeanList.size > 1 && bean.type == CombineBean.TYPE_IMG) {
                val bgFilter = buildBgFilter(combineBeanList, cover != null)
                commandBuilder.add(bgFilter + filter)
            } else {
                commandBuilder.add(filter)
            }
            commandBuilder.add("-c:v")
                    .add("libx264")
                    .add("-preset")
                    .add("ultrafast")
                    .add("-threads")
                    .add("0")
                    .add("-t")
                    .add(buildTimeString(during))
                    .add("-an")
                    .add("-y")
                    .add(slientVideoPath)
                    .run(slientVideoPath)
            // 将音频提取出来再进行合并
            var audioInputCount = 0
            for (input in combineBeanList) {
                if (!input.slient && input.hasAudio) {
                    ++audioInputCount
                }
            }
            if (audioInputCount == 0) {
                // 都为静音，直接copy静音视频到目标路径
                try {
                    FileUtils.copyFile(slientVideoPath, dstFile, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                val audioOut: String = createTempFilePath()//"$sTmpDirPath/audio${time}AUDIO_FILE_SUFFIX"
                tmpFileList.add(audioOut)
                concatAudio(combineBeanList, audioOut)

                mergeVideoAndAudio(slientVideoPath, audioOut, dstFile)
            }
        }

        // 清除临时文件
        deleteFiles(tmpFileList)

//        DLog.i(TAG, "mergeVideo time:" + (System.currentTimeMillis() - time))
    }

    private fun concatAudio(combineBeanList: List<CombineBean>, output: String) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        val commandBuilder = createBuilder()
        //        commandBuilder.add("-loglevel");
        //        commandBuilder.add("quiet");

        // 合并后视频时长
        var during = 0
        var offset = 0
        val tmpList = ArrayList<CombineBean>()
        for (input in combineBeanList) {
            if (input.srcDuring + input.startOffset > during) {
                during = input.srcDuring + input.startOffset
            }
//            if (input.startOffset > offset) {
//                // 增加gif元素后，顺序播放时，中间如果插了一个gif，音频需要插入一个等时长的空音频
//                val tmp = CombineBean()
//                tmp.srcDuring = input.startOffset - offset
//                tmp.hasAudio = false
//                tmp.file = FileUtil.BASIC_VIDEO_50_FILE_PATH
//                commandBuilder.add("-i", tmp.file)
//                tmpList.add(tmp)
//            }
            offset = input.startOffset + input.srcDuring
            commandBuilder.add("-i", input.file)
            tmpList.add(input)
        }
        commandBuilder.add("-filter_complex")
                .add(buildAudioFilter(tmpList))
                .add("-map")
                .add("[out]")
                .add("-c:v")
                .add("libx264")
                .add("-preset")
                .add("ultrafast")
                .add("-t")
                .add(buildTimeString(during))
                .add("-y")
                .add(output)
                .run(output)
    }

    private fun buildAudioFilter(combineBeanList: List<CombineBean>): String {
        val tmp = StringBuilder()
        val concatInput = ArrayList<String>(combineBeanList.size)
        for (i in combineBeanList.indices) {
            val bean = combineBeanList[i]
            if (!bean.hasAudio) {
                tmp.append("aevalsrc=0:d=")
                        .append(bean.srcDuring)
                        .append("[s")
                        .append(i)
                        .append("];")
                concatInput.add("[s$i]")
            } else if (bean.slient) {
                tmp.append("[")
                        .append(i)
                        .append(":a]volume=0.0")
                        .append("[s")
                        .append(i)
                        .append("];")
                concatInput.add("[s$i]")
            } else {
                concatInput.add("[$i:a]")
            }
        }
        for (i in concatInput.indices) {
            tmp.append(concatInput[i])
        }
        tmp.append("concat=n=")
                .append(concatInput.size)
                .append(":v=0:a=1[out]")
        val ret = tmp.toString()
//        DLog.i(TAG, "audiofilter:$ret")
        return ret
    }

    private fun buildVideoFilter(coverFilePath: String?, playTogether: Boolean,
                                 combineBeanList: List<CombineBean>,
                                 ignoreAudio: Boolean, bgChannel: String, imgBg: Boolean): String {
        // 注意：图片是第一个的输入文件
        val tmp = StringBuilder()
        // 先对各视频进行缩放裁剪
        for ((i, bean) in combineBeanList.withIndex()) {
            // [1:v]setpts=PTS-STARTPTS,scale=-1:702,crop=228:702:149:0[inner1];
            val width = (bean.srcWidth * bean.scale).toInt()
            val height = (bean.srcHeight * bean.scale).toInt()
            tmp.append("[" + (i + 1) + ":v]")
            if (playTogether && imgBg) {
                tmp.append("setpts=PTS-STARTPTS,")
            }
            tmp.append("scale=")
            if (width > height) {
                tmp.append(-1)
                tmp.append(":")
                tmp.append(height)
            } else {
                tmp.append(width)
                tmp.append(":")
                tmp.append(-1)
            }
            tmp.append(",")
            tmp.append("crop=")
            tmp.append(bean.clipRect.width())
            tmp.append(":")
            tmp.append(bean.clipRect.height())
            tmp.append(":")
            tmp.append(bean.clipRect.left)
            tmp.append(":")
            tmp.append(bean.clipRect.top)
            tmp.append("[inner" + (i + 1) + "];")
        }
        // 再对各视频进行合并
        // 以图片为底图进行合并
        var input = bgChannel
        for ((i, bean) in combineBeanList.withIndex()) {
            // [0][inner1]overlay=245:8[tmp1];[tmp1][inner2]overlay=482:8[tmp2];
            // [tmp2][inner3]overlay=8:8[tmp3];[tmp3][4]overlay=605:612;
            tmp.append(input)
            tmp.append("[inner" + (i + 1) + "]")
            tmp.append("overlay=")
            tmp.append(bean.pos.x)
            tmp.append(":")
            tmp.append(bean.pos.y)
            if (i != combineBeanList.size - 1 || !coverFilePath.isNullOrEmpty()) {
                input = "[tmp" + (i + 1) + "]"
                tmp.append(input)
                tmp.append(";")
            } else if (i == combineBeanList.size - 1 || TextUtils.isEmpty(coverFilePath)) {
                tmp.append(";")
            }
        }
        if (!TextUtils.isEmpty(coverFilePath)) {
            tmp.append(input)
            tmp.append("[" + (combineBeanList.size + 1) + "]")
            tmp.append("overlay=0:0;")
        }
        if (!ignoreAudio) {
            // 处理音频，默认合并后只有第一个音频有声音
            // [1:a]volume=1.0[a1];[2:a]volume=0.0[a2];[3:a]volume=0.0[a3];[a1][a2][a3]amix=inputs=3:duration=longest:dropout_transition=3
            var audioCount = 0
            var hasAudio = false
            for ((i, bean) in combineBeanList.withIndex()) {
                if (bean.hasAudio) {
                    hasAudio = true
                    break
                }
            }
            if (hasAudio) {
                for ((i, bean) in combineBeanList.withIndex()) {
                    if (bean.hasAudio) {
                        tmp.append("[")
                        tmp.append(i + 1)
                        tmp.append(":a]volume=")
                        if (bean.slient) {
                            tmp.append("0.0")
                        } else {
                            tmp.append("1.0")
                        }
                        tmp.append("[a")
                        tmp.append(i + 1)
                        tmp.append("];")
                        ++audioCount
                    }
                }
                for ((i, bean) in combineBeanList.withIndex()) {
                    if (bean.hasAudio) {
                        tmp.append("[a")
                        tmp.append(i + 1)
                        tmp.append("]")
                    }
                }
                tmp.append("amix=inputs=")
                tmp.append(audioCount)
                tmp.append(":duration=longest:dropout_transition=3;")
            }
        }
        val ret = tmp.substring(0, tmp.length - 1)
//        DLog.i(TAG, "videofilter:$ret")
        return ret
    }

    private fun buildBgFilter(combineBeanList: List<CombineBean>, hasCover: Boolean): String {
        val tmp = StringBuilder()
        var bgChannel = "[0]"
        for (i in 1 until combineBeanList.size) {
            val bean = combineBeanList[i]
            tmp.append("[")
            tmp.append(combineBeanList.size + i + if (hasCover) 1 else 0)
            tmp.append("]select='eq(n\\,1)',scale=")
            val width = (bean.srcWidth * bean.scale).toInt()
            val height = (bean.srcHeight * bean.scale).toInt()
            if (width > height) {
                tmp.append(-1)
                tmp.append(":")
                tmp.append(height)
            } else {
                tmp.append(width)
                tmp.append(":")
                tmp.append(-1)
            }
            tmp.append(",")
            tmp.append("crop=")
            tmp.append(bean.clipRect.width())
            tmp.append(":")
            tmp.append(bean.clipRect.height())
            tmp.append(":")
            tmp.append(bean.clipRect.left)
            tmp.append(":")
            tmp.append(bean.clipRect.top)
            tmp.append("[ov")
            tmp.append(i)
            tmp.append("];")
            tmp.append(bgChannel)
            tmp.append("[ov")
            tmp.append(i)
            tmp.append("]")
            tmp.append("overlay=")
            tmp.append(bean.pos.x)
            tmp.append(":")
            tmp.append(bean.pos.y)
            bgChannel = "[bg$i]"
            tmp.append(bgChannel)
            tmp.append(";")
        }
        val ret = tmp.toString()
//        DLog.i(TAG, "bgfilter:$ret")
        return ret
    }

    /**
     * 音频+视频合并
     * @param videoFile
     * @param audioFile
     * @param dstFile
     */
    @JvmStatic
    fun mergeVideoAndAudio(videoFile: String?, audioFile: String?, dstFile: String?) {
        if (videoFile == null || audioFile == null || dstFile == null) {
            return
        }
        // 使用MediaMuxer合并音视频，加快合成速度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mergeVideoAndAudioByMediaMuxer(videoFile, audioFile, dstFile, 0)
            return
        }
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        createBuilder()
                .add("-i")
                .add(videoFile)
                .add("-i")
                .add(audioFile)
                .add("-movflags")
                .add("faststart")
                .add("-preset")
                .add("ultrafast")
                .add(dstFile)
                .run(dstFile)
    }

    @JvmStatic
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    fun mergeVideoAndAudioByMediaMuxer(videoFile: String?, audioFilePath: String?, dstFile: String?, rotate: Int?) {
        if (videoFile == null || audioFilePath == null || dstFile == null || rotate == null) {
            return
        }
        val outputFile: String
        try {
            val file = File(dstFile)
            file.delete()
            file.createNewFile()
            outputFile = file.absolutePath
            val videoExtractor = MediaExtractor()
            videoExtractor.setDataSource(videoFile)
            val audioExtractor = MediaExtractor()
            audioExtractor.setDataSource(audioFilePath)

            val muxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            muxer.setOrientationHint(rotate)

            var trackIndex = MoviePlayer.selectTrack(videoExtractor, "video/")
            val videoFormat = videoExtractor.getTrackFormat(trackIndex)
            videoExtractor.selectTrack(trackIndex)
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
            var videoMaxInputSize = 1024
            try {
                videoMaxInputSize = videoFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val videoTrack = muxer.addTrack(videoFormat)

            trackIndex = MoviePlayer.selectTrack(audioExtractor, "audio/")
            val audioFormat = audioExtractor.getTrackFormat(trackIndex)
            audioExtractor.selectTrack(trackIndex)
            audioFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
            var audioMaxInputSize = 1024
            try {
                audioMaxInputSize = audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val audioTrack = muxer.addTrack(audioFormat)

            var sawEOS = false
            var frameCount = 0
            val offset = 0
            val videoBuf = ByteBuffer.allocate(videoMaxInputSize)
            val audioBuf = ByteBuffer.allocate(audioMaxInputSize)
            val videoBufferInfo = MediaCodec.BufferInfo()
            val audioBufferInfo = MediaCodec.BufferInfo()

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            muxer.start()

            while (!sawEOS) {
                videoBufferInfo.offset = offset
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset)

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    sawEOS = true
                    videoBufferInfo.size = 0
                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.sampleTime
                    videoBufferInfo.flags = videoExtractor.sampleFlags//BUFFER_FLAG_KEY_FRAME;
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo)
                    videoExtractor.advance()
                    frameCount++
                }
            }

//            Toast.makeText(context.applicationContext, "frame:$frameCount", Toast.LENGTH_SHORT).show()

            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            var sawEOS2 = false
            while (!sawEOS2) {

                audioBufferInfo.offset = offset
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset)

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    sawEOS2 = true
                    audioBufferInfo.size = 0
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.sampleTime
                    audioBufferInfo.flags = audioExtractor.sampleFlags
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo)
                    audioExtractor.advance()
                }
            }
            videoExtractor.release()
            audioExtractor.release()
            muxer.stop()
            muxer.release()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 合并多段音频（非拼接）
     * @param dstFile
     * @param audioFile
     */
    @JvmStatic
    fun mergeAudio(dstFile: String?, vararg audioFile: String) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (dstFile == null) {
            return
        }
        val commandBuilder = createBuilder()
        for (input in audioFile) {
            commandBuilder.add("-i", input)
        }
        commandBuilder.add("-filter_complex")
                .add("amix=inputs=" + audioFile.size + ":duration=longest:dropout_transition=0")
                .add(dstFile)
                .run(dstFile)
    }

    /**
     * 去除视频中音频
     * @param srcFile
     * @param dstFile
     */
    @JvmStatic
    fun extractVideo(srcFile: String?, dstFile: String?) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (dstFile == null || srcFile == null) {
            return
        }
        createBuilder()
                .add("-i")
                .add(srcFile)
                .add("-acodec")
                .add("copy")
                .add("-an")
                .add(dstFile)
                .run(dstFile)
    }

    /**
     * 从视频中提取音频，并在音频前延时指定时间
     * @param srcFile       视频文件
     * @param dstFile       输出音频文件
     * @param offsetTime    音频延时播放时间（单位：毫秒）
     */
    @JvmStatic
    fun extractAudio(srcFile: String?, dstFile: String?, offsetTime: Long?) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (dstFile == null || srcFile == null || offsetTime == null) {
            return
        }
        val tmp: String
        if (offsetTime == 0L) {
            tmp = dstFile
        } else {
            tmp = createTempFilePath(suffix = AUDIO_FILE_SUFFIX)// "$sTmpDirPath/${System.currentTimeMillis() + 1}.aac"
        }
        // 先提取音频，再对视频插入延时时间
        val commandBuilder = createBuilder()
                .add("-i")
                .add(srcFile)
                .add("-acodec")
                .add("copy")
                .add("-vn")
                .add(tmp)
        commandBuilder.run(tmp)

        if (offsetTime > 0) {
            commandBuilder.clear()
            commandBuilder.add("-i")
                    .add(tmp)
                    .add("-filter_complex")
                    .add("adelay=$offsetTime|$offsetTime")
                    .add(dstFile)
                    .run(dstFile)
            FileUtils.delete(File(tmp))
        }

    }

    private fun createBuilder(): CommandHelper.CommandBuilder {
        return CommandHelper.CommandBuilder.create(sFfmpegFile!!)
    }

    /**
     * 耗时方法，需要放在异步线程
     * @param cmd
     */
    @JvmStatic
    fun CommandHelper.CommandBuilder.run(output: String?) {
        if (sFfmpegFile.isNullOrEmpty()) {
            return
        }
        if (output == null) {
            return
        }
        val ffmpegFilePath = sFfmpegFile!!
        val time = System.currentTimeMillis()

        val result = this.run(executing)
        if (result != null && result.result == 1) {
            var error = result.msgError
            // 只统计后面错误信息
            if (error?.length ?: 0 > 245) {
                error = error?.substring(error.length - 245, error.length)
            }
            var command = this.getCommand()
            val file = File(output)
            val simpleCmd = command.replace(ffmpegFilePath, "")
            val ffmpegCanExecute = File(ffmpegFilePath).canExecute()
            val fileExists = file.exists()
            statisticsFfmpeg(error ?: "unknown", simpleCmd, fileExists, ffmpegCanExecute)

            if (error?.contains("unused DT entry") == true) {
                statisticsFfmpeg(error, simpleCmd, fileExists, ffmpegCanExecute)
            }
            if (error?.contains("No such file or directory") == true) {
                var fileCmd: String? = null
                for ((i, tmp) in this.params.withIndex()) {
                    if (tmp == "-i" && i < this.params.size - 1) {
                        fileCmd = this.params[i + 1]
                        if (fileCmd.isNullOrEmpty() || !File(fileCmd).exists()) {
                            fileCmd = null
                        }
                    }
                }
                statisticsFfmpeg(error, simpleCmd, fileExists, ffmpegCanExecute, fileCmd)
            }
        }
//        DLog.i(TAG, "cmd " + "time:" + (System.currentTimeMillis() - time) + " result:" + result!!.result + "  " + result.msgError)
    }

    /**
     *
     * @param time 单位ms
     * @return
     */
    private fun buildTimeString(time: Int): String {
        val offsetSecond = time / 1000
        val offsetMs = time % 1000
        return String.format("%d.%03d", offsetSecond, offsetMs)
    }

    @JvmStatic
    fun deleteFiles(path: List<String>) {
        ThreadPool.runOnNonUIThread({
            for (tmp in path) {
                FileUtils.delete(File(tmp))
            }
        }, 2000)
    }

//    private fun copyAssets(context: Context, assetsFilename: String, file: File, mode: String) {
//        try {
//            val manager = context.assets
//            val `is` = manager.open(assetsFilename)
//            copyFile(file, `is`, mode)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }
//
//    @Throws(IOException::class, InterruptedException::class)
//    private fun copyFile(file: File, `is`: InputStream, mode: String) {
//        if (!file.parentFile.exists()) {
//            file.parentFile.mkdirs()
//        }
//        val abspath = file.absolutePath
//        val out = FileOutputStream(file)
//        val buf = ByteArray(1024)
//        var len: Int = 0
//        while (`is`.read(buf).also { len = it } > 0) {
//            out.write(buf, 0, len)
//        }
//        out.close()
//        `is`.close()
//        Runtime.getRuntime().exec("chmod $mode $abspath").waitFor()
//    }

    private fun statisticsFfmpeg(msg: String, cmd: String? = null, fileExist: Boolean? = null, ffmpegExec: Boolean? = null, noFileName: String? = null) {
        val model = "${Build.VERSION.SDK_INT}_${Build.CPU_ABI}_${Build.MODEL}_${Build.BRAND}"
        var attributes = mutableListOf<String>()
        attributes.add("model")
        attributes.add(model)
        attributes.add("status")
        attributes.add("${(fileExist ?: false)}_${(ffmpegExec
                ?: false)}_${FileUtil.getExternalStorageInfoSize()}_${(noFileName ?: "")}")
        if (!msg.isNullOrEmpty()) {
            attributes.add("msg")
            attributes.add(msg)
        }
        if (!cmd.isNullOrEmpty()) {
            attributes.add("cmd")
            attributes.add(cmd)
        }
//        StatisticsUtils.statisics("ffmpeg", attributes)
    }

}
