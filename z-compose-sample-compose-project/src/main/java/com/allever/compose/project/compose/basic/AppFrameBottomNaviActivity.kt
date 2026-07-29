package com.allever.compose.project.compose.basic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.compose.BaseComposeActivity
import app.allever.android.lib.core.compose.base.AbstractComposeActivity
import app.allever.android.lib.common.compose.theme.ComposeProjectTheme
import com.allever.compose.core.ui.widget.BottomNavigationView
import com.allever.compose.core.ui.widget.TabEntity
import com.allever.compose.core.ui.widget.ViewPager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import z.compose.app.allever.android.sample.compose.project.R

class AppFrameBottomNaviActivity : BaseComposeActivity() {
    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun ContentPage() {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            //顶部状态栏，视频全面屏
            Box(modifier = Modifier.fillMaxWidth())

            //中部主界面
            val pageState = rememberPagerState(0)
            var tabIndexState by remember {
                mutableIntStateOf(0)
            }

            ViewPager(count = 3,
                pagerState = pageState,
                modifier = Modifier
                    .weight(1f),
                onPageChanged = {
                    tabIndexState = it
                }) {
                when (it) {
                    0 -> {
                        Text(text = "FirstPage", modifier = Modifier.fillMaxSize())
                    }

                    1 -> {
                        Text(text = "SecondPage", modifier = Modifier.fillMaxSize())
                    }

                    2 -> {
                        Text(text = "ThirdPage", modifier = Modifier.fillMaxSize())
                    }
                }
            }

            val tabEntityList = mutableListOf<TabEntity>().apply {
                add(TabEntity("Tab1", R.drawable.zcp_ic_menu, R.drawable.zcp_ic_menu))
                add(TabEntity("Tab2", R.drawable.zcp_ic_menu, R.drawable.zcp_ic_menu))
                add(TabEntity("Tab3", R.drawable.zcp_ic_menu, R.drawable.zcp_ic_menu))
            }
            BottomNavigationView(tabEntityList, tabIndexState) {
                tabIndexState = it
                lifecycleScope.launch {
                    pageState.scrollToPage(it)
                }
            }
        }
    }
}



