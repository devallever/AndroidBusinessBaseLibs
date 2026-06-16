package app.flash.tunnel.vpn.lib.common.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.flash.tunnel.vpn.lib.common.Common
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


/***
 * 对界面线程执行同步 I/O 操作可能会导致 ANR 或界面卡顿。您可以通过从 DataStore 异步预加载数据来减少这些问题：
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     lifecycleScope.launch {
 *         context.dataStore.data.first()
 *         // You should also handle IOExceptions here.
 *     }
 * }
 */

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "config")

object PreferenceStore {
    private val mDatsStore by lazy {
        Common.context.dataStore
    }

    fun <T> putData(key: String, value: T) {
        runBlocking {
            when (value) {
                is Boolean -> {
                    mDatsStore.putBoolean(key, value)
                }

                is Double -> {
                    mDatsStore.putDouble(key, value)
                }

                is Long -> {
                    mDatsStore.putLong(key, value)
                }

                is Int -> {
                    mDatsStore.putInt(key, value)
                }

                is String -> {
                    mDatsStore.putString(key, value)
                }

                is Float -> {
                    mDatsStore.putFloat(key, value)
                }


            }
        }
    }

    /**
     * 取出数据
     */
    fun <T> getData(key: String, defaultValue: T): T {
        val data = when (defaultValue) {
            is Int -> {
                mDatsStore.getInt(key, defaultValue)
            }

            is String -> {
                mDatsStore.getString(key, defaultValue)
            }

            is Long -> {
                mDatsStore.getLong(key, defaultValue)
            }

            is Double -> {
                mDatsStore.getDouble(key, defaultValue)
            }

            is Float -> {
                mDatsStore.getFloat(key, defaultValue)
            }

            is Boolean -> {
                mDatsStore.getBoolean(key, defaultValue)
            }

            else -> {
                throw IllegalArgumentException("This type cannot be saved to the Data Store")
            }
        }
        return data as T
    }

    fun clear() = runBlocking {
        mDatsStore.edit { it.clear() }
    }


    /**
     * 存放String数据
     */
    private suspend fun DataStore<Preferences>.putString(key: String, value: String) {
        edit {
            it[stringPreferencesKey(key)] = value
        }
    }

    /**
     * 存放Int数据
     */
    private suspend fun DataStore<Preferences>.putInt(key: String, value: Int) {
        edit {
            it[intPreferencesKey(key)] = value
        }
    }

    /**
     * 存放Long数据
     */
    private suspend fun DataStore<Preferences>.putLong(key: String, value: Long) {
        edit {
            it[longPreferencesKey(key)] = value
        }
    }

    /**
     * 存放Float数据
     */
    private suspend fun DataStore<Preferences>.putFloat(key: String, value: Float) {
        edit {
            it[floatPreferencesKey(key)] = value
        }
    }

    /**
     * 存放Double数据
     */
    private suspend fun DataStore<Preferences>.putDouble(key: String, value: Double) {
        edit {
            it[doublePreferencesKey(key)] = value
        }
    }

    /**
     * 存放Boolean数据
     */
    private suspend fun DataStore<Preferences>.putBoolean(key: String, value: Boolean) {
        edit {
            it[booleanPreferencesKey(key)] = value
        }
    }


    /**
     * 取出String数据
     */
    private fun DataStore<Preferences>.getString(key: String, default: String? = null): String =
        runBlocking {
            return@runBlocking data.map {
                it[stringPreferencesKey(key)] ?: default
            }.first()!!
        }

    /**
     * 取出Int数据
     */
    private fun DataStore<Preferences>.getInt(key: String, default: Int = 0): Int = runBlocking {
        return@runBlocking data.map {
            it[intPreferencesKey(key)] ?: default
        }.first()
    }

    /**
     * 取出Long数据
     */
    private fun DataStore<Preferences>.getLong(key: String, default: Long = 0): Long = runBlocking {
        return@runBlocking data.map {
            it[longPreferencesKey(key)] ?: default
        }.first()
    }

    /**
     * 取出Float数据
     */
    private fun DataStore<Preferences>.getFloat(key: String, default: Float = 0.0f): Float =
        runBlocking {
            return@runBlocking data.map {
                it[floatPreferencesKey(key)] ?: default
            }.first()
        }

    /**
     * 取出Double数据
     */
    private fun DataStore<Preferences>.getDouble(key: String, default: Double = 0.00): Double =
        runBlocking {
            return@runBlocking data.map {
                it[doublePreferencesKey(key)] ?: default
            }.first()
        }

    /**
     * 取出Boolean数据
     */
    private fun DataStore<Preferences>.getBoolean(key: String, default: Boolean = false): Boolean =
        runBlocking {
            return@runBlocking data.map {
                it[booleanPreferencesKey(key)] ?: default
            }.first()
        }
}