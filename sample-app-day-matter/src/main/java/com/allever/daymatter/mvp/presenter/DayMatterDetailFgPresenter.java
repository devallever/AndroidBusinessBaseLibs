package com.allever.daymatter.mvp.presenter;

import android.content.Context;

import com.allever.daymatter.data.Event;
import com.allever.daymatter.utils.DateUtils;
import com.allever.daymatter.R;
import com.allever.daymatter.mvp.BasePresenter;
import com.allever.daymatter.mvp.view.IDayMatterDetailFgView;

/**
 * Created by Allever on 18/5/22.
 */

public class DayMatterDetailFgPresenter extends BasePresenter<IDayMatterDetailFgView> {

    public void getDayMatterData(Context context, int id) {

        Event event = mDataSource.getEvent(id);

        if (event == null){
            return;
        }

        IDayMatterDetailFgView iView = mViewRef.get();
        String title;
        int leftDay = DateUtils.calDistanceDayCount(event.getYear(), event.getMonth()-1, event.getDay());

        //如果剩余天数大于等于0， 表示还没到
        if (leftDay >= 0){
            title = context.getString(R.string.dm_distance) + event.getTitle() + context.getString(R.string.dm_has);
            iView.setTvTitle(title);
            iView.setTvLeftDay(leftDay + "");
            iView.setTitleBackgroundColor(context.getResources().getColor(R.color.dm_colorDefault));
        }else {
            //如果剩余天数小于0， 表示已经过了
            title = event.getTitle() +context.getString(R.string.dm_already);
            iView.setTvTitle(title);
            iView.setTvLeftDay((-1 * leftDay) + "");
            iView.setTitleBackgroundColor(context.getResources().getColor(R.color.dm_orange_500));
        }
        String date = DateUtils.formatDate_Y_M_D_WEEK_New(context,
                event.getYear(),
                event.getMonth()-1,
                event.getDay(),
                event.getWeekDay());
        iView.setTvDate(context.getString(R.string.dm_target_date) + date);
    }
}
