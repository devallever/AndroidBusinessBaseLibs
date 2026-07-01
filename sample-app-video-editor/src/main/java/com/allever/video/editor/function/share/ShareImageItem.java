package com.allever.video.editor.function.share;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allever.video.editor.R;


/**
 * 这个是分享界面GridView的单个ITEM
 */
public class ShareImageItem extends RelativeLayout {
    private ShareImageItemData mItemData;
    private ImageView mIcon;
    private TextView mLabel;

    public ShareImageItem(Context context) {
        super(context);
        initView();
        // TODO Auto-generated constructor stub
    }

    public ShareImageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        // TODO Auto-generated constructor stub
    }

    public ShareImageItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
        // TODO Auto-generated constructor stub
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.share_image_layout_item, this, true);
        mIcon = (ImageView) findViewById(R.id.share_icon);
        mLabel = (TextView) findViewById(R.id.share_label);
    }

    public ShareImageItemData getItemData() {
        return mItemData;
    }

    /**
     * @param item 数据体
     */
    public void setItemData(ShareImageItemData item) {
        this.mItemData = item;
    }

    /**
     * 用于ArrayAdapter中getView的缓存
     */
    public static class ViewHolder {
        public ImageView icon;
        public TextView label;
    }

    public ImageView getmIcon() {
        return mIcon;
    }

    public TextView getmLabel() {
        return mLabel;
    }

    /**
     * 保存显示数据体的类
     */
    public static class ShareImageItemData {
        public ShareImageItemData(String pkgName, String activityName, Drawable icon, String label) {
            this(pkgName, activityName, icon, label, false);
        }

        public ShareImageItemData(String pkgName, String activityName, Drawable icon, String label, boolean more) {
            this.mPkgName = pkgName;
            this.mActivityName = activityName;
            this.mIcon = icon;
            this.mLabel = label;
            this.mMore = more;
        }

        private String mPkgName;
        private String mActivityName;
        private String mLabel;
        private Drawable mIcon;
        private boolean mMore = false;

        public String getmPkgName() {
            return mPkgName;
        }

        public String getmActivityName() {
            return mActivityName;
        }

        public String getmLabel() {
            return mLabel;
        }

        public Drawable getmIcon() {
            return mIcon;
        }

        public boolean isMore() {
            return mMore;
        }
    }
}
