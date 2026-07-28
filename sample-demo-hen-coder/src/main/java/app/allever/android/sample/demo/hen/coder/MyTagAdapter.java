package app.allever.android.sample.demo.hen.coder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import app.allever.android.sample.demo.hen.coder.draw.TagFlowAdapter;

import java.util.ArrayList;

public class MyTagAdapter extends TagFlowAdapter {

    private ArrayList<String> mList = new ArrayList<>();
    private Context mContext;
    public MyTagAdapter(Context context){
        mContext = context;
    }

    public void setData(ArrayList<String> list){
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position) {
        View view = View.inflate(mContext, R.layout.hc_item_tag, null);
        TextView tv = (TextView) view.findViewById(R.id.tv);
        tv.setText(mList.get(position));
        return view;
    }
}