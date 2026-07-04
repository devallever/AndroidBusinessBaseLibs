package com.allever.lose.weight.ui.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.allever.lose.weight.R;
import com.allever.lose.weight.bean.DayInfoBean;

import java.util.List;


public class TrainItemAdapter extends BaseQuickAdapter<DayInfoBean, BaseViewHolder> {
    public TrainItemAdapter(@Nullable List<DayInfoBean> data) {
        super(R.layout.lw_item_train, data);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void convert(BaseViewHolder helper, DayInfoBean item) {
        if (item == null){
            return;
        }
        CircleProgressBar circleProgressBar = helper.getView(R.id.id_item_train_progress_bar_train);
        circleProgressBar.setStyle(2);//SOLID_LINE
        CardView mCardView = helper.getView(R.id.card_view);
        helper.setText(R.id.tv_day, item.getTitle());
        String trainDetail = getContext().getString(R.string.lw_train_detail, String.valueOf(item.getLevelCount()), String.valueOf(item.getTrainedCount()));
        helper.setText(R.id.tvTrainInfoDetail,  trainDetail);
        if (item.isCurrentDay()) {
            mCardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.lw_theme_color));
            helper.setTextColor(R.id.tv_day, Color.parseColor("#FFFFFF"));
            helper.setTextColor(R.id.tvTrainInfoDetail, Color.parseColor("#FFFFFF"));
            circleProgressBar.setProgressStartColor(getContext().getResources().getColor(R.color.white));
            circleProgressBar.setProgressEndColor(getContext().getResources().getColor(R.color.white));
            helper.setTextColor(R.id.id_item_train_tv_progress, getContext().getResources().getColor(R.color.white));
        } else {
            mCardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.white));
            helper.setTextColor(R.id.tv_day, getContext().getResources().getColor(R.color.lw_theme_color));
            helper.setTextColor(R.id.tvTrainInfoDetail, getContext().getResources().getColor(R.color.lw_theme_color_weak));
            circleProgressBar.setProgressStartColor(getContext().getResources().getColor(R.color.lw_green_200));
            circleProgressBar.setProgressEndColor(getContext().getResources().getColor(R.color.lw_green_200));
            helper.setTextColor(R.id.id_item_train_tv_progress, getContext().getResources().getColor(R.color.lw_green_200));
        }



        if (item.isFinish()){
            //设置完成标志
            helper.setVisible(R.id.id_item_train_iv_finish, true);
            //其他隐藏
            helper.setVisible(R.id.id_item_train_rl_progress_bar_container, false);
            helper.setVisible(R.id.id_item_train_iv_rest, false);
        }else {
            helper.setVisible(R.id.id_item_train_iv_finish, false);
            if (item.getLevelCount() == 0){
                //无动作 休息
                helper.setVisible(R.id.id_item_train_rl_progress_bar_container, false);
                helper.setVisible(R.id.id_item_train_iv_rest, true);
            }else {
                //有动作
                helper.setVisible(R.id.id_item_train_rl_progress_bar_container, true);
                helper.setVisible(R.id.id_item_train_iv_rest, false);

                int percent = Math.round(((item.getTrainedCount()/(float)item.getLevelCount()) * 100));
                helper.setText(R.id.id_item_train_tv_progress, percent+"%");
                circleProgressBar.setMax(item.getLevelCount());
                circleProgressBar.setProgress(item.getTrainedCount());
            }
        }
    }
}
