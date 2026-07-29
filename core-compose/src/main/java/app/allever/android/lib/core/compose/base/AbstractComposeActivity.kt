package app.allever.android.lib.core.compose.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import app.allever.android.lib.core.base.AbstractSwipeBackActivity

abstract class AbstractComposeActivity: AbstractSwipeBackActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportEnableEdgeToEdge()) {
            enableEdgeToEdge()
        }
    }

    protected open fun supportEnableEdgeToEdge(): Boolean {
        return true
    }
}