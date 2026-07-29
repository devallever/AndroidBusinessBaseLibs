package app.allever.android.lib.core.compose.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import app.allever.android.lib.core.base.AbstractActivity

abstract class AbstractComposeActivity: AbstractActivity() {
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