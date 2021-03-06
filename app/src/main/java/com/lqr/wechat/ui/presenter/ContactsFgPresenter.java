package com.lqr.wechat.ui.presenter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.wechat.R;
import com.lqr.wechat.db.DBManager;
import com.lqr.wechat.db.model.Friend;
import com.lqr.wechat.ui.activity.UserInfoActivity;
import com.lqr.wechat.ui.base.BaseActivity;
import com.lqr.wechat.ui.base.BasePresenter;
import com.lqr.wechat.ui.view.IContactsFgView;
import com.lqr.wechat.util.LogUtils;
import com.lqr.wechat.util.SortUtils;
import com.lqr.wechat.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ContactsFgPresenter extends BasePresenter<IContactsFgView> {

    private List<Friend> mData = new ArrayList<>();
    private LQRHeaderAndFooterAdapter mAdapter;

    public ContactsFgPresenter(BaseActivity context) {
        super(context);
    }

    public void loadContacts() {
        setAdapter();
        loadData();
    }

    private void loadData() {
        Observable.just(DBManager.getInstance().getFriends())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(friends -> {
                    if (friends != null && friends.size() > 0) {
                        mData.clear();
                        mData.addAll(friends);
                        getView().getFooterView().setText(UIUtils.getString(R.string.count_of_contacts, mData.size()));
                        //ζ΄ηζεΊ
                        SortUtils.sortContacts(mData);
                        if (mAdapter != null)
                            mAdapter.notifyDataSetChanged();
                    }
                }, this::loadError);
    }

    private void setAdapter() {
        if (mAdapter == null) {
            LQRAdapterForRecyclerView adapter = new LQRAdapterForRecyclerView<Friend>(mContext, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Friend item, int position) {
                    helper.setText(R.id.tvName, item.getDisplayName());
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
                    Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);

                    String str = "";
                    //εΎε°ε½εε­ζ―
                    String currentLetter = item.getDisplayNameSpelling().charAt(0) + "";
                    if (position == 0) {
                        str = currentLetter;
                    } else {
                        //εΎε°δΈδΈδΈͺε­ζ―
                        String preLetter = mData.get(position - 1).getDisplayNameSpelling().charAt(0) + "";
                        //ε¦ζεδΈδΈδΈͺε­ζ―ηι¦ε­ζ―δΈεεζΎη€Ίε­ζ―ζ 
                        if (!preLetter.equalsIgnoreCase(currentLetter)) {
                            str = currentLetter;
                        }
                    }
                    int nextIndex = position + 1;
                    if (nextIndex < mData.size() - 1) {
                        //εΎε°δΈδΈδΈͺε­ζ―
                        String nextLetter = mData.get(nextIndex).getDisplayNameSpelling().charAt(0) + "";
                        //ε¦ζεδΈδΈδΈͺε­ζ―ηι¦ε­ζ―δΈεειθδΈεηΊΏ
                        if (!nextLetter.equalsIgnoreCase(currentLetter)) {
                            helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
                        } else {
                            helper.setViewVisibility(R.id.vLine, View.VISIBLE);
                        }
                    } else {
                        helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
                    }
                    if (position == mData.size() - 1) {
                        helper.setViewVisibility(R.id.vLine, View.GONE);
                    }

                    //ζ Ήζ?strζ―ε¦δΈΊη©Ίε³ε?ε­ζ―ζ ζ―ε¦ζΎη€Ί
                    if (TextUtils.isEmpty(str)) {
                        helper.setViewVisibility(R.id.tvIndex, View.GONE);
                    } else {
                        helper.setViewVisibility(R.id.tvIndex, View.VISIBLE);
                        helper.setText(R.id.tvIndex, str);
                    }
                }
            };
            adapter.addHeaderView(getView().getHeaderView());
            adapter.addFooterView(getView().getFooterView());
            mAdapter = adapter.getHeaderAndFooterAdapter();
            getView().getRvContacts().setAdapter(mAdapter);
        }
        ((LQRAdapterForRecyclerView) mAdapter.getInnerAdapter()).setOnItemClickListener((lqrViewHolder, viewGroup, view, i) -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userInfo", DBManager.getInstance().getUserInfo(mData.get(i - 1).getUserId()));//-1ζ―ε δΈΊζε€΄ι¨
            mContext.jumpToActivity(intent);
        });
    }

    private void loadError(Throwable throwable) {
        LogUtils.e(throwable.getLocalizedMessage());
        UIUtils.showToast(UIUtils.getString(R.string.load_contacts_error));
    }
}
