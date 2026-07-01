//package com.allever.video.editor.function.ad;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.os.Build;
//import androidx.annotation.LayoutRes;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.view.ViewCompat;
//import android.util.AttributeSet;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.android.absbase.App;
//import com.android.absbase.ui.view.AllinoneAdView;
//import com.android.absbase.utils.DeviceUtils;
//import com.rice.balls.utils.CubicBezierInterpolator;
//import com.allever.video.editor.ConfigManager;
//import com.allever.video.editor.R;
//
///**
// *
// */
//
//public class FlowAdView extends RelativeLayout {
//
//    private RelativeLayout mTitleLayout;
//    private RelativeLayout mTitleMore;
//    private TextView mTitleName;
//    private FrameLayout mAdContent;
//    private TextView mTitlePrefix;
//
//    private @LayoutRes
////    int mAdLayoutId = R.layout.sc_layout_style_inapp2;
//    int mAdLayoutId = 0;
//    private AllinoneAdView mAllinoneAdView;
//    private boolean isFristShow = false;
//
//    public static FlowAdView newEntranceAdView(@LayoutRes int layoutId, @Nullable ViewGroup root) {
//        LayoutInflater inflater = LayoutInflater.from(App.getContext());
//        return (FlowAdView) inflater.inflate(layoutId, root);
//    }
//
//    public static FlowAdView newEntranceAdView(@Nullable ViewGroup root) {
//        LayoutInflater inflater = LayoutInflater.from(App.getContext());
//        View view = inflater.inflate(R.layout.ad_item_bar, root);
//        if (root != null && root == view) {
//            view = root.getChildAt(root.getChildCount() - 1);
//        }
//        return (FlowAdView) view;
//    }
//
//    public FlowAdView(@NonNull Context context) {
//        super(context);
//    }
//
//    public FlowAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public FlowAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public FlowAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
//
//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//
//        mTitleLayout = findViewById(R.id.title_layout);
//        mTitleName = findViewById(R.id.title_name);
//        mTitleMore = findViewById(R.id.title_more);
//        mAdContent = findViewById(R.id.ad_content);
//        mTitlePrefix = findViewById(R.id.title_prefix);
//    }
//
//    public RelativeLayout getTitleLayout() {
//        return mTitleLayout;
//    }
//
//    public LayoutParams getTitleLayoutParams() {
//        return mTitleLayout != null ? (LayoutParams) mTitleLayout.getLayoutParams() : null;
//    }
//
//    public LayoutParams getAdContentLayoutParams() {
//        return (LayoutParams) mAdContent.getLayoutParams();
//    }
//
//    public TextView getTitleName() {
//        return mTitleName;
//    }
//
//    public void setTitlePrefixVisibility(int visibility) {
//        if (mTitlePrefix != null) {
//            mTitlePrefix.setVisibility(visibility);
//        }
//    }
//
//    public void setTitleLayoutVisibility(int visibility) {
//        if (mTitleLayout != null) {
//            mTitleLayout.setVisibility(visibility);
//        }
//    }
//
//    public void setTitleMore(View view) {
//        if (mTitleMore != null) {
//            mTitleMore.removeAllViews();
//            mTitleMore.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//        }
//    }
//
//    public void setTitleLayoutMargin(int left, int right, int top, int bottom) {
//        if (mTitleLayout == null) {
//            return;
//        }
//        LayoutParams lp = (LayoutParams) mTitleLayout.getLayoutParams();
//        if (left != -1) {
//            lp.leftMargin = left;
//        }
//        if (right != -1) {
//            lp.rightMargin = right;
//        }
//        if (top != -1) {
//            lp.topMargin = top;
//        }
//        if (bottom != -1) {
//            lp.bottomMargin = bottom;
//        }
//    }
//
//    public void setAdContentMargin(int left, int right, int top, int bottom) {
//        LayoutParams lp = (LayoutParams) mAdContent.getLayoutParams();
//        if (left != -1) {
//            lp.leftMargin = left;
//        }
//        if (right != -1) {
//            lp.rightMargin = right;
//        }
//        if (top != -1) {
//            lp.topMargin = top;
//        }
//        if (bottom != -1) {
//            lp.bottomMargin = bottom;
//        }
//    }
//
//    public void setMargin(int left, int right, int top, int bottom, int middle) {
//        LayoutParams titleLayoutLp = null;
//        if (mTitleLayout != null) {
//            titleLayoutLp = (LayoutParams) mTitleLayout.getLayoutParams();
//        }
//        LayoutParams adContentLp = (LayoutParams) mAdContent.getLayoutParams();
//
//        if (left != -1) {
//            if (titleLayoutLp != null) {
//                titleLayoutLp.leftMargin = left;
//            }
//            adContentLp.leftMargin = left;
//        }
//        if (right != -1) {
//            if (titleLayoutLp != null) {
//                titleLayoutLp.rightMargin = right;
//            }
//            adContentLp.rightMargin = right;
//        }
//        if (top != -1) {
//            if (titleLayoutLp != null) {
//                titleLayoutLp.topMargin = top;
//            }
//        }
//        if (bottom != -1) {
//            adContentLp.bottomMargin = bottom;
//        }
//        if (middle != -1 && titleLayoutLp != null) {
//            titleLayoutLp.bottomMargin = middle;
//        }
//    }
//
//    public void setAdLayoutId(@LayoutRes int layoutId) {
//        this.mAdLayoutId = layoutId;
//    }
//
//    public boolean setData(@NonNull final AdItemBean bean) {
//        if (bean.hasAd()) {
//            setVisibility(View.VISIBLE);
////            mTitleLayout.setVisibility(VISIBLE);
//            isFristShow = !bean.isIsShowed();
//            bean.setIsShowed(true);
//            mAdContent.removeAllViews();
//            View adView = bean.getAdView();
//            if (adView == null) {
//                adView = bean.createAdView(mAdLayoutId);
//            }
//            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//            lp.gravity = Gravity.CENTER;
////            lp.topMargin = DeviceUtils.dip2px(getContext(), 8);
////            lp.bottomMargin = lp.topMargin;
//            mAdContent.addView(adView, lp);
//            return true;
//        } else {
//            setVisibility(View.GONE);
//            bean.setOnAdRefreshListener(new AdItemBean.OnAdRefreshListener() {
//                @Override
//                public void onAdRefresh() {
//                    setData(bean);
//                    invalidate();
//                    showLayoutAd();
//                }
//            });
//            return false;
//        }
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
////        destroy();
//    }
//
//    public void destroy() {
//        if (mAllinoneAdView != null) {
//            mAllinoneAdView.destroy();
//            mAllinoneAdView = null;
//        }
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        if (ConfigManager.INSTANCE.getNeedAd()) {
//            if (isFristShow) {
//                isFristShow = false;
//                showLayoutAd();
//            }
//        } else {
//            destroy();
//        }
//    }
//
//    private void showLayoutAd() {
//        CubicBezierInterpolator cubicBezierInterpolator = new CubicBezierInterpolator(0, 1.06, .73, 1);
//
//        ViewCompat.setTranslationX(this, DeviceUtils.getScreenWidthPx());
//        this.animate().translationX(0).setDuration(800).setInterpolator(cubicBezierInterpolator).start();
//    }
//}
