package com.allever.video.editor.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.allever.video.editor.R;

/**
 *
 */

public class CircleSeekBar extends CustomNumSeekBar {
    public CircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setProgressTumb(getResources().getDrawable(R.drawable.image_edit_seekbar_progress));
//        setProgressBgTumb(getResources().getDrawable(R.drawable.image_edit_seekbar_progress_bg));
//        setTextColor(getResources().getColor(R.color.image_edit_seekbar_text_color));
        setTextColor(getResources().getColor(R.color.filter_original_tv_bg));
        setProgressTumb(getResources().getDrawable(R.color.edit_seekbar_select_color));
        setProgressBgTumb(getResources().getDrawable(R.color.edit_seekbar_unselect_color));
        setNumBgTumb(getResources().getDrawable(R.drawable.seek_bar_bg));
        setTouchTumb(null);
    }

    @Override
    protected int getCustomMaxTextWidth() {
        return getResources().getDimensionPixelSize(R.dimen.image_edit_seekbar_num_background_width);
    }
}
