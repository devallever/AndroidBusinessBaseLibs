package com.allever.daymatter.ui.dialog;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.allever.daymatter.R;

/**
 * Created by Allever on 18/5/28.
 */

public class SortDialog extends DialogFragment {

    private static OptionListener mOptionListener;


    public static SortDialog newInsance(OptionListener optionListener) {
        SortDialog sortDialog = new SortDialog();
        sortDialog.setCancelable(true);
        mOptionListener = optionListener;
        return sortDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dm_dialog_sort, container, false);

        findView(view);

        return view;
    }

    private void findView(View view) {
        view.findViewById(R.id.id_dialog_sort_rb_life).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, getString(R.string.dm_sort_life), 1);
            }
        });
        view.findViewById(R.id.id_dialog_sort_rb_work).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, getString(R.string.dm_sort_work), 2);
            }
        });
        view.findViewById(R.id.id_dialog_sort_rb_memory_day).setOnClickListener(v -> {
            if (mOptionListener != null) {
                mOptionListener.onItemClick(this, getString(R.string.dm_sort_memory_day), 3);
            }
        });
        view.findViewById(R.id.id_dialog_sort_tv_cancel).setOnClickListener(v -> {
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
        void onItemClick(DialogFragment dialog, String sortName, int sortId);

        void onCancel(DialogFragment dialog);
    }

}
