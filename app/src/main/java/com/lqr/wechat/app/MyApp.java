package com.lqr.wechat.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONException;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lqr.emoji.LQREmotionKit;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.loader.ImageLoader;
import com.lqr.imagepicker.view.CropImageView;
import com.lqr.wechat.api.redpacket.SignService;
import com.lqr.wechat.app.base.BaseApp;
import com.lqr.wechat.db.DBManager;
import com.lqr.wechat.db.model.Friend;
import com.lqr.wechat.db.model.Groups;
import com.lqr.wechat.manager.BroadcastManager;
import com.lqr.wechat.manager.JsonMananger;
import com.lqr.wechat.model.cache.UserCache;
import com.lqr.wechat.model.data.GroupNotificationMessageData;
import com.lqr.wechat.model.message.DeleteContactMessage;
import com.lqr.wechat.model.message.RedPacketMessage;
import com.lqr.wechat.model.redpacket.SignModel;
import com.lqr.wechat.model.response.ContactNotificationMessageData;
import com.lqr.wechat.util.LogUtils;
import com.lqr.wechat.util.PinyinUtils;
import com.lqr.wechat.util.RedPacketUtil;
import com.lqr.wechat.util.UIUtils;
import com.yunzhanghu.redpacketsdk.RPInitRedPacketCallback;
import com.yunzhanghu.redpacketsdk.RPValueCallback;
import com.yunzhanghu.redpacketsdk.RedPacket;
import com.yunzhanghu.redpacketsdk.bean.RedPacketInfo;
import com.yunzhanghu.redpacketsdk.bean.TokenData;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.util.List;

import cn.sharesdk.framework.ShareSDK;
import io.rong.imlib.AnnotationNotFoundException;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ????????????????????????????????????????????????
 * ??????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ?????????????????????????????????
 * ???????????????????????????  ????????????
 * ???????????????????????????  ?????????bug
 * ???????????????????????????????????????
 * ??????????????????????????????????????????
 * ??????????????????????????????????????????
 * ???????????????????????????????????????
 * ????????????????????????????????????
 * ????????????????????????????????????
 * ???????????????????????????????????????????????????
 *
 * @????????? CSDN_LQR
 * @?????? BaseApp???????????????????????????????????????????????????
 */
public class MyApp extends BaseApp implements RongIMClient.OnReceiveMessageListener {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        //???????????????
        initRongCloud();
        //???????????????
        initRedPacket();
        //????????????????????????ImagePicker
        initImagePicker();
        //?????????????????????
        LQREmotionKit.init(this, (context, path, imageView) -> Glide.with(context).load(path).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView));
        //?????????ShareSDK
        ShareSDK.initSDK(getContext());
    }

    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals(ContactNotificationMessage.CONTACT_OPERATION_REQUEST)) {
                //????????????????????????
                BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_RED_DOT);
            } else {
                //??????????????????????????????
                ContactNotificationMessageData c = null;
                try {
                    c = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                } catch (HttpException e) {
                    e.printStackTrace();
                    return false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                if (c != null) {
                    if (DBManager.getInstance().isMyFriend(contactNotificationMessage.getSourceUserId()))
                        return false;
                    DBManager.getInstance().saveOrUpdateFriend(
                            new Friend(contactNotificationMessage.getSourceUserId(),
                                    c.getSourceUserNickname(),
                                    null, c.getSourceUserNickname(), null, null,
                                    null, null,
                                    PinyinUtils.getPinyin(c.getSourceUserNickname()),
                                    PinyinUtils.getPinyin(c.getSourceUserNickname())
                            )
                    );
                    BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_FRIEND);
                    BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_RED_DOT);
                }
            }
        } else if (messageContent instanceof DeleteContactMessage) {
            DeleteContactMessage deleteContactMessage = (DeleteContactMessage) messageContent;
            String contact_id = deleteContactMessage.getContact_id();
            RongIMClient.getInstance().getConversation(Conversation.ConversationType.PRIVATE, contact_id, new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    RongIMClient.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, contact_id, new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            RongIMClient.getInstance().removeConversation(Conversation.ConversationType.PRIVATE, contact_id, null);
                            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
            DBManager.getInstance().deleteFriendById(contact_id);
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_FRIEND);
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            String groupId = message.getTargetId();
            GroupNotificationMessageData data = null;
            try {
                String curUserId = UserCache.getId();
                try {
                    data = jsonToBean(groupNotificationMessage.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_CREATE)) {
                    DBManager.getInstance().getGroups(groupId);
                    DBManager.getInstance().getGroupMember(groupId);
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_DISMISS)) {
                    handleGroupDismiss(groupId);
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_KICKED)) {
                    if (data != null) {
                        List<String> memberIdList = data.getTargetUserIds();
                        if (memberIdList != null) {
                            for (String userId : memberIdList) {
                                if (curUserId.equals(userId)) {
                                    RongIMClient.getInstance().removeConversation(Conversation.ConversationType.GROUP, message.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            LogUtils.sf("Conversation remove successfully.");
                                        }

                                        @Override
                                        public void onError(RongIMClient.ErrorCode e) {

                                        }
                                    });
                                }
                            }
                        }
                        List<String> kickedUserIDs = data.getTargetUserIds();
                        DBManager.getInstance().deleteGroupMembers(groupId, kickedUserIDs);
                        //??????????????????????????????????????????????????????
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_GROUP_MEMBER, groupId);
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                    }
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_ADD)) {
                    DBManager.getInstance().getGroups(groupId);
                    DBManager.getInstance().getGroupMember(groupId);
                    //??????????????????????????????????????????????????????
//                    BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_GROUP_MEMBER, groupId);
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_QUIT)) {
                    if (data != null) {
                        List<String> quitUserIDs = data.getTargetUserIds();
                        DBManager.getInstance().deleteGroupMembers(groupId, quitUserIDs);
                        //??????????????????????????????????????????????????????
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_GROUP_MEMBER, groupId);
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                    }
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_RENAME)) {
                    if (data != null) {
                        String targetGroupName = data.getTargetGroupName();
                        DBManager.getInstance().updateGroupsName(groupId, targetGroupName);
                        //????????????
                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CURRENT_SESSION_NAME);
                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
        } else {
            //TODO:???????????????????????????
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CURRENT_SESSION, message);
        }
        return false;
    }

    private void handleGroupDismiss(final String groupId) {
        RongIMClient.getInstance().getConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                RongIMClient.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        RongIMClient.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                DBManager.getInstance().deleteGroup(new Groups(groupId));
                                DBManager.getInstance().deleteGroupMembersByGroupId(groupId);
                                BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                                BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.GROUP_LIST_UPDATE);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private GroupNotificationMessageData jsonToBean(String data) {
        GroupNotificationMessageData dataEntity = new GroupNotificationMessageData();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("operatorNickname")) {
                dataEntity.setOperatorNickname(jsonObject.getString("operatorNickname"));
            }
            if (jsonObject.has("targetGroupName")) {
                dataEntity.setTargetGroupName(jsonObject.getString("targetGroupName"));
            }
            if (jsonObject.has("timestamp")) {
                dataEntity.setTimestamp(jsonObject.getLong("timestamp"));
            }
            if (jsonObject.has("targetUserIds")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserIds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserIds().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("targetUserDisplayNames")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserDisplayNames");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserDisplayNames().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("oldCreatorId")) {
                dataEntity.setOldCreatorId(jsonObject.getString("oldCreatorId"));
            }
            if (jsonObject.has("oldCreatorName")) {
                dataEntity.setOldCreatorName(jsonObject.getString("oldCreatorName"));
            }
            if (jsonObject.has("newCreatorId")) {
                dataEntity.setNewCreatorId(jsonObject.getString("newCreatorId"));
            }
            if (jsonObject.has("newCreatorName")) {
                dataEntity.setNewCreatorName(jsonObject.getString("newCreatorName"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEntity;
    }

    private void initRongCloud() {
        /**
         * OnCreate ??????????????????????????????????????????????????????????????????????????? RongIMClient ???????????? Push ??????????????? init???
         * io.rong.push ????????? push ??????????????????????????????
         */
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext())) ||
                "io.rong.push".equals(getCurProcessName(getApplicationContext()))) {
            RongIMClient.init(this);
        }

        //????????????????????????
        RongIMClient.setOnReceiveMessageListener(this);
        try {
            RongIMClient.registerMessageType(RedPacketMessage.class);
            RongIMClient.registerMessageType(DeleteContactMessage.class);
        } catch (AnnotationNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initRedPacket() {
        //???????????????SDK
        RedPacket.getInstance().initRedPacket(this, RPConstant.AUTH_METHOD_SIGN, new RPInitRedPacketCallback() {
            //???????????????TokenData???????????????
            //?????? ???????????????????????????token?????????????????????????????????token??????????????????????????????????????????
            //?????? ???????????????????????????????????????????????????SDK?????????????????????????????????????????????
            @Override
            public void initTokenData(final RPValueCallback<TokenData> callback) {
                //?????????App Server??????????????????
                //???????????????????????????UUID??????App??????userId,????????????????????????App???userId
                String token = "tempValue";
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        //Demo???URL,???????????????????????????APP Server???????????????URL
                        .baseUrl("https://rpv2.yunzhanghu.com/")
                        .build();
                SignService signService = retrofit.create(SignService.class);
                Call<SignModel> call = signService.getSignInfo(UserCache.getId(), token);
                call.enqueue(new Callback<SignModel>() {
                    @Override
                    public void onResponse(Call<SignModel> call, Response<SignModel> response) {
                        if (response.isSuccessful()) {
                            SignModel signModel = response.body();
                            LogUtils.sf(signModel.toString());
                            //?????????????????????TokenData
                            TokenData tokenData = new TokenData();
                            tokenData.authPartner = signModel.partner;
                            tokenData.authSign = signModel.sign;
                            tokenData.appUserId = signModel.user_id;
                            tokenData.timestamp = signModel.timestamp;
                            //???????????????????????????SDK
                            callback.onSuccess(tokenData);
                        } else {
                            String statusCode = response.code() + "";
                            callback.onError(statusCode, response.errorBody().toString());
                            LogUtils.d("StatusCode : " + statusCode + " Message : " + response.errorBody().toString());
                        }

                    }

                    @Override
                    public void onFailure(Call<SignModel> call, Throwable t) {
                        LogUtils.d("onFailure :" + t.getMessage());
                        callback.onError("onFailure", t.getMessage());
                    }
                });
            }

            @Override
            public RedPacketInfo initCurrentUserSync() {
                return RedPacketUtil.getCurrentUserInfo();
            }
        });
        //??????????????????????????????
        RedPacket.getInstance().setDebugMode(true);
    }

    /**
     * ????????????????????????ImagePicker
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
                Glide.with(getContext()).load(Uri.parse("file://" + path).toString()).centerCrop().into(imageView);
            }

            @Override
            public void clearMemoryCache() {

            }
        });   //?????????????????????
        imagePicker.setShowCamera(true);  //??????????????????
        imagePicker.setCrop(true);        //?????????????????????????????????
        imagePicker.setSaveRectangle(true); //???????????????????????????
        imagePicker.setSelectLimit(9);    //??????????????????
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //??????????????????
        imagePicker.setFocusWidth(800);   //?????????????????????????????????????????????????????????????????????
        imagePicker.setFocusHeight(800);  //?????????????????????????????????????????????????????????????????????
        imagePicker.setOutPutX(1000);//????????????????????????????????????
        imagePicker.setOutPutY(1000);//????????????????????????????????????
    }

    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

}
