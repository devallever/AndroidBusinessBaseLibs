package com.allever.sticker.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import org.xm.sticker.camera.R;
import com.allever.sticker.ui.adapter.RecyclerItemClickListener;
import com.allever.sticker.ui.adapter.StickerStoreAdapter;
import com.allever.sticker.bean.StoreStickerItem;
import com.allever.sticker.util.Constant;
import com.allever.sticker.util.DialogUtil;
import com.allever.sticker.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import app.allever.android.lib.core.base.AbstractActivity;


/**
 *
 * @author Allever
 * @date 18/2/8
 */

public class StoreActivity extends AbstractActivity {
    private static final String TAG = "StoreActivity";
    private StickerStoreAdapter mStickerStoreAdapter;
    private RecyclerView mRecyclerView;
    private List<StoreStickerItem> mStoreStickerItems = new LinkedList<>();
    private StoreStickerItem mHeaderStoreItem;
    private String mStoreDir;
    private AlertDialog mProgressDialog;
    private LinearLayout mNetErrorContainer;
    private Button mBtnRetry;
    private boolean mEditing;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_store);

        EventBus.getDefault().register(this);

        mStoreDir = FileUtil.getStoreDir(this);

        mEditing = getIntent().getBooleanExtra(Constant.EXTRA_EDITOR_ING,false);

        initView();
    }

    private void initView(){
        mRecyclerView = (RecyclerView)findViewById(R.id.id_store_rv_sticker_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mStickerStoreAdapter = new StickerStoreAdapter(this, mStoreStickerItems);
        mRecyclerView.setAdapter(mStickerStoreAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String name;
                if (position == 0){
                    name = mHeaderStoreItem.getName();
                }else {
                    int realPos = position - 1;
                    name = mStoreStickerItems.get(realPos).getName();
                }
                StickerDetailActivity.startSelf(StoreActivity.this, name, mEditing);
            }
            @Override
            public void onItemLongClick(View view, int position) {}
        }));

        findViewById(R.id.id_store_iv_my_sticker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoreActivity.this, MyStickerActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.id_store_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mProgressDialog = DialogUtil.createProgressAlertDialog(this,R.string.loading,true);

        mNetErrorContainer = (LinearLayout)findViewById(R.id.id_net_error);
        mBtnRetry = (Button)findViewById(R.id.id_net_error_btn_retry);
        mBtnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNetErrorContainer.setVisibility(View.INVISIBLE);
                mProgressDialog.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    private void setHeaderView(ViewGroup parent){
        View header = LayoutInflater.from(this).inflate(R.layout.sc_item_store_sticker_header, parent, false);
        Glide.with(this).load(mHeaderStoreItem.getUrl()).into((ImageView) header);
        mStickerStoreAdapter.setHeaderView(header);
    }

    private boolean isDownloaded(String stickname){
        File stickerDir = new File( mStoreDir + "/" + stickname);
        if (stickerDir.exists()){
            if (stickerDir.list().length > 0){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStickerOption(String event){
        if (Constant.EVENT_DELETE_STICKER.equals(event) || Constant.EVENT_ADD_STICKER.equals(event)){

        }
        if (Constant.EVENT_FINISH_STORE_ACTIVITY.equals(event)){
            finish();
        }
    }

    public static void startSelf(Context context){
        startSelf(context, false);
    }

    public static void startSelf(Context context, boolean editing){
        Intent intent = new Intent(context, StoreActivity.class);
        intent.putExtra(Constant.EXTRA_EDITOR_ING, editing);
        context.startActivity(intent);
    }
}
