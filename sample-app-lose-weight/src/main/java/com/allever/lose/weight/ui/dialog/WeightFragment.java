package com.allever.lose.weight.ui.dialog;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;
import com.allever.lose.weight.R;
import com.allever.lose.weight.data.DataSource;
import com.allever.lose.weight.data.Repository;

import app.allever.android.lib.core.base.AbstractFragment;

/**
 * Created by Mac on 2018/3/7.
 */

public class WeightFragment extends AbstractFragment implements CalendarView.OnDateSelectedListener {

    CalendarView mCalendarView;
    CalendarLayout calendarLayout;
    TextView currentDate;
    LinearLayout weight;
    TextView cancel;
    TextView save;
    EditText editWeight;
    private int fetureColor;
    private DataSource mDataSource;
    private int mDay;
    private int mMonth;
    private int mYear;
    private static IWeightRecordListener mListener;

    public static void setRecordListener(IWeightRecordListener weightRecordListener) {
        mListener = weightRecordListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lw_fragment_weight, container, false);

        mCalendarView = view.findViewById(R.id.calendarView);
        calendarLayout = view.findViewById(R.id.calendarLayout);
        currentDate = view.findViewById(R.id.current_date);
        weight = view.findViewById(R.id.weight);
        cancel = view.findViewById(R.id.cancel);
        save = view.findViewById(R.id.save);
        editWeight = view.findViewById(R.id.edit_weight);

        cancel.setOnClickListener(v -> {
            requireActivity().finish();
        });
        save.setOnClickListener(v -> {
            if (mListener != null){
                String valueStr = editWeight.getText().toString();
                if (valueStr.isEmpty()) valueStr = "0";
                double weight = Double.valueOf(valueStr);
                if (weight < 0){
                    Toast.makeText(requireActivity(), requireActivity().getResources().getString(R.string.weight_not_allow), Toast.LENGTH_SHORT).show();
                    return;
                }
                mListener.onSaveClick(weight, mYear, mMonth, mDay);
                requireActivity().finish();
            }
        });


        fetureColor = Color.parseColor("#d0d0d0");
        mDataSource = Repository.getInstance();
        setDate();
        return view;
    }

    private void setDate() {
        currentDate.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
        mCalendarView.setOnDateSelectedListener(this);
        Calendar calendar = mCalendarView.getSelectedCalendar();
        if (calendar.getDay() > mCalendarView.getCurDay()) {
            mCalendarView.setTextColor(fetureColor, fetureColor, fetureColor, fetureColor, fetureColor);
        }
        editWeight.setText(String.valueOf(mDataSource.getHistoryWeight(mCalendarView.getCurYear(), mCalendarView.getCurMonth(), mCalendarView.getCurDay())));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDateSelected(Calendar calendar, boolean isClick) {
        //TODO 格式化日期
        currentDate.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
        mDay = calendar.getDay();
        mMonth = calendar.getMonth();
        mYear = calendar.getYear();
        editWeight.setText(String.valueOf(mDataSource.getHistoryWeight(mYear, mMonth, mDay)));
    }

    public interface IWeightRecordListener{
        void onSaveClick(double weight, int year, int month, int day);
    }
}
