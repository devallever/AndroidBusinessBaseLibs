package app.allever.android.ai.qr.scanner.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.allever.app.qr.code.scaner.R;

/**
 *
 */

public class LoadingView extends FrameLayout implements View.OnTouchListener {
    private LottieAnimationView mAnimView;

    public LoadingView(Context context) {
        super(context);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAnimView = findViewById(R.id.anim_view);
        setOnTouchListener(this);
    }

    public void startLoading(){
        setVisibility(View.VISIBLE);
        mAnimView.playAnimation();
    }

    public void stopLoading(){
        setVisibility(View.GONE);
        mAnimView.cancelAnimation();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
