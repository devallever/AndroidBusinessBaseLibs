package app.allever.android.sample.demo.hen.coder.draw;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import app.allever.android.lib.core.base.AbstractActivity;
import app.allever.android.sample.demo.hen.coder.R;


public class PorterDuffXfermodeDemoActivity extends AbstractActivity implements View.OnClickListener {

    private PorterDuffXfermodeView porterDuffXfermodeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hc_activity_porter_duff_mode_demo);
        adaptStatusBar(findViewById(R.id.btnContainer));
        porterDuffXfermodeView = findViewById(R.id.porterDuffXfermodeView);
        findViewById(R.id.src).setOnClickListener(this);
        findViewById(R.id.dst).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);

        findViewById(R.id.src_in).setOnClickListener(this);
        findViewById(R.id.dst_in).setOnClickListener(this);
        findViewById(R.id.src_out).setOnClickListener(this);
        findViewById(R.id.dst_out).setOnClickListener(this);
        findViewById(R.id.src_over).setOnClickListener(this);
        findViewById(R.id.dst_over).setOnClickListener(this);
        findViewById(R.id.src_atop).setOnClickListener(this);
        findViewById(R.id.dst_atop).setOnClickListener(this);

        findViewById(R.id.darken).setOnClickListener(this);
        findViewById(R.id.lighten).setOnClickListener(this);
        findViewById(R.id.multiply).setOnClickListener(this);
        findViewById(R.id.screen).setOnClickListener(this);

        findViewById(R.id.xor).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.overlay).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        PorterDuff.Mode mode;
        int id = v.getId();
        if (id == R.id.src) {
            mode = PorterDuff.Mode.SRC;
        } else if (id == R.id.dst) {
            mode = PorterDuff.Mode.DST;
        } else if (id == R.id.clear) {
            mode = PorterDuff.Mode.CLEAR;
        } else if (id == R.id.src_in) {
            mode = PorterDuff.Mode.SRC_IN;
        } else if (id == R.id.dst_in) {
            mode = PorterDuff.Mode.DST_IN;
        } else if (id == R.id.src_out) {
            mode = PorterDuff.Mode.SRC_OUT;
        } else if (id == R.id.dst_out) {
            mode = PorterDuff.Mode.DST_OUT;
        } else if (id == R.id.src_over) {
            mode = PorterDuff.Mode.SRC_OVER;
        } else if (id == R.id.dst_over) {
            mode = PorterDuff.Mode.DST_OVER;
        } else if (id == R.id.src_atop) {
            mode = PorterDuff.Mode.SRC_ATOP;
        } else if (id == R.id.dst_atop) {
            mode = PorterDuff.Mode.DST_ATOP;
        } else if (id == R.id.darken) {
            mode = PorterDuff.Mode.DARKEN;
        } else if (id == R.id.lighten) {
            mode = PorterDuff.Mode.LIGHTEN;
        } else if (id == R.id.multiply) {
            mode = PorterDuff.Mode.MULTIPLY;
        } else if (id == R.id.screen) {
            mode = PorterDuff.Mode.SCREEN;
        } else if (id == R.id.xor) {
            mode = PorterDuff.Mode.XOR;
        } else if (id == R.id.add) {
            mode = PorterDuff.Mode.ADD;
        } else if (id == R.id.overlay) {
            mode = PorterDuff.Mode.OVERLAY;
        } else {
            mode = PorterDuff.Mode.MULTIPLY;
        }

        porterDuffXfermodeView.setMode(mode);
    }
}
