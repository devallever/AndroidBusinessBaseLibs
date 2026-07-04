package com.allever.lose.weight.ui.adapter;

import android.widget.Switch;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.allever.lose.weight.R;
import com.allever.lose.weight.data.Config;
import com.allever.lose.weight.util.DateUtil;

import java.util.List;

/**
 * Created by Mac on 18/3/20.
 */

public class RemindRecAdapter extends BaseQuickAdapter<Config.Reminder, BaseViewHolder> {
    public RemindRecAdapter(List<Config.Reminder> reminderList){
        super(R.layout.lw_fragment_reminder_item,reminderList);
    }
    @Override
    protected void convert(BaseViewHolder holder, Config.Reminder item) {
        if (item == null){
            return;
        }
        String remindTime = DateUtil.formatHourMinute(item.getHour(), item.getMinute());
        holder.setText(R.id.tv_time, remindTime);
        Switch switchView = (Switch) holder.getView(R.id.reminder_switch);
        switchView.setChecked(item.isRemindSwitch());
        StringBuilder repeatStr = new StringBuilder();
        if (item.isMonRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_monday) + " ");
        }
        if (item.isTueRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_tuesday) + " ");
        }
        if (item.isWebRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_wednesday) + " ");
        }
        if (item.isThurRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_thursday) + " ");
        }
        if (item.isFriRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_friday) + " ");
        }
        if (item.isSatRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_saturday) + " ");
        }
        if (item.isSunRepeat()) {
            repeatStr.append(getContext().getResources().getString(R.string.lw_sunday) + " ");
        }
        holder.setText(R.id.tv_weekly, repeatStr.toString());

//        holder.addOnClickListener(R.id.reminder_switch);
//        holder.addOnClickListener(R.id.delete);
//        holder.addOnClickListener(R.id.id_item_remind_tv_repeat);

    }
}
