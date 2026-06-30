//package com.videoeditor.ui.widget;
//
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.helper.ItemTouchHelper;
//
//import com.allever.videoeditordemo.DragHolderCallBack;
//import com.allever.videoeditordemo.DragRecyclerViewCallBack;
//
//
//public class DragItemCallBack extends ItemTouchHelper.Callback {
//
//    private DragRecyclerViewCallBack mCallBack;
//
//    public DragItemCallBack(DragRecyclerViewCallBack callBack) {
//        this.mCallBack = callBack;
//    }
//
//    @Override
//    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
//        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
//        int action = ItemTouchHelper.ACTION_STATE_IDLE | ItemTouchHelper.ACTION_STATE_DRAG;
////        return makeFlag(action,dragFlags);
//        return makeMovementFlags(dragFlags, 0);
//    }
//
//    @Override
//    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//        int start = viewHolder.getAdapterPosition();
//        int end = target.getAdapterPosition();
//
//        mCallBack.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
//        return true;
//    }
//
//    @Override
//    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
//    }
//
//    @Override
//    public boolean isLongPressDragEnabled() {
//        return false;
//    }
//
//    DragHolderCallBack mViewHolder;
//
//    @Override
//    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//        super.onSelectedChanged(viewHolder, actionState);
//        if (viewHolder instanceof DragHolderCallBack) {
//            mViewHolder = (DragHolderCallBack) viewHolder;
//            mViewHolder.onSelect();
//        } else {
//            if (mViewHolder != null) {
////                mViewHolder.onUnSelect();
//            }
//        }
//    }
//
//    @Override
//    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        super.clearView(recyclerView, viewHolder);
//        if (viewHolder instanceof DragHolderCallBack) {
//            DragHolderCallBack holder = (DragHolderCallBack) viewHolder;
//
//            if (!recyclerView.isComputingLayout() && recyclerView.getScrollState() == 0) {
//                holder.onClear();
//            }
//        }
//    }
//
//}
