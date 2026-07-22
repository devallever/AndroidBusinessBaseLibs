package app.allever.android.lib.router

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

object Router {

    private val routeMap = mutableMapOf<String, Class<*>>()

    fun register(path: String, clazz: Class<*>) {
        routeMap[path] = clazz
    }

    fun build(path: String): RouteRequest {
        return RouteRequest(path)
    }

    internal fun find(path: String): Class<*>? {
        return routeMap[path]
    }

    fun hasRoute(path: String): Boolean {
        return routeMap.containsKey(path)
    }

    fun getRouteClass(path: String): Class<*>? {
        return routeMap[path]
    }

    fun getRoutePathList(): List<String> {
        return routeMap.keys.toList()
    }
}

class RouteRequest(private val path: String) {

    private val extras = Bundle()
    private var flags: Int = 0
    private var enterAnim: Int = -1
    private var exitAnim: Int = -1

    fun withString(key: String, value: String?): RouteRequest {
        extras.putString(key, value)
        return this
    }

    fun withInt(key: String, value: Int): RouteRequest {
        extras.putInt(key, value)
        return this
    }

    fun withLong(key: String, value: Long): RouteRequest {
        extras.putLong(key, value)
        return this
    }

    fun withFloat(key: String, value: Float): RouteRequest {
        extras.putFloat(key, value)
        return this
    }

    fun withBoolean(key: String, value: Boolean): RouteRequest {
        extras.putBoolean(key, value)
        return this
    }

    fun withBundle(bundle: Bundle?): RouteRequest {
        if (bundle != null) {
            extras.putAll(bundle)
        }
        return this
    }

    fun withFlags(flags: Int): RouteRequest {
        this.flags = flags
        return this
    }

    fun withTransition(enterAnim: Int, exitAnim: Int): RouteRequest {
        this.enterAnim = enterAnim
        this.exitAnim = exitAnim
        return this
    }

    fun navigation(context: Context) {
        val clazz = Router.find(path)
        if (clazz != null) {
            val intent = Intent(context, clazz)
            intent.putExtras(extras)
            if (flags != 0) {
                intent.addFlags(flags)
            }
            if (context !is AppCompatActivity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            if (context is AppCompatActivity && enterAnim != -1 && exitAnim != -1) {
                context.overridePendingTransition(enterAnim, exitAnim)
            }
        } else {
            throw RouteNotFoundException(path)
        }
    }

    fun navigation(context: Context, requestCode: Int) {
        val clazz = Router.find(path)
        if (clazz != null) {
            val intent = Intent(context, clazz)
            intent.putExtras(extras)
            if (flags != 0) {
                intent.addFlags(flags)
            }
            if (context is AppCompatActivity) {
                context.startActivityForResult(intent, requestCode)
                if (enterAnim != -1 && exitAnim != -1) {
                    context.overridePendingTransition(enterAnim, exitAnim)
                }
            } else {
                throw IllegalArgumentException("context must be AppCompatActivity for startActivityForResult")
            }
        } else {
            throw RouteNotFoundException(path)
        }
    }
}

class RouteNotFoundException(path: String) : RuntimeException("Route not found: $path")