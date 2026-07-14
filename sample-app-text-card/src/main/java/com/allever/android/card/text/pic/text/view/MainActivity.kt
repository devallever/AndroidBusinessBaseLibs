package com.allever.android.card.text.pic.text.view

import android.os.Bundle
import app.allever.android.lib.core.base.AbstractActivity
import com.allever.android.card.text.pic.text.util.ActivityHelper

class MainActivity : AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHelper.startActivity(this, EditActivity::class.java)
        finish()
    }
}