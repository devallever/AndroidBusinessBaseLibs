package app.allever.android.ai.qr.scanner.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.StringRes;

import com.android.absbase.App;
import com.google.zxing.client.result.ParsedResultType;

public class ShareItem implements Parcelable {

    public static final int TYPE_CLIPBOARD = 1;
    public static final int TYPE_URL = 2;
    public static final int TYPE_WIFI = 3;
    public static final int TYPE_TEXT = 4;
    public static final int TYPE_CONTACT = 5;
    public static final int TYPE_PHONE = 6;
    public static final int TYPE_EMAIL = 7;
    public static final int TYPE_SMS = 8;
    public static final int TYPE_LOCATION = 9;
    public static final int TYPE_BARCODE = 10;

    public static final int TYPE_INSTAGRAM = 11;
    public static final int TYPE_FACEBOOK = 12;
    public static final int TYPE_WHATSAPP = 13;
    public static final int TYPE_YOUTUBE = 14;
    public static final int TYPE_TWITTER = 15;
    public static final int TYPE_SPOTIFY = 16;
    public static final int TYPE_VIBER = 17;

    private int resId;
    private String title;
    private int type;
    private ParsedResultType parsedResultType;
    private boolean iconHasBackground;
    private boolean lock;

    public ShareItem(){}

    public ShareItem(int resId, String title, int type, ParsedResultType parsedResultType, boolean lock){
        this.resId = resId;
        this.title = title;
        this.type = type;
        this.parsedResultType = parsedResultType;
        this.lock = lock;
    }

    public ShareItem(int resId, @StringRes int titleResId, int type, ParsedResultType parsedResultType, boolean lock, boolean iconHasBackground) {
        this.resId = resId;
        this.title = App.getContext().getResources().getString(titleResId);
        this.type = type;
        this.parsedResultType = parsedResultType;
        this.iconHasBackground = iconHasBackground;
        this.lock = lock;
    }


    protected ShareItem(Parcel in) {
        resId = in.readInt();
        title = in.readString();
        type = in.readInt();
    }

    public static final Creator<ShareItem> CREATOR;

    static {
        CREATOR = new Creator<ShareItem>() {
            @Override
            public ShareItem createFromParcel(Parcel in) {
                return new ShareItem(in);
            }

            @Override
            public ShareItem[] newArray(int size) {
                return new ShareItem[size];
            }
        };
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ParsedResultType getParsedResultType() {
        return parsedResultType;
    }

    public void setParsedResultType(ParsedResultType parsedResultType) {
        this.parsedResultType = parsedResultType;
    }

    public boolean isIconHasBackground() {
        return iconHasBackground;
    }

    public void setIconHasBackground(boolean iconHasBackground) {
        this.iconHasBackground = iconHasBackground;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(resId);
        dest.writeString(title);
        dest.writeInt(type);
    }
}
