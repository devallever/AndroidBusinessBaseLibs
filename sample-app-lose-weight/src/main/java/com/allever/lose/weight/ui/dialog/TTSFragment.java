package com.allever.lose.weight.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.allever.lose.weight.R;
import com.allever.lose.weight.util.Util;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by Mac on 2018/3/7.
 */

public class TTSFragment extends SupportFragment {

    TextView mDown;
    TextView mSetting;

    public static TTSFragment newInstance() {
        return new TTSFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lw_dialog_tts, container, false);
        mDown = view.findViewById(R.id.down_tts);
        mSetting = view.findViewById(R.id.set_tts);
        mDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.searchFromMarket(_mActivity,"text to speech");
            }
        });
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            }
        });
        return view;
    }
}
