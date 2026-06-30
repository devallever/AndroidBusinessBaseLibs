package com.allever.video.editor.function.online;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.allever.video.editor.function.DataManager;
import com.allever.video.editor.function.download.DownloadCallback;
import com.allever.video.editor.function.download.DownloadManager;
import com.allever.video.editor.function.download.OkDownloadExecutor;
import com.allever.video.editor.function.download.TaskInfo;
import com.android.absbase.App;
import com.android.absbase.helper.log.DLog;
import com.android.absbase.utils.FileUtils;
import com.android.absbase.utils.SpUtils;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.allever.video.editor.BuildConfig;
import com.allever.video.editor.utils.Base64;
import com.allever.video.editor.utils.EncryptConstant;
import com.allever.video.editor.utils.FileUtil;
import com.allever.video.editor.utils.MD5;
import com.allever.video.editor.ConfigManager;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OnlineDataManager {

    private static final String TAG = OnlineDataManager.class.getSimpleName();

    public static final String ID_ROOT = "0";
    public static final String ID_EFFECTS = "1";
    public static final String ID_TUTORIAL = "2";
    public static final String ID_STICKER = "101";
    public static final String ID_FILTER = "102";
    public static final String ID_FONT = "103";

    //根据配置而定
    public static final int TYPE_STICKER = 1;
    public static final int TYPE_FILTER = 2;
    public static final int TYPE_FONT = 3;
    public static final int TYPE_MUSIC = 7;


    public static final int IMG_TYPE_SMALL = 0;
    public static final int IMG_TYPE_MID = 1;
    public static final int IMG_TYPE_BIG = 2;

    private static final String URL_SEPARATOR = "/";

    private static final String CONFIG_NAME = "config";
    //http:/xxxxx/%s/%s
    //debug
    //https://raw.githubusercontent.com/devallever/DataProject/master/data/ftkj/%s/%s
    private static final String CONFIG_URL_FORMAT_ENC_DEBUG = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNvbnRlbnQuY29tL2RldmFsbGV2ZXIvRGF0YVByb2plY3QvbWFzdGVyL2RhdGEvZnRrai8lcy8lcw==";
    //release
    private static final String CONFIG_URL_FORMAT_ENC = "aHR0cDovL3d3dy5hbGxpbmFpLmdsb2JhbDoyMzQ1Ni8lcy8lcw==";


    /***
     * <id, OnlineDataBean > 保存所有节点
     */
    private Map<String, OnlineDataBean> mIdOnlineDataBeanMap = new HashMap<>();

    /**
     * <pkg, EffectBean> 所有具体特效，
     */
    private Map<String, OnlineEffectBean> mPkgOnlineEffectBeanMap = new HashMap<>();

//    /***
//     * <type, List<EffectBean>> 某类型的特效列表
//     */
//    private Map<Integer, List<EffectBean>> mEffectBeanList = new LinkedHashMap<>();

//    private Map<String, OnlineEffectBean> mStickerMap = new LinkedHashMap<>();
//    private Map<String, OnlineEffectBean> mFilterMap = new LinkedHashMap<>();
//    private Map<String, OnlineEffectBean> mFontMap = new LinkedHashMap<>();
//    private Map<String, OnlineEffectBean> mMusicMap = new LinkedHashMap<>();

    private Map<String, LocalDataBean> mLocalStickerMap = new LinkedHashMap<>();
    private Map<String, LocalDataBean> mLocalFilterMap = new LinkedHashMap<>();
    private Map<String, LocalDataBean> mLocalFontMap = new LinkedHashMap<>();
    private Map<String, LocalDataBean> mLocalMusicMap = new LinkedHashMap<>();

    private Map<String, String> mFileNameMd5Map = new HashMap<>();

    private OnlineDataBean mRootBean;

    private OnlineDataManager() {
    }


    public static OnlineDataManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public String getConfigUrl() {
//        if (BuildConfig.DEBUG){
//            String configUrlFormat = EncryptConstant.decodeBase64(CONFIG_URL_FORMAT_ENC_DEBUG);
//            return String.format(configUrlFormat, "photoeditor", CONFIG_NAME);
//        }else {
//            String configUrlFormat = EncryptConstant.decodeBase64(CONFIG_URL_FORMAT_ENC);
//            return String.format(configUrlFormat, BuildConfig.PRODUCT_CRC, CONFIG_NAME);
//        }
        String configUrlFormat = EncryptConstant.decodeBase64(CONFIG_URL_FORMAT_ENC);
        return String.format(configUrlFormat, "slfajsl", CONFIG_NAME);
    }

    private static class SingletonHolder {
        private static final OnlineDataManager INSTANCE = new OnlineDataManager();
    }

    public OnlineDataBean parse(File jsonFile) {
        OnlineDataBean storeOnlineBean = null;
        if (jsonFile == null || !jsonFile.exists()) {
            return null;
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile)));
            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String result = stringBuilder.toString();
            storeOnlineBean = parse(result);
            closeStream(bufferedReader);
            return storeOnlineBean;
        } catch (Exception e) {
            e.printStackTrace();
            closeStream(bufferedReader);
            return null;
        }
    }

    private void closeStream(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /***
     * parent: /ssss
     * child: /aaa
     * result = /ssss/aaa
     *
     * @param parent
     * @param child
     * @return
     */
    private String mergeUrlPath(String parent, String child) {
        if (TextUtils.isEmpty(child)) {
            return parent;
        }

        StringBuilder pathBuilder = new StringBuilder();
//        String path = "";

        //如果为空直接引用上级的path
        if (TextUtils.isEmpty(child)) {
            return parent;
        } else {
            //拼接
            if (parent.endsWith(URL_SEPARATOR)) {
                if (child.startsWith(URL_SEPARATOR)) {
//                    path = parent +  child.substring(1);
                    pathBuilder.append(parent).append(child.substring(1));
                } else {
//                    path = parent + child;
                    pathBuilder.append(parent).append(child);
                }
            } else {
                if (!child.startsWith(URL_SEPARATOR)) {
//                    path = parent + URL_SEPARATOR + child;
                    pathBuilder.append(parent).append(URL_SEPARATOR).append(child);
                } else {
//                    path = parent + child;
                    pathBuilder.append(parent).append(child);
                }
            }
        }

        return pathBuilder.toString();
    }

    private List<String> getRealImgUrl(OnlineDataBean bean, String urlPath, int type) {
        if (bean == null) {
            return null;
        }
        List<String> realImgUrlList = null;
        List<String> imgUrlList = null;

        switch (type) {
            case IMG_TYPE_SMALL:
                imgUrlList = parseText2List(bean.getSmallImg());
                break;
            case IMG_TYPE_MID:
                imgUrlList = parseText2List(bean.getMediumImg());
                break;
            case IMG_TYPE_BIG:
                imgUrlList = parseText2List(bean.getBigImg());
                break;
            default:
                break;
        }
        if (imgUrlList != null && imgUrlList.size() > 0) {
            realImgUrlList = new ArrayList<>(imgUrlList.size());
            for (String smallImgUrl : imgUrlList) {
                String realImgUrl = smallImgUrl;
                if (URLUtil.isNetworkUrl(realImgUrl)) {
                    realImgUrlList.add(realImgUrl);
                } else {
                    realImgUrl = mergeUrlPath(urlPath, smallImgUrl);
                    realImgUrlList.add(realImgUrl);
                }
            }
        }

        return realImgUrlList;
    }


    private void modifyChildParms(OnlineDataBean parent, OnlineDataBean child) {
        if (parent == null || child == null) {
            return;
        }

        mIdOnlineDataBeanMap.put(child.getId(), child);

        DLog.d(TAG, "parent id = " + parent.getId());
        DLog.d(TAG, "child id = " + child.getId());

        child.setHostname(parent.getHostname());
        String childPath = child.getPath();
        DLog.d(TAG, "id = " + child.getId() + " childPath = " + childPath);
        DLog.d(TAG, "id = " + child.getId() + " parentPath = " + parent.getPath());
        String path = mergeUrlPath(parent.getPath(), childPath);
        DLog.d(TAG, "id = " + child.getId() + " after mergeurl = " + path);
        child.setPath(path);

        OnlineEffectBean onlineEffectBean = new OnlineEffectBean();
        onlineEffectBean.setId(child.getId());
        onlineEffectBean.setType(child.getType());
        onlineEffectBean.setHostname(child.getHostname());
        onlineEffectBean.setPath(child.getPath());
        onlineEffectBean.setName(child.getName());
        onlineEffectBean.setPkgName(child.getPkgName());

        String urlPath = mergeUrlPath(child.getHostname(), child.getPath());
        String downloadUrl = child.getDownloadUrl();
        if (URLUtil.isNetworkUrl(downloadUrl)) {
            onlineEffectBean.setDownloadUrl(downloadUrl);
        } else {
            downloadUrl = mergeUrlPath(urlPath, child.getDownloadUrl());
            onlineEffectBean.setDownloadUrl(downloadUrl);
        }

        onlineEffectBean.setNeedBuy(child.isNeedBuy());
        onlineEffectBean.setSmallImg(child.getSmallImg());
        onlineEffectBean.setMediumImg(child.getMediumImg());
        onlineEffectBean.setBigImg(child.getBigImg());

        onlineEffectBean.setSmallImgUrlList(getRealImgUrl(child, urlPath, IMG_TYPE_SMALL));
        onlineEffectBean.setMidImgUrlList(getRealImgUrl(child, urlPath, IMG_TYPE_MID));
        onlineEffectBean.setBigImgUrlList(getRealImgUrl(child, urlPath, IMG_TYPE_BIG));

        String tutorialUrl = mergeUrlPath(urlPath, child.getTutorialUrl());
        onlineEffectBean.setTutorialUrl(tutorialUrl);

        onlineEffectBean.setTutorialDescription(child.getTutorialDescription());
        onlineEffectBean.setRecommends(child.getRecommends());
        onlineEffectBean.setFlow(child.getFlow());

        onlineEffectBean.setBuildin(false);
        onlineEffectBean.setAssetName("");
        onlineEffectBean.setResIconName(0);

        String pkg = onlineEffectBean.getPkgName();
        if (!TextUtils.isEmpty(pkg)) {
            DLog.d(TAG, "modifyChildParms() pkg = " + pkg);
            mPkgOnlineEffectBeanMap.put(pkg, onlineEffectBean);
        }

        //分类
        switch (onlineEffectBean.getType()) {
            case TYPE_STICKER:
                if (!TextUtils.isEmpty(pkg)) {
//                    mStickerMap.put(pkg, onlineEffectBean);
                    mLocalStickerMap.put(pkg, LocalDataBean.Companion.toLocalBean(onlineEffectBean));
                }
                break;
            case TYPE_FILTER:
                if (!TextUtils.isEmpty(pkg)) {
//                    mFilterMap.put(pkg, onlineEffectBean);
                    mLocalFilterMap.put(pkg, LocalDataBean.Companion.toLocalBean(onlineEffectBean));
                }
                break;
            case TYPE_FONT:
                if (!TextUtils.isEmpty(pkg)) {
//                    mFontMap.put(pkg, onlineEffectBean);
                    mLocalFontMap.put(pkg, LocalDataBean.Companion.toLocalBean(onlineEffectBean));
                }
                break;
            case TYPE_MUSIC:
                if (!TextUtils.isEmpty(pkg)) {
//                    mMusicMap.put(pkg, onlineEffectBean);
                    mLocalMusicMap.put(pkg, LocalDataBean.Companion.toLocalBean(onlineEffectBean));
                }
                break;
            default:
                break;
        }

        return;
    }

    /***
     * @param jsonStr encoded
     * @return
     */
    public OnlineDataBean parse(String jsonStr) {
        try {
            String subJson = jsonStr.substring(2, jsonStr.length() - 2);
            String decodeJson = new String(Base64.decode(subJson));
            mRootBean = new Gson().fromJson(decodeJson, OnlineEffectBean.class);
//            addStoreOnlineBean2Maps(mRootBean);
            //遍历root
            // todo 1.遍历每个节点, 把上一级的hostName赋值给下一级
            // todo 2.组装url
            // todo 3.分类， 保存 pkg -> effectBean

            mIdOnlineDataBeanMap.put(mRootBean.getId(), mRootBean);
            List<OnlineDataBean> childList = mRootBean.getChild();
            if (childList != null && childList.size() > 0) {
                for (int i = 0; i < childList.size(); i++) {
                    //二级 effect, tutorial, mainflow
                    OnlineDataBean child2 = childList.get(i);
                    modifyChildParms(mRootBean, child2);
                    List<OnlineDataBean> childList2 = child2.getChild();
                    if (childList2 != null && childList2.size() > 0) {
                        for (int j = 0; j < childList2.size(); j++) {
                            //三级，sticker, font, filter....
                            OnlineDataBean child3 = childList2.get(j);
                            modifyChildParms(child2, child3);
                            List<OnlineDataBean> childList3 = child3.getChild();
                            if (childList3 != null && childList3.size() > 0) {
                                for (int k = 0; k < childList3.size(); k++) {
                                    //四级，具体 sticker, font, filter.... 或分类
                                    OnlineDataBean child4 = childList3.get(k);
                                    modifyChildParms(child3, child4);
                                    List<OnlineDataBean> childList4 = child4.getChild();
                                    if (childList4 != null && childList4.size() > 0) {
                                        for (int m = 0; m < childList4.size(); m++) {
                                            //五级，具体 sticker, font, filter....
                                            OnlineDataBean child5 = childList4.get(m);
                                            modifyChildParms(child4, child5);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


//            JSONObject rootJsonObj = new JSONObject(jsonStr);
//            String id = rootJsonObj.optString("id");
//            int type = rootJsonObj.optInt("type");
//            String hostname = rootJsonObj.optString("hostname");
//            String path = rootJsonObj.optString("path");
//            JSONArray rootChild = rootJsonObj.getJSONArray("child");
//            List<OnlineDataBean> childList = new ArrayList<>();
//            if (rootChild != null){
//                for (int i=0; i<rootChild.length(); i++){
//                    //二级：effect , Tutorial, mainflow
//                    JSONObject secondJsonObj = rootChild.optJSONObject(i);
//
//                }
//            }
//            mRootBean = new OnlineDataBean();
//            mRootBean.setHostname(hostname);
//            mRootBean.setId(id);
//            mRootBean.setType(type);
//            mRootBean.setPath(path);
//            mRootBean.setChild(childList);
            // 把OnlineDataBean转成EffectBean
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mRootBean;
    }


//    public Map<String, OnlineEffectBean> getStickerBeanList(){
//        return mStickerMap;
//    }

    public Map<String, LocalDataBean> getLocalStickerBeanList() {
        return mLocalStickerMap;
    }

    public OnlineEffectBean getEffectBeanByPkg(String pkg) {
        return mPkgOnlineEffectBeanMap.get(pkg);
    }


    public Map<String, LocalDataBean> getLocalMusicBeanList() {
        return mLocalMusicMap;
    }

    public Map<String, LocalDataBean> getLocalFontBeanList() {
        return mLocalFontMap;
    }


    //    private void addStoreOnlineBean2Maps(OnlineDataBean bean) {
//        if (bean == null || mIdOnlineDataBeanMap == null) {
//            return;
//        }
//        String id = bean.getId();
////        if (mIdOnlineDataBeanMap.containsKey(id)) {
////            RLog.e(TAG, "repeat key");
////        }
//        mIdOnlineDataBeanMap.put(id, bean);
//        List<OnlineDataBean> childBeans = bean.getChild();
//        if (childBeans != null && childBeans.size() > 0) {
//            for (OnlineDataBean childBean : childBeans) {
//                addStoreOnlineBean2Maps(childBean);
//            }
//        }
//    }
//
    public OnlineDataBean getOnlineDataBeanById(String id) {
        if (mIdOnlineDataBeanMap != null) {
            return mIdOnlineDataBeanMap.get(id);
        } else {
            return null;
        }
    }

    public OnlineDataBean getRootStoreOnlineBean() {
        return mRootBean;
    }


    /***
     * 把逗号分隔的字符串解析成列表,逗号分隔
     * @param text
     * @return
     */
    public List<String> parseText2List(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        List<String> valueList = new ArrayList<>();
        if (text.contains(",")) {
            String[] values = text.split(",");
            valueList.addAll(Arrays.asList(values));
        } else {
            valueList.add(text);
        }
        return valueList;
    }

    public String getMd5FromFileName(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }
        String result = mFileNameMd5Map.get(fileName);
        if (TextUtils.isEmpty(result)) {
            String md5 = MD5.getMD5Str(fileName);
            mFileNameMd5Map.put(fileName, md5);
            return md5;
        }
        return result;
    }

    public boolean checkDownloaded(OnlineEffectBean bean) {
        if (bean == null) {
            return false;
        }
        String fileName = FileUtil.getFileName(bean.getDownloadUrl());
        String md5FileName = OnlineDataManager.getInstance().getMd5FromFileName(fileName);
        String apkPath = DataManager.RES_STICKER_DIR + File.separator + md5FileName;
        return FileUtil.isExistsFile(apkPath);
    }

//    public ExtraBean toExtraBean(OnlineDataBean bean, int type, String filePath) {
//        return ExtraBean.create(bean.getName()
//                , bean.getPkgName()
//                , 0
//                , 0
//                , false
//                , 1
//                , type
//                , filePath
//                , true);
//    }


    private static final String SP_KEY_CONFIG_DOWNLOAD_TIME = "time";
    private static final String CONFIG_SP_NAME = "config";

    private static final String mConfigDir = DataManager.CONFIG_DIR + File.separator;
    private static final String mTempDir = DataManager.TEMP_DIR + File.separator;
    private static final String mConfigFileName = DataManager.CONFIG_FILE_NAME;
    private static final String mEffectFileName = DataManager.EFFECT_FILE_NAME;

    private static long mEffectLastUpdateTime = -1L;
    private static ConfigBean mRootConfigBean = null;

    private boolean mIsPauseConfig = false;
    private boolean mIsPauseEffect = false;

    private DownloadCallback mDownloadConfigCallback = new DownloadCallback() {
        @Override
        public void onStart() {
            mIsPauseConfig = true;
        }

        @Override
        public void onConnected(long totalLength) {
        }

        @Override
        public void onProgress(long current, long totalLength) {
            DLog.d(TAG, "onProgress: config file " + current + " / " + totalLength);
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
        }

        @Override
        public void onCompleted(TaskInfo taskInfo) {
            DLog.d(TAG, "Download config finish");
            try {
                mIsPauseConfig = false;
                copyAndDeleteTmpFile(taskInfo);
                //重新加载配置数据
                mRootConfigBean = null;
                mRootConfigBean = loadRootConfig();
                //纪录下载的时间戳
                SpUtils.obtain(CONFIG_SP_NAME).save(SP_KEY_CONFIG_DOWNLOAD_TIME, System.currentTimeMillis());
                //处理Effect_info
                handleEffect();
                DownloadManager.getInstance().removeTask(taskInfo.getUrl());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
            DLog.d(TAG, "onError: download config file error");
            //下载出错
            if (isNetWorkAvailable(App.getContext()) && checkErrorTimeOut(ERROR_TYPE_CONFIG)) {
                //网络可用，并超时
                downloadConfig();
                ConfigManager.INSTANCE.setLastRetryDownloadConfigTime(System.currentTimeMillis());
            } else {
                //如果存在，加载旧的Config
                if (checkConfigFileExist()) {
                    mRootConfigBean = loadRootConfig();
                }
                if (checkEffectFileExist()) {
                    loadEffect();
                }
            }

        }
    };


    private DownloadCallback mDownloadEffectCallback = new DownloadCallback() {
        @Override
        public void onStart() {
            mIsPauseEffect = false;
        }

        @Override
        public void onConnected(long totalLength) {
        }

        @Override
        public void onProgress(long current, long totalLength) {
            DLog.d(TAG, "onProgress: effect file " + current + " / " + totalLength);
        }

        @Override
        public void onPause(TaskInfo taskInfo) {
            mIsPauseEffect = true;
        }

        @Override
        public void onCompleted(TaskInfo taskInfo) {
            //下载完成
            DLog.d(TAG, "Download effect finish");
            DownloadManager.getInstance().removeTask(taskInfo.getUrl());
            try {
                copyAndDeleteTmpFile(taskInfo);
                //解析
                loadEffect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Exception e) {
            DLog.d(TAG, "onError: download effect file error");
            //错误，重试
            if (isNetWorkAvailable(App.getContext()) && checkErrorTimeOut(ERROR_TYPE_EFFECT)) {
                //网络可用，并超时
                downloadEffect();
                ConfigManager.INSTANCE.setLastRetryDownloadEffectTime(System.currentTimeMillis());
            } else {
                //存在就加载旧的effect
                if (checkEffectFileExist()) {
                    loadEffect();
                }
            }
        }
    };

    public void download() {
        handleConfig();
    }

    public void pauseDownload() {
        DownloadManager.getInstance().pauseAllTask();
    }

    public void restartDownloadConfig() {
        if (mIsPauseConfig) {
            downloadConfig();
        }
        if (mIsPauseEffect) {
            downloadEffect();
        }
    }

    private void handleConfig() {
        if (checkConfigFileExist()) {
            DLog.d(TAG, "handleConfig: exist config file");
            //存在
            mRootConfigBean = loadRootConfig();
            if (mRootConfigBean != null) {
                mEffectLastUpdateTime = mRootConfigBean.getLastUpdateTime();
            }
            if (checkConfigTimeOut()) {
                //超时
                downloadConfig();
            } else {
                handleEffect();
            }
        } else {
            //不存在
            DLog.d(TAG, "handleConfig: not exist config file");
            downloadConfig();
        }
    }

    private void handleEffect() {
        if (checkEffectFileExist()) {
            //存在
            if (mRootConfigBean != null) {
                if (mEffectLastUpdateTime != mRootConfigBean.getLastUpdateTime()) {
                    //不相等，修改过
                    downloadEffect();
                } else {
                    loadEffect();
                }
            }
        } else {
            //不存在
            downloadEffect();
        }
    }

    private void downloadConfig() {
        DLog.d(TAG, "config url = ${OnlineDataManager.getInstance().configUrl}");
        DownloadManager.getInstance().start(new OkDownloadExecutor(mConfigFileName, mTempDir, OnlineDataManager.getInstance().getConfigUrl(), mDownloadConfigCallback));
    }

    private void downloadEffect() {
        //根据versionCode 获取 对应版本的Config
        ConfigBean versionConfigBean = getVersionConfig(mRootConfigBean);
        if (versionConfigBean != null) {
            DLog.d(TAG, "versionCode = " + versionConfigBean.getVersion());
            String url = versionConfigBean.getUrl();
            DLog.d(TAG, "effect url = " + url);
            DownloadManager.getInstance().start(new OkDownloadExecutor(mEffectFileName, mTempDir, url, mDownloadEffectCallback));
        }
    }

    private ConfigBean getVersionConfig(ConfigBean configBean) {
        if (configBean == null) {
            return null;
        }

        String rootVersionText = configBean.getVersion();
        if (TextUtils.isEmpty(rootVersionText)) {
            return null;
        }

        ConfigBean versionConfigBean = null;

        // - ：表示区间
        //1. 先解析 “,”
        //2. 再解析 “-”
        try {
            //第一次遍历根的version
            if (rootVersionText.contains(",")) {
                //1-3，5，7-10
                //从RootConfig解析出来
                String[] rootVersionArr = rootVersionText.split(",");
                for (String versionText : rootVersionArr) {
                    //1-3     7-10
                    if (versionText.contains("-")) {
                        //含有“-”，继续分隔，获取区间
                        String[] subVersionArr = versionText.split("-");
                        //区间
                        if (subVersionArr.length >= 2) {
                            int startIndex = Integer.valueOf(subVersionArr[0]);
                            int endIndex = Integer.valueOf(subVersionArr[1]);
                            //遍历区间
                            for (int i = startIndex; i <= endIndex; i++) {
                                DLog.d(TAG, "versionCode = " + i);
                                if (BuildConfig.VERSION_CODE == i) {
                                    //versionConfigBean =  configBean;
                                    return configBean;
                                }
                            }
                        } else {
                            //格式错误
                            return null;
                        }
                    } else {
                        //不含“-”，单个版本号 5
                        DLog.d(TAG, "versionCode = " + versionText);
                        if (BuildConfig.VERSION_CODE == Integer.valueOf(versionText)) {
                            //versionConfigBean =  configBean;
                            return configBean;
                        }
                    }
                }
            } else if (rootVersionText.contains("-")) {
                //如果不存在“,”分隔，则只有一个区间
                //1-10
                String[] rootVersionArr = rootVersionText.split("-");
                if (rootVersionArr.length >= 2) {
                    int startIndex = Integer.valueOf(rootVersionArr[0]);
                    int endIndex = Integer.valueOf(rootVersionArr[1]);
                    //遍历区间
                    for (int i = startIndex; i <= endIndex; i++) {
                        DLog.d(TAG, "versionCode = " + i);
                        if (BuildConfig.VERSION_CODE == i) {
                            //versionConfigBean =  configBean;
                            return configBean;
                        }
                    }
                } else {
                    //格式有误
                    return null;
                }
            } else {
                //都没有，只能是单个 1
                DLog.d(TAG, "versionCode = " + configBean.getVersion());
                if (BuildConfig.VERSION_CODE == Integer.valueOf(configBean.getVersion())) {
                    //versionConfigBean = configBean;
                    return configBean;
                }
            }

            //如果versionConfigBean为空，表示当前版本不存在rootConfigBean的version中，继续遍历rootConfigBean的子Config
            List<ConfigBean> childConfigBeanList = configBean.getChild();
            if (childConfigBeanList == null || childConfigBeanList.size() == 0) {
                return null;
            }

            for (ConfigBean bean : childConfigBeanList) {
                versionConfigBean = getVersionConfig(bean);
                if (versionConfigBean != null) {
                    return versionConfigBean;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionConfigBean;
    }

    private boolean checkConfigTimeOut() {
        if (mRootConfigBean == null) {
            return false;
        }

        long timeOut = mRootConfigBean.getTime();
        long lastSaveTime = SpUtils.obtain(CONFIG_SP_NAME).get(SP_KEY_CONFIG_DOWNLOAD_TIME, 0L);
        long curTime = System.currentTimeMillis();
        long timeInterval = (curTime - lastSaveTime) / 1000 / 60;
        return timeInterval > timeOut;
    }

    private static final int ERROR_TYPE_CONFIG = 1;
    private static final int ERROR_TYPE_EFFECT = 2;
    //单位：分钟
    private static final int RETRY_TIME_INTERVAL = 30;

    /***
     * 判断重试超时
     * @param type
     * @return
     */
    private boolean checkErrorTimeOut(int type) {
        long lastRetryTimeMills = 0L;
        if (type == ERROR_TYPE_CONFIG) {
            lastRetryTimeMills = ConfigManager.INSTANCE.getLastRetryDownloadConfigTime();
        } else {
            lastRetryTimeMills = ConfigManager.INSTANCE.getLastRetryDownloadEffectTime();
        }

        if (lastRetryTimeMills == 0) {
            //没有被赋过值，就是没有重试过
            return true;
        }

        long curTime = System.currentTimeMillis();
        long timeInterval = (curTime - lastRetryTimeMills) / 1000 / 60;
        return timeInterval > RETRY_TIME_INTERVAL;
    }

    private ConfigBean loadRootConfig() {
        if (mRootConfigBean != null) {
            return mRootConfigBean;
        }
        return ConfigBean.parse(mConfigDir + mConfigFileName);
    }

    private void loadEffect() {
        OnlineDataManager.getInstance().parse(new File(mConfigDir, mEffectFileName));
    }

    private boolean checkConfigFileExist() {
        String filePath = mConfigDir + mConfigFileName;
        return FileUtils.isExistFile(filePath);
    }

    private boolean checkEffectFileExist() {
        String filePath = mConfigDir + mEffectFileName;
        return FileUtils.isExistFile(filePath);
    }

    private void copyAndDeleteTmpFile(TaskInfo taskInfo) throws IOException {
        if (taskInfo == null) {
            return;
        }
        String tmpFilePath = taskInfo.getPath() + taskInfo.getFileName();
        FileUtil.copyAndDeleteFile(tmpFilePath, mConfigDir);
    }

    /**
     * 检测手机网络是否可用的方法
     *
     * @return 可用返回TRUE, 否则返回FALSE
     */
    private boolean isNetWorkAvailable(Context context) {
        boolean result = false;
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    result = true;
                }
            }
        }
        return result;
    }
}
