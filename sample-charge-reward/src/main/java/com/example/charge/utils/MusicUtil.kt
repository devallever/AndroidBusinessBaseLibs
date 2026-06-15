package com.example.charge.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.charge.ChargeApp
import com.example.charge.R

object MusicUtil {

    // ====== 对外字段（与你现有一致，方便无感替换） ======
/*    var mainPlayer: MediaPlayer? = null
        private set*/
    var hitMolePlayer: MediaPlayer? = null
        private set
    var receiveCoinPlayer: MediaPlayer? = null
        private set

    private var hasInit = false

    var isOpenMusic: Boolean
        get() = SpUtil.get(SpKey.IS_MUSIC_OPEN, true)
        set(value) {
            SpUtil.put(SpKey.IS_MUSIC_OPEN, value)
/*            if (value) {
                play(mainPlayer)
            } else {
                // 关闭音乐时立即静音
                pause(mainPlayer)
            }*/
        }

    // ====== 轨道与播放器统一管理 ======
    private enum class Track(val resId: Int, val looping: Boolean) {
//        MAIN(R.raw.bgm, true),
        HIT_MOLE(R.raw.hit_mole_bgm, true),
        RECEIVE_COIN(R.raw.receive_coin_bgm, true)
    }

    private val players = mutableMapOf<Track, MediaPlayer?>()

    fun init() {
        if (hasInit) return
        hasInit = true

        Track.entries.forEach { track ->
            players[track] = buildPlayer(track)
        }

//        mainPlayer = players[Track.MAIN]
        hitMolePlayer = players[Track.HIT_MOLE]
        receiveCoinPlayer = players[Track.RECEIVE_COIN]

//        play(mainPlayer)
    }

    fun pause(player: MediaPlayer?) {
        runCatching {
            if (player?.isPlaying == true) player.pause()
        }
    }

    fun play(player: MediaPlayer?) {
        runCatching {
            if (isOpenMusic && player != null && !player.isPlaying) {
                player.start()
            }
        }
    }

    /** 释放全部 */
    fun release() {
        // 停止并释放全部
        players.forEach { (_, mp) ->
            runCatching {
                if (mp?.isPlaying == true) mp.pause()
                mp?.seekTo(0)
                mp?.reset()
                mp?.release()
            }
        }
        players.clear()

        // 同步清空对外字段
//        mainPlayer = null
        hitMolePlayer = null
        receiveCoinPlayer = null
        hasInit = false
    }

    // ====== 内部：构建与重建 ======
    private fun buildPlayer(track: Track): MediaPlayer {
        val context = ChargeApp.instance
        val mp = MediaPlayer.create(context, track.resId).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            isLooping = track.looping

            // 出错自动重建该轨
            setOnErrorListener { _, _, _ ->
                rebuild(track)
                true
            }
        }
        return mp
    }

    private fun rebuild(track: Track) {
        // 释放旧的
        players[track]?.let { old ->
            runCatching {
                old.reset()
                old.release()
            }
        }
        // 重建并回填
        val newMp = buildPlayer(track)
        players[track] = newMp

        // 同步到对外字段
        when (track) {
//            Track.MAIN -> mainPlayer = newMp
            Track.HIT_MOLE -> hitMolePlayer = newMp
            Track.RECEIVE_COIN -> receiveCoinPlayer = newMp
        }

        // 若开关开启则继续播放
        if (isOpenMusic) runCatching { newMp.start() }
    }
}
