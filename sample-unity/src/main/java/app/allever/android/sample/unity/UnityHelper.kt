package app.allever.android.sample.unity

import android.annotation.SuppressLint
import android.content.Context
import com.unity3d.player.UnityPlayer

object UnityHelper {

    @SuppressLint("StaticFieldLeak")
    var unityPlayer: UnityPlayer? = null
        private set

    fun initUnityPlayer(context: Context) {
        if (unityPlayer == null) unityPlayer = UnityPlayer(context)
    }

    fun destroyUnityPlayer() {
//        unityPlayer?.quit()
        unityPlayer = null
    }
}