package com.allever.sticker.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xm.sticker.camera.R;
import com.allever.sticker.ui.adapter.RecyclerItemClickListener;
import com.allever.sticker.bean.Sticker;
import com.allever.sticker.ui.adapter.StickerRecyclerAdapter;

import app.allever.android.lib.core.base.AbstractFragment;

/**
 *
 * @author Allever
 * @date 2017/12/31
 */

public class StickerFragment extends AbstractFragment {

    private OnStickerClickListener mStickerClickListener;
    private Context mContext;
    private Sticker mSticker;

    private RecyclerView mRecyclerView;
    private StickerRecyclerAdapter mStickerRecyclerAdapter;

    public StickerFragment(){

    }
    @SuppressLint("ValidFragment")
    public StickerFragment(Context context,Sticker sticker){
        mContext = context;
        mSticker = sticker;
        mStickerClickListener = (OnStickerClickListener)mContext;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sc_fragment_sticker,container,false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.id_fragment_sticker_recycler);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,6));
        mStickerRecyclerAdapter = new StickerRecyclerAdapter(mContext,mSticker);
        mRecyclerView.setAdapter(mStickerRecyclerAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mContext,
                                                                            mRecyclerView,
                                                                            new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mStickerClickListener != null){
                    if (mSticker.getResId()!=null){
                        mStickerClickListener.onStickClick(mSticker.getResId()[position],null);
                    }else {
                        mStickerClickListener.onStickClick(-1,mSticker.getStickerPathList().get(position));
                    }

                }
            }
            @Override
            public void onItemLongClick(View view, int position) {
            }
        }));
        return view;
    }

    public Sticker getSticker(){
        return mSticker;
    }

    public interface OnStickerClickListener{
        void onStickClick(int resId, String path);
    }
}
