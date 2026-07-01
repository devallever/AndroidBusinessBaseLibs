package com.allever.video.editor.function.share;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;


import com.android.absbase.App;
import com.allever.video.editor.R;
import com.allever.video.editor.ui.bean.BitmapBean;
//import com.allever.video.editor.utils.DataManager;
import com.allever.video.editor.utils.FileUtil;
import com.allever.video.editor.utils.MediaTypeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareImageTools {
    /**
     * 朋友圈支持的最低微信版本
     */
    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private static boolean mIsStarted = false;

//	private static final int THUMB_SIZE = 90;

    /**
     *
     */
    public static final String FACEBOOK_SEND_PIC_TO_SHARE_PACKAGE_NAME = "com.facebook.katana";
    public static final String FACEBOOK_SEND_PIC_TO_SHARE_ACTIVITY_NAME1 = "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias";
    /**
     * 更新的
     */
    public static final String FACEBOOK_SEND_PIC_TO_SHARE_ACTIVITY_NAME2 = "com.facebook.composer.shareintent.ImplicitShareIntentHandler";


//	public static final String TWITTER = "Twitter";
//	public static final String TWITTER_SEND_PIC_TO_SHARE_PACKAGE_NAME = "com.twitter.android";
//	public static final String TWITTER_SEND_PIC_TO_SHARE_ACTIVITY_NAME = "com.twitter.android.composer.ComposerActivity";

    public static final String WHATSAPP_SEND_PIC_TO_SHARE_PACKAGE_NAME = "com.whatsapp";
    public static final String WHATSAPP_SEND_PIC_TO_SHARE_ACTIVITY_NAME = "com.whatsapp.ContactPicker";

    public static final String INSTAGRAM_SEND_PIC_TO_SHARE_PACKAGE_NAME = "com.instagram.android";
    public static final String INSTAGRAM_SEND_PIC_TO_SHARE_ACTIVITY_NAME = "com.instagram.android.activity.ShareHandlerActivity";

    //	public static final String LINE = "Line";
//	public static final String LINE_SEND_PIC_TO_SHARE_PACKAGE_NAME = "jp.naver.line.android";
//	public static final String LINE_SEND_PIC_TO_SHARE_ACTIVITY_NAME = "jp.naver.line.android.activity.selectchat.SelectChatActivity";
//	
    public static final String WEIXIN_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME = "com.tencent.mm";
    public static final String WEIXIN_SEND_PIC_TO_PENGYOUQUAN_ACTIVITY_NAME = "com.tencent.mm.ui.tools.ShareToTimeLineUI";

    public static final String QQ_SEND_PIC_TO_KONGJIAN_PACKAGE_NAME = "com.qzone";
    public static final String QQ_SEND_PIC_TO_KONGJIAN_ACTIVITY_NAME = "com.qzonex.module.operation.ui.QZonePublishMoodActivity";

    public static final String SINA_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME = "com.sina.weibo";
    public static final String SINA_SEND_PIC_TO_PENGYOUQUAN_ACTIVITY_NAME = "com.sina.weibo.ComposerDispatchActivity";

//	public static final String WEIXIN_SEND_PIC_TO_FRIEND_PACKAGE_NAME = "com.tencent.mm";

    //微信发送到朋友
    public static final String WEIXIN_SEND_PIC_TO_FRIEND_ACTIVITY_NAME = "com.tencent.mm.ui.tools.ShareImgUI";
    //微信添加收藏
    public static final String WEIXIN_SEND_PIC_TO_COLLECTION_ACTIVITY_NAME = "com.tencent.mm.ui.tools.AddFavoriteUI";

//	public static final String PRINT_AS_PACKAGE_NAME = "print_as";

//    public static final String PRINT_LOCAL_PACKAGE_NAME = "print_local";
//
//    public static final String SNAP_PACKAGE_NAME = "com.snapchat.android";
//
//    public static final String MESSENGER_PACKAGE_NAME = "com.facebook.orca";

    /**
     * 用于判断应用是否安装
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean getAppIsInstalled(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) return false;

        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
            if (packageInfo != null) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static boolean getAppIsInstalled(Context context, String[] pkgNames) {
        if (pkgNames == null) {
            return false;
        }
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> list = pm.getInstalledPackages(0);
            for (PackageInfo info : list) {
                for (String name : pkgNames) {
                    if (info.packageName.equals(name)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * Facebook 是否安装
     *
     * @param context
     * @return
     */
    public static boolean isFacebookInstalled(Context context) {
        return getAppIsInstalled(context, new String[]{"com.facebook.katana", "com.facebook.lite"});
    }

    /**
     * 启动分享的Activity
     *
     * @param pkgName 包名
     * @param appName 分享Activcity的Activity的（package name + activity name）
     */
    public static boolean startShareActivity(final Context context, String pkgName, final String appName, final BitmapBean bean) {
        if (MediaTypeUtil.INSTANCE.isText(bean.mType)) {
            return startCommonShareTextActivity(context, pkgName, appName, bean.mPath);
        }
        return startCommonShareActivity(context, pkgName, appName, bean.mUri, MediaTypeUtil.INSTANCE.isImage(bean.mType));
    }

    /**
     * 普通的启动Activity的方法
     *
     * @param context
     * @param pkgName
     * @param appName
     * @param u
     * @param isImage
     * @return
     */
    public static boolean startCommonShareActivity(final Context context, String pkgName, String appName, final Uri u, boolean isImage) {
        boolean flag = true;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (TextUtils.isEmpty(appName)) {
            shareIntent.setPackage(pkgName);
        } else {
            shareIntent.setComponent(new ComponentName(pkgName, appName));
        }
        if (isImage) {
            shareIntent.setType("image/*");
        } else {
            shareIntent.setType("video/*");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, u);
        try {
            context.startActivity(shareIntent);
        } catch (Throwable e) {//增加保护
            flag = false;
            List<ShareImageItem.ShareImageItemData> result = getAllShareTools(context, isImage);
            for (ShareImageItem.ShareImageItemData data : result) {
                if (data.getmPkgName().equals(pkgName)) {
                    shareIntent.setComponent(new ComponentName(data.getmPkgName(), data.getmActivityName()));
                    flag = true;
                    break;
                }
            }
            try {
                if (flag) {
                    context.startActivity(shareIntent);
                }
            } catch (Throwable e1) {
                flag = false;
            }
        }

        return flag;
    }

    /**
     * 启动分享的Activity （文案）
     *
     * @param context
     * @param pkgName  包名
     * @param appName  分享Activcity的Activity的（package name + activity name
     * @param contents 分享文案title、description、url
     * @return
     */
    public static boolean startShareTextActivity(final Context context, String pkgName,
                                                 final String appName, final String[] contents) {
        if (pkgName.equals(WEIXIN_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME)) {
        }

        return startCommonShareTextActivity(context, pkgName, appName, contents[1]);
    }

    /**
     * 普通的启动Activity的方法
     *
     * @param context
     * @param pkgName
     * @param appName
     * @param description
     * @return
     */
    public static boolean startCommonShareTextActivity(final Context context, String pkgName, String appName, String description) {
        boolean flag = true;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(appName)) {
            shareIntent.setComponent(new ComponentName(pkgName, appName));
        } else {
            shareIntent.setPackage(pkgName);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, description);
        //shareIntent.putExtra(Intent.EXTRA_TEXT, "This is text");


        try {
            context.startActivity(shareIntent);
        } catch (Throwable e) {//增加保护
            e.printStackTrace();
            flag = false;
            List<ShareImageItem.ShareImageItemData> result = getAllShareTextTools(context);

            for (ShareImageItem.ShareImageItemData data : result) {
                if (data.getmPkgName().equals(pkgName)) {
                    shareIntent.setComponent(new ComponentName(data.getmPkgName(), data.getmActivityName()));
                    flag = true;
                    break;
                }
            }
            try {
                context.startActivity(shareIntent);
            } catch (Throwable e1) {
                flag = false;
            }
        }

        return flag;
    }


    /**
     * @param pkgName
     * @param activityName
     * @return
     */
    public static boolean isDefaultTools(String pkgName, String activityName) {
		/*if(SystemUtils.isCnUser()) {
			if((WEIXIN_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME.equals(pkgName) && WEIXIN_SEND_PIC_TO_PENGYOUQUAN_ACTIVITY_NAME.equals(activityName))
					|| (QQ_SEND_PIC_TO_KONGJIAN_PACKAGE_NAME.equals(pkgName) && QQ_SEND_PIC_TO_KONGJIAN_ACTIVITY_NAME.equals(activityName))
					|| (SINA_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME.equals(pkgName) && SINA_SEND_PIC_TO_PENGYOUQUAN_ACTIVITY_NAME.equals(activityName))
//					|| PRINT_AS_PACKAGE_NAME.equals(pkgName)
					|| PRINT_LOCAL_PACKAGE_NAME.equals(pkgName)){
				return true;
			} else{
				return false;
			}
		} else*/
        {
            if ((FACEBOOK_SEND_PIC_TO_SHARE_PACKAGE_NAME.equals(pkgName) && (FACEBOOK_SEND_PIC_TO_SHARE_ACTIVITY_NAME1.equals(activityName) || FACEBOOK_SEND_PIC_TO_SHARE_ACTIVITY_NAME2.equals(activityName)))
                    || (WHATSAPP_SEND_PIC_TO_SHARE_PACKAGE_NAME.equals(pkgName) && WHATSAPP_SEND_PIC_TO_SHARE_ACTIVITY_NAME.equals(activityName))
                    || (INSTAGRAM_SEND_PIC_TO_SHARE_PACKAGE_NAME.equals(pkgName) && INSTAGRAM_SEND_PIC_TO_SHARE_ACTIVITY_NAME.equals(activityName))
//					|| PRINT_AS_PACKAGE_NAME.equals(pkgName)
//                    || PRINT_LOCAL_PACKAGE_NAME.equals(pkgName)
            ) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 获取分享第一个界面的数据
     *
     * @param context
     * @param type        1为图片 2视频  3为Text
     * @param needExtra   需不需要extra 只有预览界面需要  其他都是False
     * @param isNeedPrint 是否需要有打印
     * @return
     */
    @SuppressLint("NewApi")
    public static List<ShareImageItem.ShareImageItemData> getTop3ShareToolsAndMore(Context context, int type, boolean needExtra, boolean isNeedPrint) {

        List<ShareImageItem.ShareImageItemData> result = getTop3ShareTools(context, type, needExtra, isNeedPrint);
        result.add(getMore(context));
        return result;
    }

    public static ShareImageItem.ShareImageItemData getMore(Context context) {
        Resources res = context.getResources();
        return new ShareImageItem.ShareImageItemData(null, null, res.getDrawable(R.drawable.share_icon_more), res.getString(R.string.more), true);
    }

    public static List<ShareImageItem.ShareImageItemData> getTop3ShareTools(Context context, int type, boolean needExtra, boolean isNeedPrint) {
        List<ShareImageItem.ShareImageItemData> result = new ArrayList<ShareImageItem.ShareImageItemData>();
        Resources res = context.getResources();
		/*if(SystemUtils.isCnUser()) {
			//weixin 朋友圈
			result.add(new ShareImageItem.ShareImageItemData(WEIXIN_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME,
					WEIXIN_SEND_PIC_TO_PENGYOUQUAN_ACTIVITY_NAME, res.getDrawable(R.drawable.share_icon_wechat), res.getString(R.string.peng_you_quan)));
			//ＱＱ空间
			result.add(new ShareImageItem.ShareImageItemData(QQ_SEND_PIC_TO_KONGJIAN_PACKAGE_NAME,
					QQ_SEND_PIC_TO_KONGJIAN_ACTIVITY_NAME, res.getDrawable(R.drawable.share_icon_qq), res.getString(R.string.kong_jian)));
			//新浪
			result.add(new ShareImageItem.ShareImageItemData(SINA_SEND_PIC_TO_PENGYOUQUAN_PACKAGE_NAME,
					SINA_SEND_PIC_TO_PENGYOUQUAN_ACTIVITY_NAME, res.getDrawable(R.drawable.share_icon_weibo), res.getString(R.string.sina)));
		} else*/
        {
            //facebook
            result.add(new ShareImageItem.ShareImageItemData(FACEBOOK_SEND_PIC_TO_SHARE_PACKAGE_NAME,
                    FACEBOOK_SEND_PIC_TO_SHARE_ACTIVITY_NAME1, res.getDrawable(R.drawable.share_icon_facebook), res.getString(R.string.facebook)));
            //whatsApp
            result.add(new ShareImageItem.ShareImageItemData(WHATSAPP_SEND_PIC_TO_SHARE_PACKAGE_NAME,
                    WHATSAPP_SEND_PIC_TO_SHARE_ACTIVITY_NAME, res.getDrawable(R.drawable.share_icon_whatsapp), res.getString(R.string.whatsapp)));
            //instagram
            result.add(new ShareImageItem.ShareImageItemData(INSTAGRAM_SEND_PIC_TO_SHARE_PACKAGE_NAME,
                    INSTAGRAM_SEND_PIC_TO_SHARE_ACTIVITY_NAME, res.getDrawable(R.drawable.share_icon_ins), res.getString(R.string.instagram)));
        }
        if (type == 1 && needExtra) {
            getStoreTools(context, result, true);
        }
        return result;
    }

    /**
     * 获取保存的工具
     *
     * @param result
     * @param isImage 代表图片还是视频
     */
    private static void getStoreTools(Context context, List<ShareImageItem.ShareImageItemData> result, boolean isImage) {
        try {
            String tool1PkgName = "";
            String tool1ActivityName = "";
            String tool2PkgName = "";
            String tool2ActivityName = "";
//            if (isImage) {
//                tool1PkgName = DataManager.getInstance().getShareImageTool1PkgName();
//                tool1ActivityName = DataManager.getInstance().getShareImageTool1ActivityName();
//                tool2PkgName = DataManager.getInstance().getShareImageTool2PkgName();
//                tool2ActivityName = DataManager.getInstance().getShareImageTool2ActivityName();
//            } else {
//                tool1PkgName = DataManager.getInstance().getShareVideoTool1PkgName();
//                tool1ActivityName = DataManager.getInstance().getShareVideoTool1ActivityName();
//                tool2PkgName = DataManager.getInstance().getShareVideoTool2PkgName();
//                tool2ActivityName = DataManager.getInstance().getShareVideoTool2ActivityName();
//            }
            boolean tool1None = TextUtils.isEmpty(tool1PkgName);
            boolean tool2None = TextUtils.isEmpty(tool2PkgName);
            if (!tool1None && !tool2None) { //两个包名都存在
                boolean tool1IsDefault = isDefaultTools(tool1PkgName, tool1ActivityName);
                boolean tool2IsDefault = isDefaultTools(tool2PkgName, tool2ActivityName);
                if (tool1IsDefault && tool2IsDefault) {//两个工具都不行
                    getCountTools(context, result, isImage, 2);
                } else if (!tool1IsDefault && !tool2IsDefault) {
                    boolean tool1IsInstall = getAppIsInstalled(context, tool1PkgName);
                    boolean tool2IsInstall = getAppIsInstalled(context, tool2PkgName);
                    if (tool1IsInstall && tool2IsInstall) {//两个都已经安装
                        PackageManager pm = context.getPackageManager();
                        ActivityInfo info1 = pm.getActivityInfo(new ComponentName(tool1PkgName, tool1ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                        ActivityInfo info2 = pm.getActivityInfo(new ComponentName(tool2PkgName, tool2ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                        result.add(new ShareImageItem.ShareImageItemData(tool1PkgName,
                                tool1ActivityName, info1.loadIcon(pm), info1.loadLabel(pm).toString()));
                        result.add(new ShareImageItem.ShareImageItemData(tool2PkgName,
                                tool2ActivityName, info2.loadIcon(pm), info2.loadLabel(pm).toString()));
                    } else if ((!tool1IsInstall) && (!tool2IsInstall)) {//两个都没有安装
                        getCountTools(context, result, isImage, 2);
                    } else {//有且只有一个安装了
                        if (tool1IsInstall) {//tool1已经安装
                            PackageManager pm = context.getPackageManager();
                            ActivityInfo info1 = pm.getActivityInfo(new ComponentName(tool1PkgName, tool1ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                            //第一个
                            result.add(new ShareImageItem.ShareImageItemData(tool1PkgName,
                                    tool1ActivityName, info1.loadIcon(pm), info1.loadLabel(pm).toString()));

                            //过滤掉 tool1PkgName  tool1ActivityName
                            getCountTools(context, result, isImage, 1, new String[]{tool1PkgName}, new String[]{tool1ActivityName});
                        } else {//tool2已经安装
                            PackageManager pm = context.getPackageManager();
                            ActivityInfo info2 = pm.getActivityInfo(new ComponentName(tool2PkgName, tool2ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                            //第一个
                            result.add(new ShareImageItem.ShareImageItemData(tool2PkgName,
                                    tool2ActivityName, info2.loadIcon(pm), info2.loadLabel(pm).toString()));

                            //过滤掉 tool2PkgName  tool2ActivityName
                            getCountTools(context, result, isImage, 1, new String[]{tool2PkgName}, new String[]{tool2ActivityName});
                        }
                    }
                } else {
                    if (tool1IsDefault) {//tool1不可用
                        boolean tool2IsInstall = getAppIsInstalled(context, tool2PkgName);
                        if (tool2IsInstall) {
                            PackageManager pm = context.getPackageManager();
                            ActivityInfo info2 = pm.getActivityInfo(new ComponentName(tool2PkgName, tool2ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                            //第一个
                            result.add(new ShareImageItem.ShareImageItemData(tool2PkgName,
                                    tool2ActivityName, info2.loadIcon(pm), info2.loadLabel(pm).toString()));

                            //过滤掉 tool2PkgName  tool2ActivityName
                            getCountTools(context, result, isImage, 1, new String[]{tool2PkgName}, new String[]{tool2ActivityName});
                        } else {
                            getCountTools(context, result, isImage, 2);
                        }
                    } else {//tool2不可用
                        boolean tool1IsInstall = getAppIsInstalled(context, tool1PkgName);
                        if (tool1IsInstall) {
                            PackageManager pm = context.getPackageManager();
                            ActivityInfo info1 = pm.getActivityInfo(new ComponentName(tool1PkgName, tool1ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                            //第一个
                            result.add(new ShareImageItem.ShareImageItemData(tool1PkgName,
                                    tool1ActivityName, info1.loadIcon(pm), info1.loadLabel(pm).toString()));

                            //过滤掉 tool1PkgName  tool1ActivityName
                            getCountTools(context, result, isImage, 1, new String[]{tool1PkgName}, new String[]{tool1ActivityName});
                        } else {
                            getCountTools(context, result, isImage, 2);
                        }
                    }
                }
            } else if (tool1None && tool2None) {//都不存在
                getCountTools(context, result, isImage, 2);
            } else {
                if (!tool1None) {//tool1存在
                    if (isDefaultTools(tool1PkgName, tool1ActivityName)) {
                        getCountTools(context, result, isImage, 2);
                    } else {
                        if (getAppIsInstalled(context, tool1PkgName)) {//上次保存的工具还存在 只需要一个
                            PackageManager pm = context.getPackageManager();
                            ActivityInfo info1 = pm.getActivityInfo(new ComponentName(tool1PkgName, tool1ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                            //第一个
                            result.add(new ShareImageItem.ShareImageItemData(tool1PkgName,
                                    tool1ActivityName, info1.loadIcon(pm), info1.loadLabel(pm).toString()));

                            //过滤掉 tool1PkgName  tool1ActivityName
                            getCountTools(context, result, isImage, 1, new String[]{tool1PkgName}, new String[]{tool1ActivityName});
                        } else {//上次保存的工具已经被卸载
                            getCountTools(context, result, isImage, 2);
                        }
                    }
                } else {//tool2存在
                    if (isDefaultTools(tool2PkgName, tool2ActivityName)) {
                        getCountTools(context, result, isImage, 2);
                    } else {
                        if (getAppIsInstalled(context, tool2PkgName)) {//上次保存的工具还存在 只需要一个
                            PackageManager pm = context.getPackageManager();
                            ActivityInfo info2 = pm.getActivityInfo(new ComponentName(tool2PkgName, tool2ActivityName), PackageManager.MATCH_DEFAULT_ONLY);
                            //第一个
                            result.add(new ShareImageItem.ShareImageItemData(tool2PkgName,
                                    tool2ActivityName, info2.loadIcon(pm), info2.loadLabel(pm).toString()));

                            //过滤掉 tool2PkgName  tool2ActivityName
                            getCountTools(context, result, isImage, 1, new String[]{tool2PkgName}, new String[]{tool2ActivityName});
                        } else {//上次保存的工具已经被卸载
                            getCountTools(context, result, isImage, 2);
                        }
                    }
                }
            }

        } catch (Throwable e) {
        }
    }

    /**
     * 获取需要数量的不重复的工具
     *
     * @param context
     * @param result
     * @param count   需要的个数
     * @param isImage 图片或者视频
     * @return 返回增加的数量
     */
    private static int getCountTools(Context context, List<ShareImageItem.ShareImageItemData> result, boolean isImage, int count) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = getAllSendImageOrVideoTools(context, isImage);
        int size = list.size();
        int num = 0;
        for (int i = 0; i < size; i++) {
            ResolveInfo info = list.get(i);
            if (!isDefaultTools(info.activityInfo.packageName, info.activityInfo.name)) {//不是默认的工具 则
                result.add(new ShareImageItem.ShareImageItemData(info.activityInfo.packageName,
                        info.activityInfo.name, info.loadIcon(pm), info.loadLabel(pm).toString()));
                num++;
                if (num == count) {//拿到两个工具
                    break;
                }
            } else {
                continue;
            }
        }
        return num;
    }

    /**
     * 获取需要数量的不重复的工具
     *
     * @param context
     * @param result
     * @param count        需要的个数
     * @param isImage      图片或者视频
     * @param pkgName      需要过滤的工具
     * @param activityName
     * @return 返回增加的数量
     */
    private static int getCountTools(Context context, List<ShareImageItem.ShareImageItemData> result, boolean isImage, int count, String[] pkgName, String[] activityName) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = getAllSendImageOrVideoTools(context, isImage);
        int size = list.size();
        int num = 0;
        for (int i = 0; i < size; i++) {
            ResolveInfo info = list.get(i);
            if (!isDefaultTools(info.activityInfo.packageName, info.activityInfo.name)
                    && !isFilterTools(info, pkgName, activityName)) {//不是默认的工具 且不在过滤的列表中
                result.add(new ShareImageItem.ShareImageItemData(info.activityInfo.packageName,
                        info.activityInfo.name, info.loadIcon(pm), info.loadLabel(pm).toString()));
                num++;
                if (num == count) {//拿到两个工具
                    break;
                }
            } else {
                continue;
            }
        }
        return num;
    }

    /**
     * 用于判断info 是不是需要过滤的里面的工具
     *
     * @param info
     * @param pkgName      不能为空 且和下一个个数必须一致
     * @param activityName 不能为空
     * @return
     */
    private static boolean isFilterTools(ResolveInfo info, String[] pkgName, String[] activityName) {
        int size = pkgName.length;
        boolean result = false;
        String filterPkgName = info.activityInfo.packageName;
        String filterActivityName = info.activityInfo.name;
        for (int i = 0; i < size; i++) {
            if (pkgName[i].equals(filterPkgName) && activityName[i].equals(filterActivityName)) {
                result = true;
                break;
            }
        }
        return result;
    }


    /**
     * 获取分享第二个界面的数据
     *
     * @return
     */
    public static List<ShareImageItem.ShareImageItemData> getAllShareTools(Context context, boolean isImage) {
        List<ShareImageItem.ShareImageItemData> result = new ArrayList<ShareImageItem.ShareImageItemData>();
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = getAllSendImageOrVideoTools(context, isImage);
        for (ResolveInfo ri : list) {
            if (!ri.activityInfo.packageName.equals(FileUtil.PACKAGE_NAME)) {
                result.add(new ShareImageItem.ShareImageItemData(ri.activityInfo.packageName,
                        ri.activityInfo.name, ri.loadIcon(pm), ri.loadLabel(pm).toString()));
            }
        }
        return result;
    }

    /**
     * 获取分享第二个界面的数据 (文案)
     *
     * @param context
     * @return
     */
    public static List<ShareImageItem.ShareImageItemData> getAllShareTextTools(Context context) {
        List<ShareImageItem.ShareImageItemData> result = new ArrayList<ShareImageItem.ShareImageItemData>();
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = getAllSendTextTools(context);
        for (ResolveInfo ri : list) {
            if (!ri.activityInfo.packageName.equals(FileUtil.PACKAGE_NAME)) {
                result.add(new ShareImageItem.ShareImageItemData(ri.activityInfo.packageName,
                        ri.activityInfo.name, ri.loadIcon(pm), ri.loadLabel(pm).toString()));
            }
        }
        return result;
    }


    private static List<ResolveInfo> getAllSendImageOrVideoTools(Context context, boolean isImage) {
        PackageManager pm = context.getPackageManager();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (isImage) {
            shareIntent.setType("image/*");
        } else {
            shareIntent.setType("video/*");
        }
        shareIntent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> list = pm.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return list;
    }

    private static List<ResolveInfo> getAllSendTextTools(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> list = pm.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return list;
    }

    /**
     * 用于jin验证
     *
     * @return
     */
    public static String getPkgName() {
        return App.getPackageName();
    }


    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    /**
     * 多图分享  图库增加
     */
    public static List<ShareImageItem.ShareImageItemData> getAllShareMutilMediaTools(Context context, int imageNum, int videoNum) {
        List<ShareImageItem.ShareImageItemData> result = new ArrayList<ShareImageItem.ShareImageItemData>();
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = getAllSendMutilMediaTools(context, imageNum, videoNum);
        for (ResolveInfo ri : list) {
            if (!ri.activityInfo.packageName.equals(FileUtil.PACKAGE_NAME)) {
                result.add(new ShareImageItem.ShareImageItemData(ri.activityInfo.packageName,
                        ri.activityInfo.name, ri.loadIcon(pm), ri.loadLabel(pm).toString()));
            }
        }
        return result;
    }

    /**
     * 获取分享多个Image / video的列表
     *
     * @param context
     * @param imageNum
     * @param videoNum
     * @return
     */
    private static List<ResolveInfo> getAllSendMutilMediaTools(Context context, int imageNum, int videoNum) {
        PackageManager pm = context.getPackageManager();
        Intent shareIntent = new Intent();
        if (imageNum > 1 || videoNum > 1 || (imageNum > 0 && videoNum > 0)) {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        } else {
            shareIntent.setAction(Intent.ACTION_SEND);
        }
        if (imageNum > 0 && videoNum > 0) {
            shareIntent.setType("media/*");
        } else if (videoNum > 0) {
            shareIntent.setType("video/*");
        } else {
            shareIntent.setType("image/*");
        }
        shareIntent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> list = pm.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return list;
    }

    /**
     * 普通的启动Activity分享多图或者视频的方法
     *
     * @param context
     * @param pkgName
     * @param appName
     * @param uris
     * @param imageNum
     * @param videoNum
     * @return
     */
    public static boolean startCommonShareMutilMediaActivity(final Context context, String pkgName, String appName
            , final ArrayList<Uri> uris, int imageNum, int videoNum) {
        if (uris == null || uris.size() == 0) {
            return false;
        }
        boolean flag = true;
        Intent shareIntent = new Intent();
        if (imageNum > 1 || videoNum > 1 || (imageNum > 0 && videoNum > 0)) {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        } else {
            shareIntent.setAction(Intent.ACTION_SEND);
        }
        shareIntent.setComponent(new ComponentName(pkgName, appName));
        if (imageNum > 0 && videoNum > 0) {
            shareIntent.setType("media/*");
        } else if (videoNum > 0) {
            shareIntent.setType("video/*");
        } else {
            shareIntent.setType("image/*");
        }
        if (shareIntent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            shareIntent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }
        try {
            context.startActivity(shareIntent);
        } catch (Throwable e) {//增加保护
            flag = false;
            List<ShareImageItem.ShareImageItemData> result = getAllShareMutilMediaTools(context, imageNum, videoNum);
            for (ShareImageItem.ShareImageItemData data : result) {
                if (data.getmPkgName().equals(pkgName)) {
                    shareIntent.setComponent(new ComponentName(data.getmPkgName(), data.getmActivityName()));
                    flag = true;
                    break;
                }
            }
            try {
                if (flag) {
                    context.startActivity(shareIntent);
                }
            } catch (Throwable e1) {
                flag = false;
            }
        }
        return flag;
    }

    //私密相册分享的实现

    public static boolean startPrivateShareActivity(final Context context, String pkgName, String appName, File f, boolean isImage) {
        if (f != null && f.exists()) {
            boolean flag = true;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setComponent(new ComponentName(pkgName, appName));
            if (isImage) {
                shareIntent.setType("image/*");
            } else {
                shareIntent.setType("video/*");
            }
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            try {
                context.startActivity(shareIntent);
            } catch (Throwable e) {//增加保护
                flag = false;
                List<ShareImageItem.ShareImageItemData> result = getAllShareTools(context, isImage);
                for (ShareImageItem.ShareImageItemData data : result) {
                    if (data.getmPkgName().equals(pkgName)) {
                        shareIntent.setComponent(new ComponentName(data.getmPkgName(), data.getmActivityName()));
                        flag = true;
                        break;
                    }
                }
                try {
                    if (flag) {
                        context.startActivity(shareIntent);
                    }
                } catch (Throwable e1) {
                    flag = false;
                }
            }

            return flag;
        } else {
            return false;
        }
    }


    //Instagram 私密相册分享的实现

    public static boolean startInstagramPrivateShareActivity(final Context context, String pkgName, String appName, File f, boolean isImage) {
        if (f != null && f.exists()) {
            boolean flag = true;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setComponent(new ComponentName(pkgName, appName));
            if (isImage) {
                shareIntent.setType("image/*");
            } else {
                shareIntent.setType("video/*");
            }
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "#zcamera");
            try {
                context.startActivity(shareIntent);
            } catch (Throwable e) {//增加保护
                flag = false;
                List<ShareImageItem.ShareImageItemData> result = getAllShareTools(context, isImage);
                for (ShareImageItem.ShareImageItemData data : result) {
                    if (data.getmPkgName().equals(pkgName)) {
                        shareIntent.setComponent(new ComponentName(data.getmPkgName(), data.getmActivityName()));
                        flag = true;
                        break;
                    }
                }
                try {
                    if (flag) {
                        context.startActivity(shareIntent);
                    }
                } catch (Throwable e1) {
                    flag = false;
                }
            }

            return flag;
        } else {
            return false;
        }
    }

    /**
     * 普通的启动Instagram 分享的方法
     *
     * @param context
     * @param pkgName
     * @param appName
     * @param u
     * @param isImage
     * @return
     */
    public static boolean startInstagramShareActivity(final Context context, String pkgName, String appName, final Uri u, boolean isImage) {
        boolean flag = true;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setComponent(new ComponentName(pkgName, appName));
        if (isImage) {
            shareIntent.setType("image/*");
        } else {
            shareIntent.setType("video/*");
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, u);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "#zcamera");

        try {
            context.startActivity(shareIntent);
        } catch (Throwable e) {//增加保护
            flag = false;
            List<ShareImageItem.ShareImageItemData> result = getAllShareTools(context, isImage);
            for (ShareImageItem.ShareImageItemData data : result) {
                if (data.getmPkgName().equals(pkgName)) {
                    shareIntent.setComponent(new ComponentName(data.getmPkgName(), data.getmActivityName()));
                    flag = true;
                    break;
                }
            }
            try {
                if (flag) {
                    context.startActivity(shareIntent);
                }
            } catch (Throwable e1) {
                flag = false;
            }
        }

        return flag;
    }
}
