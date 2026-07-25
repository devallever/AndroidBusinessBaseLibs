package app.allever.android.ai.qr.scanner.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allever.app.qr.code.scaner.R;

public class ShareItemInput extends LinearLayout {



    private static final int INPUT_TYPE_TEXT = 0;
    private static final int INPUT_TYPE_NUMBER = 1;


    private Context mContext;

    private TextView mTvTitle;
    private TextView mTvTips;
    private EditText mEtContent;

    private String mTitle = null;
    private String mHint = null;
    private int mMaxLength = 300;
    private int mInputType = INPUT_TYPE_TEXT;


    public ShareItemInput(Context context) {
        this(context, null);
    }

    public ShareItemInput(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShareItemInput(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.ShareItemInput);

        mTitle = typedArray.getString(R.styleable.ShareItemInput_siiTitle);

        mHint = typedArray.getString(R.styleable.ShareItemInput_siiHint);

        mMaxLength = typedArray.getInt(R.styleable.ShareItemInput_siiMaxContentLength, 300);

        mInputType = typedArray.getInt(R.styleable.ShareItemInput_siiInputType, INPUT_TYPE_TEXT);

        //获取资源后要及时回收
        typedArray.recycle();

        initView();

        setListener();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.share_item_input,this,true);
        mEtContent = view.findViewById(R.id.id_et_content);
        mTvTitle = view.findViewById(R.id.id_tv_title);
        mTvTips = view.findViewById(R.id.id_tv_length_tip);

        //设置字体
//        Typeface robotoBold = Typeface.createFromAsset(mContext.getAssets(), "roboto_bold.ttf");
//        Typeface robotoRegular =  Typeface.createFromAsset(mContext.getAssets(), "roboto_regular.ttf");
//        mTvTitle.setTypeface(robotoBold);
//        mTvTips.setTypeface(robotoRegular);
//        mEtContent.setTypeface(robotoRegular);

        //设置显示
        mTvTitle.setText(mTitle);
        mTvTips.setText(mEtContent.getText().length() + "/" + mMaxLength);
        mEtContent.setHint(mHint);
        mEtContent.setHintTextColor(mContext.getResources().getColor(R.color.share_item_input_hint));
        //设置最大长度
        mEtContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mMaxLength)});

        //设置输入类型
        switch (mInputType){
            case INPUT_TYPE_TEXT:
                mEtContent.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case INPUT_TYPE_NUMBER:
                mEtContent.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            default:
                mEtContent.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
        }
    }

    private int contentLength;
    private void setListener() {
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                contentLength = mEtContent.getText().toString().length();
                mTvTips.setText(contentLength + "/" + mMaxLength);

                if (contentLength > mMaxLength){
                    mTvTips.setTextColor(getResources().getColor(R.color.red));
                }else {
                    mTvTips.setTextColor(getResources().getColor(R.color.share_item_tv_input_index));
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public void setMax(int max){
        mMaxLength = max;
    }

    public void setTitle(String title){
        mTvTitle.setText(title);
    }

    public void setHint(String hint){
        mHint = hint;
    }

    public String getText(){
        return mEtContent.getText().toString();
    }

    public void setText(CharSequence text){
        mEtContent.setText(text);
    }

    public void setSelection(int position){
        mEtContent.setSelection(position);
    }
}
