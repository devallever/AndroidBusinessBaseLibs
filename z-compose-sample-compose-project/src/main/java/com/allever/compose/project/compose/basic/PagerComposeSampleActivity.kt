package com.allever.compose.project.compose.basic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.allever.android.lib.common.compose.BaseComposeActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState

class PagerComposeSampleActivity : BaseComposeActivity() {
    override fun init() {
        initTopBar("PagerComposeSampleActivity")
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun ContentPage() {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //水平ViewPager
            LabelView("水平ViewPager HorizontalPager")
            val pagerState = rememberPagerState(0)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(count = 3, state = pagerState) { index ->
                    when (index) {
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0x80ff0000))
                            )
                        }
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0x8000ff00))
                            )
                        }
                        2 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0x800000ff))
                            )
                        }
                    }
                }
            }

            //垂直ViewPager
            LabelView("垂直ViewPager VerticalPager")
            val pagerStateV = rememberPagerState(0)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                VerticalPager(count = 3, state = pagerStateV) { index ->
                    when (index) {
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0x80ff0000))
                            )
                        }
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0x8000ff00))
                            )
                        }
                        2 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(Color(0x800000ff))
                            )
                        }
                    }
                }
            }
        }
    }
}