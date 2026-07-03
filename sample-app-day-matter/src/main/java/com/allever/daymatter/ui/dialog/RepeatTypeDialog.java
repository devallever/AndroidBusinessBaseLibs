package com.allever.daymatter.ui.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.allever.daymatter.R;
import com.allever.daymatter.utils.Constants;


/**
 * Created by Allever on 18/5/28.
 */

public class RepeatTypeDialog extends DialogFragment {

    private static OptionListener mOptionListener;

    public static RepeatTypeDialog newInsance(OptionListener optionListener) {
        RepeatTypeDialog repeatTypeDialog = new RepeatTypeDialog();
        repeatTypeDialog.setCancelable(true);
        mOptionListener = optionListener;
        return repeatTypeDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dm_dialog_repeat_type, container, false);
        findView(view);
        return view;
    }

    private void findView(View view) {
        view.findViewById(R.id.id_dialog_repeat_type_rb_no_repeat).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, Constants.REPEAT_TYPE_NO_REPEAT);
            }
        });
        view.findViewById( R.id.id_dialog_repeat_type_rb_per_week).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, Constants.REPEAT_TYPE_PER_WEEK);
            }
        });
        view.findViewById(R.id.id_dialog_repeat_type_rb_per_month).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, Constants.REPEAT_TYPE_PER_MONTH);
            }
        });
        view.findViewById(R.id.id_dialog_repeat_type_rb_per_year).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, Constants.REPEAT_TYPE_PER_YEAR);
            }
        });
        view.findViewById(R.id.id_dialog_repeat_type_tv_cancel).setOnClickListener(v -> {
            if (mOptionListener != null){
                mOptionListener.onCancel(this);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public interface OptionListener {
        void onItemClick(DialogFragment dialog, int repeatType);

        void onCancel(DialogFragment dialog);
    }

}
