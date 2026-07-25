package app.allever.android.ai.qr.scanner.ui;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.client.android.DeviceUtils;
import com.google.zxing.client.result.ParsedResultType;
import com.allever.app.qr.code.scaner.R;
import app.allever.android.ai.qr.scanner.bean.ShareItem;
import app.allever.android.ai.qr.scanner.core.preview.ResultUIModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * 主界面分享 tab
 */
public class GeneratorFragment extends BaseFragment {
    private List<ShareItem> mShareItemList = new ArrayList<>();

    private static final int MAX_COL = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_share, container, false);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.rv_share_type);
        ShareTypeAdapter adapter = new ShareTypeAdapter(mShareItemList);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), MAX_COL));
        final int spacingInPixels = DeviceUtils.dip2px(requireContext(), 16);
        final int firstTopSpacing = DeviceUtils.dip2px(requireContext(), 20);
        final int bottomSpacing = DeviceUtils.dip2px(requireContext(), 8);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                int pos = parent.getChildLayoutPosition(view);
                if ((pos / MAX_COL) == 0) {
                    outRect.top = firstTopSpacing;
                }
                outRect.bottom = bottomSpacing;
                if ((pos % MAX_COL) == 0) {
                    outRect.left = spacingInPixels;
                    outRect.right = 0;
                } else if ((pos % MAX_COL) + 1 == MAX_COL) {
                    outRect.left = 0;
                    outRect.right = spacingInPixels;
                } else {
                    outRect.left = 0;
                    outRect.right = 0;
                }
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setShareItemClickListener(position -> {
            InputFragment inputFragment = new InputFragment(mShareItemList.get(position));
            inputFragment.show(getFragmentManager(), GeneratorFragment.class.getName());
        });

        return view;
    }

    private void initData(){
        mShareItemList = Arrays.asList(
                new ShareItem(R.drawable.icon_clipboard, R.string.share_type_clipboard, ShareItem.TYPE_CLIPBOARD, ParsedResultType.TEXT, false, true),
                new ShareItem(R.drawable.icon_url, R.string.result_type_name_uri, ShareItem.TYPE_URL, ParsedResultType.URI, false, true),
//                new ShareItem(R.drawable.icon_wifi, R.string.result_type_name_wifi, ShareItem.TYPE_WIFI, ParsedResultType.WIFI, false, true),
                new ShareItem(R.drawable.icon_word, R.string.result_type_name_text, ShareItem.TYPE_TEXT, ParsedResultType.TEXT, false, true),
                new ShareItem(R.drawable.icon_user, R.string.result_type_name_addressbook, ShareItem.TYPE_CONTACT, ParsedResultType.ADDRESSBOOK, false, true),
                new ShareItem(R.drawable.icon_number, R.string.result_type_name_tel, ShareItem.TYPE_PHONE, ParsedResultType.TEL, false, true),
//                new ShareItem(R.drawable.icon_email, R.string.result_type_name_email_address, ShareItem.TYPE_EMAIL, ParsedResultType.EMAIL_ADDRESS, false, true),
                new ShareItem(R.drawable.icon_message, R.string.result_type_name_sms, ShareItem.TYPE_SMS, ParsedResultType.SMS, false, true)

//                new ShareItem(R.drawable.icon_instagram, R.string.result_type_name_instagram, ShareItem.TYPE_INSTAGRAM, ParsedResultType.INSTAGRAM, true, false),
//                new ShareItem(R.drawable.icon_facebook, R.string.result_type_name_facebook, ShareItem.TYPE_FACEBOOK, ParsedResultType.FACEBOOK, true, false),
//                new ShareItem(R.drawable.icon_whatsapp, R.string.result_type_name_whatsapp, ShareItem.TYPE_WHATSAPP, ParsedResultType.WHATSAPP, true, false),
//                new ShareItem(R.drawable.icon_youtube, R.string.result_type_name_youtube, ShareItem.TYPE_YOUTUBE, ParsedResultType.YOUTUBE, true, false),
//                new ShareItem(R.drawable.icon_twitter, R.string.result_type_name_twitter, ShareItem.TYPE_TWITTER, ParsedResultType.TWITTER, true, false),
//                new ShareItem(R.drawable.icon_spotify, R.string.result_type_name_spotify, ShareItem.TYPE_SPOTIFY, ParsedResultType.SPOTIFY, true, false),
//                new ShareItem(R.drawable.icon_viber, R.string.result_type_name_viber, ShareItem.TYPE_VIBER, ParsedResultType.VIBER, true, false)
        );
    }

    private class ShareTypeAdapter extends RecyclerView.Adapter<ShareViewHolder>{

        private List<ShareItem> shareItemList;
        private OnShareItemClickListener listener;

        public ShareTypeAdapter(List<ShareItem> shareItemList){
            this.shareItemList = shareItemList;
        }

        @Override
        public ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.share_type_item, parent, false);
            ShareViewHolder shareViewHolder = new ShareViewHolder(view);
            return shareViewHolder;
        }

        @Override
        public void onBindViewHolder(ShareViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            ShareItem shareItem = shareItemList.get(position);
            holder.tvTitle.setText(shareItem.getTitle());
            holder.ivLogo.setImageResource(shareItem.getResId());
            if (shareItem.isIconHasBackground()) {
                holder.ivLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                if (shareItem.getType() == ShareItem.TYPE_CLIPBOARD) {
                    ResultUIModel.INSTANCE.changeBackground(ResultUIModel.INSTANCE.getClipboardUIData(), holder.ivLogo, false);
                } else {
                    ResultUIModel.INSTANCE.changeBackground(shareItem.getParsedResultType(), holder.ivLogo, false);
                }
            } else {
                holder.ivLogo.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            if (listener != null){
                holder.llRootView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(position);
                    }
                });
            }

//            Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "roboto_bold.ttf");
//            holder.tvTitle.setTypeface(type);
        }

        @Override
        public int getItemCount() {
            return shareItemList.size();
        }

        public void setShareItemClickListener(OnShareItemClickListener listener){
            this.listener = listener;
        }
    }

    private class ShareViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivLogo;
        private TextView tvTitle;
        private LinearLayout llRootView;
        public ShareViewHolder(View itemView){
            super(itemView);
            ivLogo = (ImageView)itemView.findViewById(R.id.iv_share_type_img);
            tvTitle = (TextView)itemView.findViewById(R.id.tv_share_type_title);
            llRootView = (LinearLayout)itemView.findViewById(R.id.ll_share_type_container);
        }
    }

    public interface OnShareItemClickListener{
        void onItemClick(int position);
    }
}
