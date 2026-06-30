package app.android.allever.gp.quick.project.vm

import androidx.fragment.app.Fragment
import app.allever.android.lib.core.app.App
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.R
import app.android.allever.gp.quick.project.ui.HistoryFragment
import app.android.allever.gp.quick.project.ui.SpeedTestFragment
import app.android.allever.gp.quick.project.ui.TabEntity
import app.android.allever.gp.quick.project.ui.ToolsFragment
import com.flyco.tablayout.listener.CustomTabEntity

/**
 *@Description
 *@author: zq
 *@date: 2024/1/9
 */
class HomeViewModel : BaseViewModel() {
    val fragmentList = mutableListOf<Fragment>().apply {
        add(SpeedTestFragment())
        add(HistoryFragment())
        add(ToolsFragment())
    }

    private val mTitles = arrayOf(
        R.string.tab_speed_test,
        R.string.tab_history,
        R.string.tab_tools,
    )
    private val mIconUnselectIds = intArrayOf(
        R.drawable.ic_tab_speed_un_select,
        R.drawable.ic_tab_history_un_select,
        R.drawable.ic_tab_tools_unselect,
    )
    private val mIconSelectIds = intArrayOf(
        R.drawable.ic_tab_speed_selected,
        R.drawable.ic_tab_history_selected,
        R.drawable.ic_tab_tools_selected,
    )
    val tabEntities: ArrayList<CustomTabEntity> =
        arrayListOf<CustomTabEntity>().apply {
            for (i in mTitles.indices) {
                add(
                    TabEntity(
                        App.context.getString(mTitles[i]),
                        mIconSelectIds[i],
                        mIconUnselectIds[i]
                    )
                )
            }
        }


}