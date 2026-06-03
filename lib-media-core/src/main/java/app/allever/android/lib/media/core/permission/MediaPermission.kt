package app.allever.android.lib.media.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import app.allever.android.lib.media.core.model.MediaType

/**
 * 媒体库权限适配
 * 自动处理 Android 各版本的存储权限差异：
 * - API < 33：READ_EXTERNAL_STORAGE
 * - API >= 33：READ_MEDIA_IMAGES / READ_MEDIA_VIDEO / READ_MEDIA_AUDIO（细分）
 */
object MediaPermission {

    // ==================== 权限判断 ====================

    /**
     * 根据请求的类型集合，返回所需权限数组
     */
    fun requiredPermissions(types: Set<MediaType.Type>): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= 33 -> buildList {
                if (types.contains(MediaType.Type.IMAGE))
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                if (types.contains(MediaType.Type.VIDEO))
                    add(Manifest.permission.READ_MEDIA_VIDEO)
                if (types.contains(MediaType.Type.AUDIO))
                    add(Manifest.permission.READ_MEDIA_AUDIO)
            }.toTypedArray()
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * 检查是否已拥有所需权限
     * @return true 已全部授权，false 存在未授权的权限
     */
    fun hasPermission(context: Context, types: Set<MediaType.Type>): Boolean {
        return requiredPermissions(types).all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 检查是否拥有所有媒体类型的权限
     */
    fun hasAllPermission(context: Context): Boolean {
        return hasPermission(context, MediaType.ALL)
    }

    // ==================== 权限常量 ====================

    /** API 13+ 细分权限集合 */
    object Api33 {
        const val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES
        const val READ_MEDIA_VIDEO = Manifest.permission.READ_MEDIA_VIDEO
        const val READ_MEDIA_AUDIO = Manifest.permission.READ_MEDIA_AUDIO
    }

    /** API 13 以下通用存储权限 */
    object Legacy {
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
