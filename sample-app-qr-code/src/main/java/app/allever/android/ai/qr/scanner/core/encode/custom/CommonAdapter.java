package app.allever.android.ai.qr.scanner.core.encode.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonAdapter<T>  extends BaseAdapter {

	protected List<T> mDatas;
	protected LayoutInflater mInflater;
    protected Context mContext;
    protected int layoutId;
    
    public CommonAdapter(Context ctx, List<T> lists, int layoutId){
    	this.mDatas=lists;
    	mInflater=(LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	this.mContext=ctx;
    	this.layoutId=layoutId;
    }

    public void setDatas(List<T> mDatas) {
        this.mDatas = mDatas;
        notifyDataSetChanged();
    }

    @Override
	public int getCount() {
		return mDatas==null?0:mDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh=ViewHolder.getViewHolder(convertView, mContext, parent, layoutId, position);
		convert(vh, mDatas.get(position));
		return vh.getConvertView();
	}
	
	abstract protected void convert(ViewHolder vh,T item);

    public void replace(List<T> list){
        if(null==list){
            return;
        }
        if(null==mDatas){
            mDatas=new ArrayList<T>();
        } else {
            mDatas.clear();
        }
        mDatas.addAll(list);
        this.notifyDataSetChanged();
    }

    public void addAll(List<T> list){
        if(null==list){
            return;
        }
        if(null==mDatas){
            mDatas=new ArrayList<T>();
        }
        mDatas.addAll(list);
        this.notifyDataSetChanged();
    }

    public List<T> getApps(){
      return mDatas;
    };

    public void clearData(){
        if(null!=mDatas){
            mDatas.clear();
        }
    }

    public T getItemByPosition(int i){
        if(i<0){
            return null;
        }
        return mDatas.get(i);
    }
}
