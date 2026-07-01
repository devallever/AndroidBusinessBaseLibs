package com.allever.video.editor.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allever.video.editor.R;
import com.allever.video.editor.utils.FontUtil;


public class SettingItem extends RelativeLayout {

    TextView mStatus;
    ImageView mSettingIv;
    ImageView mSettingIvRight;
    SettingCheck mSettingCheck;
    TextView mSettingTitle;
    TextView mSettingDesc;
    ImageView mArrowRight;
    RelativeLayout mRightLayout;
    View mRedDot;

    private boolean mCheckAble;

    public SettingItem(Context context) {
        super(context);
        init(context, null);
    }

    public SettingItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initView();
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItem);
            boolean isCheck = ta.getBoolean(R.styleable.SettingItem_check, false);
            boolean isArrow = ta.getBoolean(R.styleable.SettingItem_isshowrightarrow, false);
            int titleId = ta.getResourceId(R.styleable.SettingItem_settingitemtitle, -1);
            int descId = ta.getResourceId(R.styleable.SettingItem_desc, -1);
            Drawable iconLeft = ta.getDrawable(R.styleable.SettingItem_lefticon);
            Drawable iconRight = ta.getDrawable(R.styleable.SettingItem_righticon);
            int titleColorId = ta.getResourceId(R.styleable.SettingItem_settingitemtitlecolor, -1);
            int descColorId = ta.getResourceId(R.styleable.SettingItem_settingitemdesccolor, -1);
            mCheckAble = ta.getBoolean(R.styleable.SettingItem_checkable, false);
            inflate(getContext(), R.layout.setting_item, this);
            if (iconLeft != null) {
                mSettingIv.setImageDrawable(iconLeft);
            } else {
                mSettingIv.setVisibility(View.GONE);
            }
            if (iconRight != null) {
                mSettingIvRight.setImageDrawable(iconRight);
                int color = getResources().getColor(R.color.default_btn_color_filter);
                mSettingIvRight.setColorFilter(color);
            } else {
                mSettingIvRight.setVisibility(View.GONE);
            }
            if (titleId != -1) {
                mSettingTitle.setText(titleId);
            } else {
                mSettingTitle.setVisibility(View.GONE);
            }
            if (titleColorId != -1) {
                mSettingTitle.setTextColor(getResources().getColor(titleColorId));
            }
            if (descId != -1) {
                mSettingDesc.setText(descId);
            } else {
                mSettingDesc.setVisibility(View.GONE);
            }
            if (descColorId != -1) {
                mSettingDesc.setTextColor(getResources().getColor(descColorId));
            }
            if (mCheckAble) {
                mSettingCheck.setVisibility(View.VISIBLE);
            } else {
                mSettingCheck.setVisibility(View.GONE);
            }
            if (!isArrow && !mCheckAble) {
                mRightLayout.setVisibility(View.GONE);
            } else {
                mRightLayout.setVisibility(View.VISIBLE);
            }
            FontUtil.setCustomFont(mSettingTitle, mSettingDesc);
            setCheck(isCheck);
            setArrowRight(isArrow);
            ta.recycle();
        }
        setClickable(true);
    }

    private void initView() {
        mStatus = (TextView) findViewById(R.id.status);
        mSettingIv = (ImageView) findViewById(R.id.setting_iv);
        mSettingIvRight = (ImageView) findViewById(R.id.setting_iv_right);
        mSettingCheck = (SettingCheck) findViewById(R.id.setting_check);
        mSettingTitle = (TextView) findViewById(R.id.setting_title);
        mSettingDesc = (TextView) findViewById(R.id.setting_desc);
        mArrowRight = (ImageView) findViewById(R.id.arrow_right);
        mRightLayout = (RelativeLayout) findViewById(R.id.right_layout);
        mRedDot = findViewById(R.id.red_dot);
    }

    public void setCheck(boolean check) {
        if (mCheckAble && mSettingCheck != null) {
            mSettingCheck.setCheck(check);
        }
    }
    public void setIvRight(boolean isShow) {
        if (isShow) {
            mSettingIvRight.setVisibility(View.VISIBLE);
        } else {
            mSettingIvRight.setVisibility(View.GONE);
        }
    }

    public void setIvDrawable(Drawable drawable) {
        mSettingIv.setImageDrawable(drawable);
    }

    public void setArrowRight(boolean isShow) {
        if (isShow) {
            mArrowRight.setVisibility(View.VISIBLE);
            setCheckAble(false);
        } else {
            mArrowRight.setVisibility(View.GONE);
        }
    }

    public void setDesc(int resId) {
        if (!mSettingDesc.isShown()) {
            mSettingDesc.setVisibility(View.VISIBLE);
        }
        mSettingDesc.setText(resId);
    }

    public void setStatus(String status) {
        if (!TextUtils.isEmpty(status)) {
            mStatus.setText(status);
        }
    }

    public void setShowRedDot(boolean show) {
        if (mRedDot != null) {
            mRedDot.setVisibility(show ? VISIBLE : GONE);
        }
    }

    public boolean isCheck() {
        return mSettingCheck.isCheck();
    }

    public void setCheckAble(boolean checkAble) {
        if (mCheckAble != checkAble) {
            mCheckAble = checkAble;
            mSettingCheck.setVisibility(mCheckAble ? View.VISIBLE : View.GONE);
        }
    }
}
