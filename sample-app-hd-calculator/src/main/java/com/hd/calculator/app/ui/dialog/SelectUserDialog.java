package com.hd.calculator.app.ui.dialog;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.base.BaseDialog;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.databinding.DialogUserListBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.ui.adapter.UserAdapter;
import com.hd.calculator.app.ui.item.UserItem;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectUserDialog extends BaseDialog<DialogUserListBinding> {
    private UserAdapter mAdapter;
    //data
    private List<UserItem> mUserList = new ArrayList<>();

    //itemListener
    private ItemClickListener mItemClickListener;
    private String mUsername;

    public SelectUserDialog(@NonNull Context context) {
        super(context);
        mUsername = AccountManager.getIns().getAccount().getUserName();
    }

    //setItemListener
    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    @Override
    protected DialogUserListBinding getViewBinding() {
        return DialogUserListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.rvRemark.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new UserAdapter(mUserList);
        mBinding.rvRemark.setAdapter(mAdapter);
        mAdapter.setItemClickListener(item -> {
            for (UserItem userItem : mUserList) {
                userItem.setSelected(userItem.getUserName().equals(item.getUserName()));
            }
            mAdapter.notifyDataSetChanged();

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(item);
                dismiss();
            }
        });

        initUserList();
    }

    private void initUserList() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            mUserList.clear();

            List<AccountEntity> accountList = DataBaseRepository.getInstance().getAccountList();
            for (AccountEntity accountEntity : accountList) {
                UserItem userItem = new UserItem();
                userItem.setUserId(accountEntity.getUserId());
                userItem.setUserName( accountEntity.getUserName());
                userItem.setPwd(accountEntity.getPassword());
                userItem.setBoss(accountEntity.isBoss());
                userItem.setSelected(accountEntity.getUserName().equals(mUsername));
                mUserList.add(userItem);
            }

            ThreadUtils.runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
            });
        });
    }

    public interface ItemClickListener {
        void onItemClick(UserItem item);
    }
}
