package com.clean.wood

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import com.clean.wood.data.AdManager
import com.clean.wood.ui.fragments.BaseFragment
import com.clean.wood.ui.fragments.MainFragment
import com.clean.wood.utils.Constant
import java.util.Stack

class MainActivity : AppCompatActivity() {
    companion object {
        private const val STACK_CACHED_KEY = "bundle_cached_stack_key"
    }

    private var fragmentStack: Stack<String> = Stack()
    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            val currentFragment: BaseFragment? = getTopFragment()
            if (currentFragment?.backPressedEnable() == true) {
                currentFragment.onBackPressed()
                return
            }
            pop()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
        setContentView(R.layout.wood_activity_main)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        if (savedInstanceState == null) {
            push(MainFragment())
        }
    }

    override fun onStop() {
        super.onStop()
        getTopFragment()?.changeHideState(true)
    }

    override fun onStart() {
        super.onStart()
        if (!WoodApp.alreadyInBackground) {
            getTopFragment()?.changeHideState(false)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val cachedStackString = savedInstanceState.getString(STACK_CACHED_KEY)
        if (cachedStackString?.isNotEmpty() == true) {
            cachedStackString.split(",").forEach {
                fragmentStack.push(it)
            }
            refreshBackPressedState()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STACK_CACHED_KEY, fragmentStack.joinToString(","))
        super.onSaveInstanceState(outState)
    }

    fun push(fragment: BaseFragment) {
        val targetKey = fragment.stackKey()
        var currentFragment: BaseFragment? = getTopFragment()
        if (targetKey == currentFragment?.stackKey()) {
            return
        }
        var targetFragment = supportFragmentManager.findFragmentByTag(targetKey)
        supportFragmentManager.beginTransaction().apply {
            if (targetFragment != null) {
                currentFragment = null
                while (!fragmentStack.empty()) {
                    val top = fragmentStack.pop()
                    if (top == targetKey) {
                        break
                    }
                    supportFragmentManager.findFragmentByTag(top)?.let { remove(it) }
                }
            }
            if (targetFragment == null) {
                targetFragment = fragment
                add(R.id.main_container, fragment, targetKey)
            }
            show(targetFragment!!)
            currentFragment?.let {
                it.changeHideState(true)
                hide(it)
            }
        }.commitAllowingStateLoss()
        fragmentStack.push(targetKey)
        refreshBackPressedState()
    }

    fun pop() {
        if (fragmentStack.size > 1) {
            val currentFragment: BaseFragment? = getTopFragment()
            fragmentStack.pop()
            val nextPageKey = fragmentStack.peek()
            val nextFragment: BaseFragment? =
                supportFragmentManager.findFragmentByTag(nextPageKey) as BaseFragment?
            supportFragmentManager.beginTransaction().apply {
                currentFragment?.let {
                    it.changeHideState(true)
                    remove(it)
                }
                nextFragment?.let {
                    show(it)
                    it.changeHideState(false)
                }
            }.commitAllowingStateLoss()
        } else {
            finish()
        }
        refreshBackPressedState()
    }

    private fun getTopFragment(): BaseFragment? {
        if (fragmentStack.isNotEmpty()) {
            val currentTag = fragmentStack.peek()
            return supportFragmentManager.findFragmentByTag(currentTag) as BaseFragment?
        }
        return null
    }

    fun getTopFragmentKey(): String? {
        if (fragmentStack.isNotEmpty()) {
            val currentTag = fragmentStack.peek()
            return currentTag
        }
        return null
    }

    fun refreshBackPressedState() {
        onBackPressedCallback.isEnabled =
            fragmentStack.size > 1 || getTopFragment()?.backPressedEnable() == true
    }
}