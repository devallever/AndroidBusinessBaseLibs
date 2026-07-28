package app.allever.android.sample.demo.hen.coder.draw;

import android.os.Bundle;

import androidx.annotation.Nullable;

import app.allever.android.lib.core.base.AbstractActivity;
import app.allever.android.sample.demo.hen.coder.R;


public class CustomDrawActivity extends AbstractActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_activity_custom_draw);
        adaptStatusBar(findViewById(R.id.myCustomView));
    }
}
