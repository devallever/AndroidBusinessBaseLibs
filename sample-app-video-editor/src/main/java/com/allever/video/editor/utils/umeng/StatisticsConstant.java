package com.allever.video.editor.utils.umeng;

/**
 *
 */

public interface StatisticsConstant {
    public static final int SOURCE_TYPE_EDIT = 1;
    public static final int SOURCE_TYPE_VIDEO_STASH = 2;
    public static final int SOURCE_TYPE_IMAGE_STASH = 3;
    public static final int SOURCE_TYPE_COLLAGE = 4;
    public static final int SOURCE_TYPE_FREESTYLE_FILTER = 5; //freestyle二级滤镜 只应用滤镜

//    public static final String switch_status = "";//"status";//开关状态
//    public static final String switch_status_open = "";//"open";//开关状态
//    public static final String switch_status_close = "";//"close";//开关状态
//
//    // 结构化统计的事件: 1级
//    public static final String EVENT_MAIN = "";//"m";//主界面
//    public static final String EVENT_notification = "";//"n";//通知栏
//    public static final String EVENT_lock = "";//"l";//锁屏
//    public static final String EVENT_widget = "";//"w";//桌面小部件
//    public static final String EVENT_setting = "";//"s";//设置页
//    public static final String EVENT_alert = "";//"a";//弹窗
//    public static final String EVENT_function = "";//"f";//功能
//
//    // 结构化统计的事件: 2级
//    // 主界面
//    public static final String main_show = "";//"show"; // 点击手电开关按钮
//    public static final String main_flash_switch = "";//"switch"; // 点击手电开关按钮
//    public static final String flash_strobe = "";//"strobe"; // 切换模式操作
//    public static final String flash_mode_0 = "";//"m0"; // 使用默认模式,以用户当天切换至，并开启灯光为标准
//    public static final String flash_mode_1 = "";//"m1"; // 使用模式1
//    public static final String flash_mode_2 = "";//"m2"; // 使用模式2
//    public static final String flash_mode_3 = "";//"m3"; // 使用模式3
//    public static final String flash_mode_s = "";//"ms"; // 使用SOS模式
//    public static final String settings_icon = "";//"settings"; // 点击设置入口按钮
//    public static final String giftbox = "";//"giftbox"; // 点击礼盒
//    // 通知栏
//    public static final String notification_flash_swtich = "";//"flash"; // 点击手电开关按钮
//    public static final String notification_wifi_swtich = "";//"wifi"; // 点击Wifi开关按钮
//    public static final String notification_4g_swtich = "";//"4g"; // 点击移动流量按钮
//    public static final String notification_rotate_swtich = "";//"rotate"; // 点击屏幕翻转按钮
//    public static final String notification_bluetooth_swtich = "";//"bluetooth"; // 点击蓝牙模式按钮
//    public static final String notification_clean_swtich = "";//"clean"; // 点击清理按钮（待定）
//    public static final String notification_function_display = "";//"display"; // 功能通知栏展示
//
//    // 锁屏界面
//    public static final String lock_flash_switch = "";//"flash"; // 点击手电开关按钮
//    public static final String lock_clean_icon = "";//"clean"; // 点击清理按钮（待定）
//    public static final String lock_sos_icon = "";//"sos"; // 点击SOS按钮
//    public static final String lock_camera_icon = "";//"camera"; // 点击相机按钮
//    public static final String lock_call_icon = "";//"call"; // 点击电话按钮
//    public static final String lock_display = "";//"display"; // 锁屏展示
//    public static final String lock_type_chargelock_display = "";//"chargelock"; // 锁屏展示
//    public static final String lock_type_lockscreen_display = "";//"lockscreen"; // 锁屏展示
//    // 桌面小部件
//    public static final String widget_flash_switch_0 = "";//"s0"; // 点击手电开关按钮（条状小部件）
//    public static final String widget_flash_switch_1 = "";//"s1"; // 点击手电开关按钮（按钮小部件）
//    public static final String widget_wifi_icon = "";//"wifi"; // 点击WiFi按钮（条状）
//    public static final String widget_4g_icon = "";//"4g"; // 点击移动网络按钮（条状）
//    public static final String widget_rotate_icon = "";//"rotate"; // 点击屏幕翻转按钮（条状）
//    public static final String widget_bar_display = "";//"bar"; // 条状小部件展示
//    public static final String widget_button_display = "";//"button"; // 按钮小部件展示
//    // 设置页
//    public static final String settings_call_icon = "";//"call"; // 点击来电提醒开关
//    public static final String settings_notification_icon = "";//"notice"; // 点击通知提醒开关
//    public static final String settings_notificationbar_icon = "";//"noticebar"; // 点击通知栏开关
//    public static final String settings_screenlock_icon = "";//"screenlock"; // 点击通知栏开关
//    public static final String settings_feedback_icon = "";//"feedback"; // 点击意见反馈入口
//    public static final String settings_update_icon = "";//"update"; // 点击检查更新入口
//    public static final String settings_about_icon = "";//"about"; // 点击关于入口
//    public static final String settings_wifi_scan_icon = "";//"wifi"; // 点击wifi自动扫描
//
//    // 弹窗
//    public static final String alert_rate_display = "";//"display"; // 评分界面展示
//    public static final String alert_rate_like = "";//"like"; // 点击“喜欢”按钮
//    public static final String alert_rate_later = "";//"later"; // 点击“不喜欢”按钮
//    public static final String alert_rate_close = "";//"close"; // 评分引导关闭
//    // public static final String 消息弹窗展示 = "";//"消息弹窗展示"; // 消息弹窗展示
//    // public static final String 消息弹窗点击 = "";//"消息弹窗点击"; // 消息弹窗点击
//
//
//    // 结构化统计的事件: 3级
////    public static final String EVENT_3_country = "";//"c";//国家
////    public static final String EVENT_3_version = "";//"v";//版本


    // 通知栏消息相关
    public static final String UMENG_EVENT_message = "";//"msg"; // 通知栏消息id
    public static final String UMENG_MESSAGE_TYPE_display = "";//"display";// 通知栏消息展示
    public static final String UMENG_MESSAGE_TYPE_click = "";//"click";// 通知栏消息点击
    public static final String UMENG_MESSAGE_TYPE_ignore = "";//"ignore";// 通知栏消息忽略
    public static final String UMENG_MESSAGE_TYPE_close = "";//"close";// 通知栏消息关闭
    public static final String UMENG_MESSAGE_CLICK_TYPE_app = "";//"app"; // 通知栏消息打开app
    public static final String UMENG_MESSAGE_CLICK_TYPE_url = "";//"url"; // 通知栏消息打开网页
    public static final String UMENG_MESSAGE_CLICK_TYPE_activity = "";//"activity";// 通知栏消息打开activity
    public static final String UMENG_MESSAGE_CLICK_TYPE_custom = "";//"custom";// 通知栏消息点击自定义

    // umeng 自定义消息
    public static final String UMENG_EVNET_message_custom = "";//"msg_custom"; // umeng自定义消息
    public static final String UMENG_message_custom_type_obtain = "";//"obtain"; // umeng自定义消息

//    // 商业化统计
//
//    // 买量信息
//    public static final String UMENG_EVENT_BUY_USER = "";//"buyuser"; // 通知栏消息id
//
//    // 功能相关统计
//    public static final String UMENG_FUNCTION_WIFI = "";//"wifi"; // wifi功能相关统计
//    public static final String UMENG_FUNCTION_WIFI_START_SCAN = "";//"start_scan"; // wifi功能相关统计
//    public static final String UMENG_FUNCTION_WIFI_RISKY = "";//"risky"; // wifi功能相关统计
//    public static final String UMENG_FUNCTION_WIFI_SAFED = "";//"safed"; // wifi功能相关统计



    // 自定义事件统计：

    public static final String EVENT_KEY_TYPE = "";//"type";
    public static final String EVENT_KEY_ACTION = "";//"action";


    // 设置项：
    public static final String EVENT_SETTINGS_INCALL = "";//"incall";
    public static final String EVENT_SETTINGS_INCALL_LED_MODE = "";//"led_mode";
    public static final String EVENT_SETTINGS_INCALL_LED_SWITCH = "";//"led_switch";

    public static final String EVENT_SETTINGS_CALL_ICON = "";//"call"; // 点击来电提醒开关
    public static final String EVENT_SETTINGS_NOTIFICATION_ICON = "";//"notice"; // 点击通知提醒开关
    public static final String EVENT_SETTINGS_NOTIFICATIONBAR_ICON = "";//"noticebar"; // 点击通知栏开关
    public static final String EVENT_SETTINGS_SCREENLOCK_ICON = "";//"screenlock"; // 点击通知栏开关
    public static final String EVENT_SETTINGS_FEEDBACK_ICON = "";//"feedback"; // 点击意见反馈入口
    public static final String EVENT_SETTINGS_UPDATE_ICON = "";//"update"; // 点击检查更新入口
    public static final String EVENT_SETTINGS_ABOUT_ICON = "";//"about"; // 点击关于入口
    public static final String EVENT_SETTINGS_WIFI_ICON = "";//"wifi"; // 点击wifi自动扫描
    public static final String EVENT_SETTINGS_IG_ICON = "";//"campage_more_ig_click"; // 点击wifi自动扫描


    // 功能：来电闪屏
    public static final String EVENT_FUNC_INCALL_SHOW = "";//"incall_show";
    public static final String EVENT_FUNC_INCALL_KEY_STYLE = "";//"style";
    public static final String EVENT_FUNC_INCALL_ANSWER = "";//"incall_answer";
    public static final String EVENT_FUNC_INCALL_CLOSE = "";//"incall_close";

    // 功能：锁屏
    public static final String EVENT_FUNC_LOCKSCREEN = "";//"lockscreen"; // 锁屏展示
    public static final String EVENT_FUNC_LOCKSCREEN_VALUE_CHARGELOCK = "";//"charge_lock"; // 充电锁
    public static final String EVENT_FUNC_LOCKSCREEN_VALUE_SCREENLOCK = "";//"screen_lock"; // 工具锁
    public static final String EVENT_FUNC_LOCKSCREEN_VALUE_CHARGELOCK_UNLOCK = "";//"charge_lock_unlock"; // 充电锁解锁
    public static final String EVENT_FUNC_LOCKSCREEN_VALUE_SCREENLOCK_UNLOCK = "";//"screen_lock_unlock"; // 工具锁解锁

    // 功能：买量
    public static final String EVENT_FUNC_BUYUSER = "";//"buyuser";
    public static final String EVENT_FUNC_BUYUSER_KEY_SOURCE = "";//"source";

    // 功能：wifi
    public static final String EVENT_FUNC_WIFI = "";//"wifi";
    public static final String EVENT_FUNC_WIFI_VALUE_START_SCAN = "";//"start_scan"; // wifi功能相关统计
    public static final String EVENT_FUNC_WIFI_VALUE_RISKY = "";//"risky"; // wifi功能相关统计
    public static final String EVENT_FUNC_WIFI_VALUE_SAFED = "";//"safed"; // wifi功能相关统计

    // 功能：通知栏bar
    public static final String EVENT_FUNC_NOTIFICATIONBAR = "";//"notifi_bar";
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_FLASH_SWITCH = "";//"flash"; // 点击手电开关按钮
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_WIFI_SWITCH = "";//"wifi"; // 点击Wifi开关按钮
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_4G_SWITCH = "";//"4g"; // 点击移动流量按钮
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_ROTATE_SWITCH = "";//"rotate"; // 点击屏幕翻转按钮
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_BLUETOOTH_SWITCH = "";//"bluetooth"; // 点击蓝牙模式按钮
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_CLEAN_SWITCH = "";//"clean"; // 点击清理按钮（待定）
    public static final String EVENT_FUNC_NOTIFICATIONBAR_VALUE_FUNCTION_DISPLAY = "";//"display"; // 功能通知栏展示

    // 功能：主界面
    public static final String EVENT_FUNC_MAIN_PAGE = "";//"main_page";
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_MAIN_SHOW = "";//"show"; // 点击手电开关按钮
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_MAIN_FLASH_SWITCH_OPEN = "";//"switch_open"; // 点击手电开关按钮
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_MAIN_FLASH_SWITCH_CLOSE = "";//"switch_close"; // 点击手电开关按钮
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_FLASH_STROBE = "";//"strobe"; // 切换模式操作
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_FLASH_MODE_0 = "";//"m0"; // 使用默认模式,以用户当天切换至，并开启灯光为标准
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_FLASH_MODE_1 = "";//"m1"; // 使用模式1
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_FLASH_MODE_2 = "";//"m2"; // 使用模式2
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_FLASH_MODE_3 = "";//"m3"; // 使用模式3
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_FLASH_MODE_S = "";//"ms"; // 使用SOS模式
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_SETTINGS_ICON = "";//"settings"; // 点击设置入口按钮
    public static final String EVENT_FUNC_MAIN_PAGE_VALUE_GIFTBOX = "";//"giftbox"; // 点击礼盒

    // 功能：评分引导
    public static final String EVENT_FUNC_RATE_GUIDE = "";//"rate_guide";
    public static final String EVENT_FUNC_RATE_GUIDE_VALUE_DISPLAY = "";//"display"; // 评分界面展示
    public static final String EVENT_FUNC_RATE_GUIDE_VALUE_LIKE = "";//"like"; // 点击“喜欢”按钮
    public static final String EVENT_FUNC_RATE_GUIDE_VALUE_LATER = "";//"later"; // 点击“不喜欢”按钮
    public static final String EVENT_FUNC_RATE_GUIDE_VALUE_CLOSE = "";//"close"; // 评分引导关闭
    public static final String EVENT_FUNC_IG_GUIDE_VALUE_SHOW = "";//"window_ig_show"; // IG引导展示
    public static final String EVENT_FUNC_IG_GUIDE_VALUE_CLICK = "";//"window_ig_follow_click"; // IG引导点击

    // 功能：桌面小部件
    public static final String EVENT_FUNC_WIDGET = "";//"widget";
    public static final String EVENT_FUNC_WIDGET_VALUE_FLASH_SWITCH_0 = "";//"s0"; // 点击手电开关按钮（条状小部件）
    public static final String EVENT_FUNC_WIDGET_VALUE_FLASH_SWITCH_1 = "";//"s1"; // 点击手电开关按钮（按钮小部件）
    public static final String EVENT_FUNC_WIDGET_VALUE_WIFI_ICON = "";//"wifi"; // 点击WiFi按钮（条状）
    public static final String EVENT_FUNC_WIDGET_VALUE_4G_ICON = "";//"4g"; // 点击移动网络按钮（条状）
    public static final String EVENT_FUNC_WIDGET_VALUE_ROTATE_ICON = "";//"rotate"; // 点击屏幕翻转按钮（条状）
    public static final String EVENT_FUNC_WIDGET_VALUE_BAR_DISPLAY = "";//"bar"; // 条状小部件展示
    public static final String EVENT_FUNC_WIDGET_VALUE_BUTTON_DISPLAY = "";//"button"; // 按钮小部件展示

    // 功能：来电闪屏引导界面
    public static final String EVENT_FUNC_INCALL_GUIDE = "";//"incall_guide";
    public static final String EVENT_FUNC_INCALL_GUIDE_VALUE_DISPLAY = "";//"display";
    public static final String EVENT_FUNC_INCALL_GUIDE_VALUE_READY_DISPLAY = "";//"ready_display";
    public static final String EVENT_FUNC_INCALL_GUIDE_VALUE_SCREEN_FLASH = "";//"screen_flash";
    public static final String EVENT_FUNC_INCALL_GUIDE_VALUE_CALL_FLASH = "";//"call_flash";

    // 离线统计，用来统计系统的一些配置项变化情况，24小时统计一次
    public static final String EVENT_PREFIX_OFFLINE = "";//"offline";
    public static final String EVENT_OFFLINE_CALL = EVENT_SETTINGS_CALL_ICON; // 来电提醒开关
    public static final String EVENT_OFFLINE_NOTIFICATION = EVENT_SETTINGS_NOTIFICATION_ICON; // 通知提醒开关
    public static final String EVENT_OFFLINE_NOTIFICATIONBAR = EVENT_SETTINGS_NOTIFICATIONBAR_ICON; // 通知栏开关
    public static final String EVENT_OFFLINE_WIFI = EVENT_SETTINGS_WIFI_ICON ; // 点击wifi自动扫描
    public static final String EVENT_OFFLINE_SCREENLOCK = EVENT_SETTINGS_SCREENLOCK_ICON; // 锁屏开关
    public static final String EVENT_OFFLINE_INCALL_LED_MODE = EVENT_SETTINGS_INCALL_LED_MODE;
    public static final String EVENT_OFFLINE_INCALL_LED_SWITCH = EVENT_SETTINGS_INCALL_LED_SWITCH;
    public static final String EVENT_OFFLINE_SECURE_KEYGUARD = "";//"secure_keyguard";


    //key
    public static final String EVENT_KEY_CLICK = "";//"click";//点击统计
    public static final String EVENT_KEY_STATUS = "";//"status";//状态统计
    public static final String EVENT_KEY_SHOW = "";//"show";//显示统计
    public static final String EVENT_KEY_USE = "";//"use";//使用统计

    //value
    public static final String EVENT_VALUE_STATUS_OPEN = "";//"open";
    public static final String EVENT_VALUE_STATUS_CLOSE = "";//"close";
    public static final String EVENT_VALUE_STATUS_DURATION = "";//"duration";

    /**
     * 主界面
     */
    public static final String EVENT_FUNC_CAMPAGE = "";//"campage";
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FREQUENCY = "";//"frequency";//进入相机的频次（即通过各类入口进入相机的次数）
    public static final String EVENT_FUNC_CAMPAGE_VALUE_CAPTURE_CLICK = "";//"campage_capture";//拍照
    public static final String EVENT_FUNC_CAMPAGE_VALUE_CAPTURE_LONGPRESS = "";//"campage_capture_longpress";//录制短视频
    public static final String EVENT_FUNC_CAMPAGE_VALUE_CAPTURE_CANCEL = "";//"campage_capture_cancel";//录制短视频滑动取消

    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_CLICK = "";//"campage_filter";//滤镜入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_STICKER_CLICK = "";//"campage_sticker";//贴纸入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_VIGNETTE_CLICK = "";//"campage_fliter_vignette";//暗角点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_BLUR_CLICK = "";//"campage_fliter_blur";//模糊点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_RANDOM_CLICK = "";//"campage_fliter_random";// 随机点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_DOWN_CLICK = "";//"campage_fliter_down";//  收起点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_MORE_CLICK = "";//"campage_fliter_more";//更多按钮点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAST_PHOTO_CLICK = "";//"campage_lastphoto";//照片浏览入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_FREQUENCY_BACK = "";//"frequency_filter_back";//照片浏览入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FILTER_FREQUENCY_FRONT = "";//"frequency_filter_front";//照片浏览入口点击

    public static final String EVENT_FUNC_CAMPAGE_VALUE_BACK_CLICK = "";//"campage_back";//贴纸入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_MORE_CLICK = "";//"campage_more";//更多入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_MORE_HDR_CLICK = "";//"campage_more_hdr";//HDR点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_MORE_NINE_BOX_CLICK = "";//"campage_more_nine_box";//9宫格点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_MORE_SCREENCAP_CLICK = "";//"campage_more_screencap";//屏幕捕获点击操作
    public static final String EVENT_FUNC_CAMPAGE_VALUE_SWIPE_CLICK = "";//"campage_swipe_setting";//swipe设置
    public static final String EVENT_FUNC_CAMPAGE_VALUE_MORE_FEEDBACK_CLICK = "";//"campage_more_feedback";//反馈入口点击操作
    public static final String EVENT_FUNC_CAMPAGE_VALUE_MORE_ABOUT_CLICK = "";//"campage_more_about";//关于入口点击操作


    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_CLICK = "";//"campage_layout";//布局入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_SIZE_CLICK = "";//"campage_layout_size";//尺寸切换点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_BORDER_CLICK = "";//"campage_layout_border";//边框开启点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_DOWN_CLICK = "";//"campage_layout_down";//收起布局面板点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_9_16_BACK = "";//"frequency_layout_9_16_back";//收起布局面板点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_3_4_BACK = "";//"frequency_layout_3_4_back";//收起布局面板点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_1_1_BACK = "";//"frequency_layout_1_1_back";//收起布局面板点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_9_16_FRONT = "";//"frequency_layout_9_16_front";//收起布局面板点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_3_4_FRONT = "";//"frequency_layout_3_4_front";//收起布局面板点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_LAYOUT_1_1_FRONT = "";//"frequency_layout_1_1_front";//收起布局面板点击

    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_CLICK = "";//"campage_timer";//定时器入口点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_NONE_TAKE_BACK = "";//"campage_timer_none_take_back";//不使用定时器及后置摄像头完成拍摄的次数
    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_3S_TAKE_BACK = "";//"frequency__timer_3s_back";//使用3s定时器及后置摄像头完成拍摄的次数
    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_10S_TAKE_BACK = "";//"frequency__timer_10s_back";//使用10s定时器及后置摄像头完成拍摄的次数
    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_NONE_TAKE_FRONT = "";//"frequency__timer_none_front";//不使用定时器及前置摄像头完成拍摄的次数
    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_3S_TAKE_FRONT = "";//"frequency__timer_3s_front";//使用3s定时器及前置摄像头完成拍摄的次数
    public static final String EVENT_FUNC_CAMPAGE_VALUE_TIME_10S_TAKE_FRONT = "";//"frequency__timer_10s_front";//使用10s定时器及前置摄像头完成拍摄的次数

    public static final String EVENT_FUNC_CAMPAGE_VALUE_FLASH_CLICK = "";//"campage_flash";//闪光灯入口点击

    public static final String EVENT_FUNC_CAMPAGE_VALUE_CAMCHANGE_CLICK = "";//"campage_camchange_click";//镜头转换点击
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FREQUENCY_BACK = "";//"frequency_back";// 使用后置摄像头完成拍照次数,
    public static final String EVENT_FUNC_CAMPAGE_VALUE_FREQUENCY_FRONT = "";//"frequency_front";// 使用前置摄像头完成拍照次数,

    public static final String EVENT_FUNC_CAMPAGE_VALUE_CAMERA_BUSY_SHOW = "";//"window_issue_occupied_show";// 摄像头占用弹窗展示
    public static final String EVENT_FUNC_CAMPAGE_VALUE_CAMERA_BUSY_CLICK = "";//"window_issue_occupied_exit_click";// 摄像头占用退出点击

    public static final String EVENT_FUNC_CAMPAGE_VALUE_PAUSE_MOTION_CLICK = "";//"campage_pause_motion";//暂停录制视频
    public static final String EVENT_FUNC_CAMPAGE_VALUE_SWITCH_CAMERA_IN_MOTION_CLICK = "";//"campage_switch_cam_in_motion";//录制视频中切换摄像头
    public static final String EVENT_FUNC_CAMPAGE_VALUE_CANCEL_MOTION_CLICK = "";//"campage_cancel_motion";//录制视频取消
    public static final String EVENT_FUNC_CAMPAGE_VALUE_DONE_MOTION_CLICK = "";//"campage_done_motion";//确认录制视频

    //主界面事件
    public static final String EVENT_FUNC_CAMPAGE_FILTER_VIGNETTE_STATUS = "";//"vignette_status";//暗角状态

    public static final String EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS = "";//"blur_status";//模糊状态
    public static final String EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS_VALUE_OFF = "";//"blur_off";//模糊off
    public static final String EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS_VALUE_RADIAL = "";//"blur_radial";//blur_radial
    public static final String EVENT_FUNC_CAMPAGE_FILTER_BLUR_STATUS_VALUE_LINEAR = "";//"blur_linear";//blur_linear

    public static final String EVENT_FUNC_CAMPAGE_MORE_HDR_STATUS = "";//"hdr_status";//hdr状态

    public static final String EVENT_FUNC_CAMPAGE_MORE_NINE_BOX_STATUS = "";//"nine_box_status";//9宫格状态

    public static final String EVENT_FUNC_CAMPAGE_MORE_SCREENCAP_STATUS = "";//"screencap_status";//屏幕捕获点击状态

    public static final String EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS = "";//"layout_size_status";//尺寸切换状态
    public static final String EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_16_9 = "";//"layout_size_16_9";//尺寸切换状态
    public static final String EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_3_4 = "";//"layout_size_3_4";//尺寸切换状态
    public static final String EVENT_FUNC_CAMPAGE_LAYOUT_SIZE_STATUS_VALUE_1_1 = "";//"layout_size_1_1";//尺寸切换状态

    public static final String EVENT_FUNC_CAMPAGE_LAYOUT_BORDER_STATUS = "";//"border_status";//边框开启状态

    public static final String EVENT_FUNC_CAMPAGE_TIME_STATUS = "";//"timer_status";//定时器状态
    public static final String EVENT_FUNC_CAMPAGE_TIME_STATUS_NONE = "";//"timer_none";//定时器状态
    public static final String EVENT_FUNC_CAMPAGE_TIME_STATUS_3S = "";//"timer_3s";//定时器状态
    public static final String EVENT_FUNC_CAMPAGE_TIME_STATUS_10S = "";//"timer_10s";//定时器状态

    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS = "";//"flash_status";//闪光灯状态
    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS_OFF = "";//"flash_off";//闪光灯状态off
    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS_ON = "";//"flash_on";//闪光灯状态on
    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS_AUTO = "";//"flash_auto";//闪光灯状态auto
    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS_TORCH = "";//"flash_torch";//闪光灯状态torch
    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS_LIGHT_ON = "";//"flash_light_on";//闪光灯状态light_on
    public static final String EVENT_FUNC_CAMPAGE_FLASH_STATUS_LIGHT_OFF = "";//"flash_light_off";//闪光灯状态light_off

    public static final String EVENT_FUNC_CAMPAGE_CAMCHANGE_STATUS = "";//"camchange_status";//镜头状态
    public static final String EVENT_FUNC_CAMPAGE_CAMCHANGE_STATUS_VALUE_BACK = "";//"camchange_back";//镜头状态
    public static final String EVENT_FUNC_CAMPAGE_CAMCHANGE_STATUS_VALUE_FRONT = "";//"camchange_front";//镜头状态
    public static final String EVENT_FUNC_CAMPAGE_CAMSOUND_STATUS = "";//"camsound_status";//拍照声音状态

    /**
     *图片预览（编辑）
     */
    public static final String EVENT_FUNC_PREVIEW = "";//"preview";
    public static final String EVENT_FUNC_PREVIEW_VALUE_CANCEL_CLICK = "";//"preview_cancel";// 图片预览关闭按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_ALBUM_CLICK = "";//"preview_album";// 图片预览相册按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_GIF_CLICK = "";//"preview_gif";// 图片预览gif按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_TRASH_CLICK = "";//"preview_trash";// 图片预览删除按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_SHARE_CLICK = "";//"preview_share";// 图片预览分享按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_SHARE_CHANNEL = "";//"preview_share_channel";// （x表示用户分享的渠道，若无法统计到完成分享则使用点击行为进行表示）, 相关渠道分享次数

    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_CLICK = "";//"preview_editor";// 图片预览编辑按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_CANCEL_CLICK = "";//"editor_cancel";// 图片预览编辑取消按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_SAVE_CLICK = "";//"editor_save";// 图片预览编辑保存按钮点击


    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_CLICK = "";//"editor_param";// 图片预览编辑参数按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_CONTRAST_CLICK = "";//"editor_param_contrast";// 对比度按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_SATURATION_CLICK = "";//"editor_param_saturation";// 饱和度按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_BRIGHTNESS_CLICK = "";//"editor_param_brightness";// 亮度按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_VIGNETTE_CLICK = "";//"editor_param_vignette";// 暗角按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_SHARPEN_CLICK = "";//"editor_param_sharpen";// 锐度按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_TEMPERATURE_CLICK = "";//"editor_param_temperature";// 温度按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_PARAM_TONE_CLICK = "";//"editor_param_tone";// 色调按钮点击

    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_CLICK = "";//"editor_tools";// 工具按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_CROP_CLICK = "";//"editor_tools_crop";// 裁剪按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_ROTATE_CLICK = "";//"editor_tools_rotate";// 翻转按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_STICKERS_CLICK = "";//"editor_tools_stickers";// 贴纸按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_BEAUTY_CLICK = "";//"editor_tools_beauty";// 美化按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_DOODLE_CLICK = "";//"editor_tools_doodle";// 涂鸦按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_COLLAGE_CLICK = "";//"editor_tools_collage";// 拼图按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_BLUR_CLICK = "";//"editor_tools_blur";// 模糊按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_TEXT_CLICK = "";//"editor_tools_text";// 文本按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_MIRROR_CLICK = "";//"editor_tools_mirror";// 镜像按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_TOOLS_FRAME_CLICK = "";//"editor_tools_frame";// 边框按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_FILTER_CLICK = "";//"editor_filter";// 滤镜按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_ERASER_CLICK = "";//"edit_eraser_click";
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_MIXER_CLICK = "";//"edit_mixer_click";
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_ERASER_SAVE = "";//"edit_eraser_save";
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_MIXER_SAVE = "";//"edit_mixer_save";

    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_FILTER_VIGNETTE_CLICK = "";//"editor_filter_vignette";// 滤镜暗角按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_FILTER_BLUR_CLICK = "";//"editor_filter_blur";// 滤镜模糊按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_FILTER = "";//"editor_filter_edit_frequency";// 滤镜模糊按钮点击

    public static final String EVENT_FUNC_PREVIEW_VALUE_TOOL_EDITOR_WATERMARK_CLICK = "";//"picview_editor_tools_watermark_click";// 水印按钮点击
    public static final String EVENT_FUNC_PREVIEW_VALUE_EDITOR_WATERMARK_CLICK = "";//"picview_editor_watermark_click";// 编辑页面点击图片右下角水印
    public static final String EVENT_FUNC_PREVIEW_VALUE_WATERMARK_GENERATE = "";//"picview_editor_watermark_";// 使用该水印生成照片的次数

    public static final String EVENT_FUNC_PREVIEW_VALUE_BOOBJOB_SHOW = "";//"beauty_boobJob_show";
    public static final String EVENT_FUNC_PREVIEW_VALUE_BOOBJOB_CLICK = "";//"beauty_boobJob_click";
    public static final String EVENT_FUNC_PREVIEW_VALUE_BOOBJOB_SAVE = "";//"beauty_boobJob_save";

    public static final String EVENT_FUNC_PREVIEW_VALUE_BUTTOCKS_SHOW = "";//"beauty_buttocks_show";
    public static final String EVENT_FUNC_PREVIEW_VALUE_BUTTOCKS_CLICK = "";//"beauty_buttocks_click";
    public static final String EVENT_FUNC_PREVIEW_VALUE_BUTTOCKS_SAVE = "";//"beauty_buttocks_save";

    /**
     * 滤镜商店
     */
    public static final String EVENT_FUNC_STORE = "";//"store";
    public static final String EVENT_FUNC_STORE_VALUE_GIFTBOX_CLICK = "";//"store_giftbox";//商店礼盒按钮点击
    public static final String EVENT_FUNC_STORE_VALUE_CANCEL_CLICK = "";//"store_cancel";//商店关闭按钮点击
    public static final String EVENT_FUNC_STORE_VALUE_FINDER_CLICK = "";//"store_finder";//商店管理按钮点击
    public static final String EVENT_FUNC_STORE_VALUE_BANNER_CLICK = "";//"filter_banner";//（x表示相应的banner ID）, 商店Banner点击
    public static final String EVENT_FUNC_STORE_VALUE_BANNER_SHOW = "";//"filter_banner";//（x表示相应的banner ID）, 商店Banner展示
    public static final String EVENT_FUNC_STORE_VALUE_CATEGORY_CLICK = "";//"filter_category";//（x表示相应的滤镜分类）, 商店分类滤镜点击
    public static final String EVENT_FUNC_STORE_VALUE_CATEGORY_SHOW = "";//"filter_category";//（x表示相应的滤镜分类）, 商店分类滤镜展示
    public static final String EVENT_FUNC_STORE_VALUE_FILTER_CLICK = "";//"filter_filter";//（x表示相应的滤镜）, 商店单款滤镜点击
    public static final String EVENT_FUNC_STORE_VALUE_FILTER_SHOW = "";//"filter_filter";//（x表示相应的滤镜）,  商店单款滤镜展示
    public static final String EVENT_FUNC_STORE_VALUE_LINE_CLICK = "";//"filter_line";//x表示滤镜在商店第几行）商店位置点击（以大的行作为商店位置标准
    public static final String EVENT_FUNC_STORE_VALUE_LINE_SHOW = "";//"filter_line";//x表示滤镜在商店第几行）商店位置展示（以大的行作为商店位置标准
    public static final String EVENT_FUNC_STORE_VALUE_CATEGORY_DOWNLOAD = "";//"filter_category_download";//商店各类别滤镜下载
    public static final String EVENT_FUNC_STORE_VALUE_CATEGORY_PAGE_SHOW = "";//"filter_category_page";//商店各类别滤镜详情页展示
    public static final String EVENT_FUNC_STORE_VALUE_CATEGORY_VIEW = "";//"filter_category_view";//商店各类别滤镜效果展示
    public static final String EVENT_FUNC_STORE_VALUE_CATEGORY_SHOW_AD = "";//"filter_category_showad";//商店各类别滤镜效果展示
    // 贴纸相关
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_BANNER_CLICK = "";//"sticker_banner";//（x表示相应的banner ID）, 商店Banner点击
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_BANNER_SHOW = "";//"sticker_banner";//（x表示相应的banner ID）, 商店Banner展示
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_CATEGORY_CLICK = "";//"sticker_category";//（x表示相应的贴纸分类）, 商店贴纸分类点击
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_CATEGORY_SHOW = "";//"sticker_category";//（x表示相应的贴纸分类）, 商店贴纸分类展示
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_CLICK = "";//"sticker_sticker";//（x表示相应的贴纸）, 商店单款贴纸点击
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_SHOW = "";//"sticker_sticker";//（x表示相应的贴纸）,  商店单款贴纸展示
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_LINE_CLICK = "";//"sticker_line";//x表示贴纸在商店第几行）商店位置点击（以大的行作为商店位置标准
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_LINE_SHOW = "";//"sticker_line";//x表示贴纸在商店第几行）商店位置展示（以大的行作为商店位置标准
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_DOWNLOAD = "";//"sticker_download";//商店贴纸下载
    public static final String EVENT_FUNC_STORE_VALUE_TEMPLATE_DOWNLOAD = "";//"template_download";//商店贴纸下载
    public static final String EVENT_FUNC_STORE_VALUE_TEMPLATE_SHOW = "";//"template_show";
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_PAGE_SHOW = "";//"sticker_page";//商店各贴纸详情页展示
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_GIF_CLICK = "";//"sticker_gif_click";//商店各贴纸详情页展示
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_GIF_APPLY = "";//"sticker_gif_apply";//商店各贴纸详情页展示
    public static final String EVENT_FUNC_STORE_VALUE_STICKER_GIF_SHOW = "";//"sticker_gif_show";//商店各贴纸详情页展示

    /**
     * SVIP
     */
//    public static final String EVENT_FUNC_SVIP_ICON = "";//"svip_icon_annual";     // icon图标
//    public static final String EVENT_FUNC_SVIP_IN_SHARE = "";//"svip_in_share";    // 分享页面svip按钮
//    public static final String EVENT_FUNC_SVIP_DIALOG = "";//"svip_dialog";    // 点击资源付费弹窗
//    public static final String EVENT_FUNC_SVIP_ACTIVITY = "";//"svip_activity";    // 主界面
//    public static final String EVENT_FUNC_SVIP_DETENTION = "";//"svip_detention";    // 挽留界面

    public static final String EVENT_KEY_BUY_SUCCESS = "";//"buy_success";

//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_MAIN = "";//"main";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_STORE = "";//"store";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_DETENTION = "";//"detention";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_SHARE_REMOVE_AD = "";//"share_remove_ad";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_RES_UNLOCK = "";//"res_unlock";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_FIRST_EDIT_FINISH = "";//"first_edit_finish";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_CLOSE_AD_THREE_TIMES= "";//"close_ad_three_times";
//    public static final String EVENT_FUNC_SVIP_VALUE_ENTRANCE_EXIT_WITHOUT_SHOW_VIP = "";//"exit_without_show_vip";

    /**
     * 升级弹窗
     */
    public static final String EVENT_FUNC_APP_UPDATE_DIALOG = "";//"app_update";
    public static final String EVENT_FUNC_APP_UPDATE_DIALOG_VALUE_DISPLAY = "";//"display"; // 升级弹窗展示
    public static final String EVENT_FUNC_APP_UPDATE_DIALOG_CANCEL = "";//"cancle"; // 升级弹窗取消
    public static final String EVENT_FUNC_APP_UPDATE_DIALOG_DOWNLOAD = "";//"download"; // 升级弹窗点击下载

    /**
     * 滤镜版本兼容弹窗
     */
    public static final String EVENT_FUNC_FILTER_UPDATE_DIALOG = "";//"filter_update";
    public static final String EVENT_FUNC_FILTER_UPDATE_DIALOG_VALUE_DISPLAY = "";//"display"; // 滤镜版本兼容弹窗展示
    public static final String EVENT_FUNC_FILTER_UPDATE_DIALOG_DOWNLOAD = "";//"download"; // 滤镜版本兼容弹窗点击下载
    public static final String EVENT_FUNC_FILTER_UPDATE_DIALOG_CANCEL = "";//"cancel"; // 滤镜版本兼容弹窗关闭

    /**
     * 暂存页面
     */
    public static final String EVENT_FUNC_STASH = "";//"stash";
    /**
     * 视频暂存页面
     */
    public static final String EVENT_FUNC_VIDEO_STASH = "";//"vstash";

    public static final String EVENT_FUNC_STASH_VALUE_CANCEL_CLICK = "";//"temp_cancel_click";//暂存页关闭按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_FILTER_CLICK = "";//"temp_filter_click";//暂存页滤镜按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_SAVE_CLICK = "";//"temp_save_click";//暂存页保存按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_SHARE_CLICK = "";//"temp_share_click";//暂存页保存按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_RANDOM_CLICK = "";//" temp_filter_random_click";//暂存页随机点击
    public static final String EVENT_FUNC_STASH_VALUE_BLUR_CLICK = "";//" temp_filter_blur_click";//暂存页模糊点击
    public static final String EVENT_FUNC_STASH_VALUE_VIGNETTE_CLICK = "";//" temp_filter_vignette_click";
    public static final String EVENT_FUNC_STASH_VALUE_FILTER_CLICK_ = "";//" temp_filter_click_";
    public static final String EVENT_FUNC_STASH_VALUE_FILTER_DOWN_CLICK_ = "";//" temp_filter_down_click";
    public static final String EVENT_FUNC_STASH_VALUE_FILTER_MORE_CLICK_ = "";//" temp_filter_more_click";
    public static final String EVENT_FUNC_STASH_VALUE_STICKER_CLICK = "";//" temp_stickers_click";
    public static final String EVENT_FUNC_STASH_VALUE_STICKER_DOWN_CLICK = "";//" temp_stickers_down_click";
    public static final String EVENT_FUNC_STASH_VALUE_STICKER_SAVE_CLICK = "";//" temp_stickers_saved";
    public static final String EVENT_FUNC_STASH_VALUE_STICKER_SHARE_CLICK = "";//" temp_stickers_shared";
    public static final String EVENT_FUNC_STASH_VALUE_TEXT_CLICK = "";//" temp_text_click";
    public static final String EVENT_FUNC_STASH_VALUE_TEXT_DOWN_CLICK = "";//" temp_text_done_click";
    public static final String EVENT_FUNC_STASH_VALUE_TEXT_SAVE_CLICK = "";//" temp_text_saved";
    public static final String EVENT_FUNC_STASH_VALUE_TEXT_SHARE_CLICK = "";//" temp_text_shared";

    public static final String EVENT_FUNC_STASH_VALUE_TEMP_DOODLE_CLICK = "";//"temp_doodle_click";//暂存页涂鸦按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_TEMP_DOODLE_DOWN_CLICK = "";//"temp_doodle_done_click";//暂存页完成按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_TEMP_DOODLE_SAVE = "";//"temp_doodle_saved";//暂存页使用涂鸦后进行保存
    public static final String EVENT_FUNC_STASH_VALUE_TEMP_DOODLE_SHARE = "";//"temp_doodle_shared";//暂存页使用涂鸦后进行分享

    public static final String EVENT_FUNC_VIDEO_STASH_VALUE_SHARE_CLICK = "";//" temp_video_shared";//视频暂存页面分享按钮点击
    public static final String EVENT_FUNC_VIDEO_STASH_VALUE_GIF_CLICK = "";//" temp_gif_shared";//视频暂存页面转换gif按钮点击

    public static final String EVENT_FUNC_STASH_VALUE_WATERMARK_CLICK = "";//"temp_watermark_click"; //暂存水印按钮点击
    public static final String EVENT_FUNC_STASH_VALUE_WATERMARK_CLICK_ = "";//"temp_watermark_click_"; //暂存水印点击
    public static final String EVENT_FUNC_STASH_VALUE_WATERMARK_CUSTOM_CLICK = "";//"temp_watermark_custom_click"; //暂存文本水印点击
    public static final String EVENT_FUNC_STASH_VALUE_WATERMARK_FREQUENCY = "";//"temp_watermark_frequency_"; //暂存使用该水印生成照片的次数
    public static final String EVENT_FUNC_STASH_VALUE_WATERMARK_CUSTOM_FREQUENCY = "";//"temp_watermark_custom_frequency"; //暂存使用水印且自定义文案生成照片的次数

    public static final String EVENT_FUNC_STASH_VALUE_EMOJI_DOWNLOAD_SHOW = "";//" sticker_emoji_download_show";//下载页面展示次数
    public static final String EVENT_FUNC_STASH_VALUE_EMOJI_DOWNLOAD_CLICK = "";//" sticker_emoji_download_click";//下载页面展示次数
    public static final String EVENT_FUNC_STASH_VALUE_DOWNLOAD_EMOJI__CLICK = "";//" sticker_emoji_download_emoji_click";//下载页面展示次数

    /**
     * Photo editor主页面
     */
    public static final String EVENT_FUNC_MAINPAGE = "";//"mainpage";
    public static final String EVENT_FUNC_MAINPAGE_BTN_BEAUTY = "";//"mainpage_beauty";
    public static final String EVENT_FUNC_MAINPAGE_BTN_COLLAGE = "";//"mainpage_collage";
    public static final String EVENT_FUNC_MAINPAGE_BTN_EDIT = "";//"mainpage_edit";
    public static final String EVENT_FUNC_MAINPAGE_BTN_SELFIE = "";//"mainpage_selfie";
    public static final String EVENT_FUNC_MAINPAGE_BTN_GALLERY = "";//"mainpage_galley";
    public static final String EVENT_FUNC_MAINPAGE_BTN_EFFECT = "";//"mainpage_effect";
    public static final String EVENT_FUNC_MAINPAGE_BTN_FREESTYLE= "";//"mainpage_freestyle";
    public static final String EVENT_FUNC_MAINPAGE_FREQUENCY = "";//"frequency";//进入主页的频次
    public static final String EVENT_FUNC_MAINPAGE_BEAUTY_SHOW = "";//"beauty_show";//美颜入口展示次数
    public static final String EVENT_FUNC_MAINPAGE_BEAUTY_CLICK = "";//"beauty_click";//美颜入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_COLLAGE_SHOW = "";//"collage_show";//拼图入口展示次数
    public static final String EVENT_FUNC_MAINPAGE_COLLAGE_CLICK = "";//"collage_click";//拼图入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_FREESTYLE_CLICK = "";//"freestyle_click";//FREESTYLE入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_FREESTYLE_SHOW = "";//"freestyle_show";//FREESTYLE入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_TEMPLATE_SHOW = "";//"template_show";//拼图入口展示次数
    public static final String EVENT_FUNC_MAINPAGE_TEMPLATE_CLICK = "";//"template_click";//拼图入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_EDIT_SHOW = "";//"edit_show";//编辑入口展示次数
    public static final String EVENT_FUNC_MAINPAGE_EDIT_CLICK = "";//"edit_click";//编辑入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_SELFIE_SHOW = "";//"selfie_show";//拍照入口展示次数
    public static final String EVENT_FUNC_MAINPAGE_SELFIE_CLICK = "";//"selfie_click";//拍照入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_GALLERY_SHOW = "";//"gallery_show";//相册入口展示次数
    public static final String EVENT_FUNC_MAINPAGE_GALLERY_CLICK = "";//"gallery_click";//相册入口点击次数
    public static final String EVENT_FUNC_MAINPAGE_SETTINGS_CLICK = "";//"settings_click"; // 点击设置入口按钮
    public static final String EVENT_FUNC_MAINPAGE_EFFECT_SHOW = "";//"effect_show";
    public static final String EVENT_FUNC_MAINPAGE_EFFECT_ICON = "";//"effect_click";

    /**
     * 服务器json数据下载统计
     */
    public static final String EVENT_SERVER_DATA_REQUEST = "";//"server_data_request"; //请求数据
    public static final String EVENT_SERVER_DATA_REQUEST_SUCCESS = "";//"server_data_request_success"; //下载成功
    public static final String EVENT_SERVER_DATA_REQUEST_ERROR = "";//"server_data_request_error"; //下载出错

    /**
     * 服务器urls更新统计
     */
    public static final String EVENT_SERVER_URL_UPDATE = "";//"server_url_update"; //请求服务器配置urls
    public static final String EVENT_SERVER_URL_UPDATE_SUCCESS = "";//"server_url_update_success"; //更新成功
    public static final String EVENT_SERVER_URL_UPDATE_ERROR = "";//"server_url_update_error"; //更新失败
    public static final String EVENT_SERVER_URL = "";//"server_url"; //服务器配置urls更新后，选中连接服务器url

    /**
     * 预加载文件下载统计
     */
    public static final String EVENT_PRELOAD_DATA_DOWNLOAD = "";//"preload_data_download"; // 预加载文件下载
    public static final String EVENT_PRELOAD_DATA_DOWNLOAD_SUCCESS = "";//"preload_data_download_success"; //下载成功
    public static final String EVENT_PRELOAD_DATA_DOWNLOAD_ERROR = "";//"preload_data_download_error"; //下载出错

    /**
     * 下载速度测试
     */
    public static final String EVENT_SPEED_TEST_SUCCESS = "";//"speed_test_new_success"; //下载成功
    public static final String EVENT_SPEED_TEST_INCORRECT = "";//"speed_test_new_incorrect"; //下载文件有误
    public static final String EVENT_SPEED_TEST_ERROR = "";//"speed_test_new_error"; //下载出错


    public static final String EVENT_PUSH_SPEED_TEST_SUCCESS = "";//"push_speed_test_success"; //下载成功
    public static final String EVENT_PUSH_SPEED_TEST_INCORRECT = "";//"push_speed_test_incorrect"; //下载文件有误
    public static final String EVENT_PUSH_SPEED_TEST_ERROR = "";//"push_speed_test_error"; //下载出错


    /**
     * 设置页
     */
    public static final String EVENT_FUNC_SETTING = "";//"setting";
    public static final String EVENT_FUNC_SETTING_VALUE_FEEDBACK_CLICK = "";//"feedback";//反馈入口点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_MORE_CLICK = "";//"more";//更多入口点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_SOUND_CLICK = "";//"sound";//声音点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_SHARE_CLICK = "";//"share";//分享入口点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_INS_CLICK = "";//"ins";//ins关注入口点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_FB_CLICK = "";//"fb";//fb关注点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_TWITTER_CLICK = "";//"twitter";//twitter关注点击操作
    public static final String EVENT_FUNC_SETTING_GUIDE_MANUAL_CLICK = "";//"guide";//twitter关注点击操作
    public static final String EVENT_FUNC_SETTING_VALUE_ABOUT_CLICK = "";//"about";//about

    //资源应用次数 包名区分
    public static final String EVENT_FUNC_STORE_ALL_APPLY = "";//"store_all_apply";
    //资源下载次数 包名区分
    public static final String EVENT_FUNC_STORE_ALL_DOWNLOAD = "";//"store_all_download";
    public static final String EVENT_FUNC_STORE_ALL_SHOW = "";//"store_all_show";
    public static final String EVENT_FUNC_STORE_ALL_CLICK = "";//"store_all_click";

    //资源应用次数 包名区分  主界面
    public static final String EVENT_FUNC_STORE_MAIN_APPLY = "";//"store_mainpage_apply";
    //资源下载次数 包名区分 主界面
    public static final String EVENT_FUNC_STORE_MAIN_DOWNLOAD = "";//"store_mainpage_download";
    public static final String EVENT_FUNC_STORE_MAIN_SHOW= "";//"store_mainpage_show";
    public static final String EVENT_FUNC_STORE_MAIN_CLICK = "";//"store_mainpage_click";

    //资源应用次数 包名区分  sticker商店
    public static final String EVENT_FUNC_STORE_STICKER_APPLY = "";//"store_sticker_apply";
    //资源下载次数 包名区分 sticker商店
    public static final String EVENT_FUNC_STORE_STICKER_DOWNLOAD = "";//"store_sticker_download";
    public static final String EVENT_FUNC_STORE_STICKER_SHOW = "";//"store_sticker_show";
    public static final String EVENT_FUNC_STORE_STICKER_CLICK = "";//"store_sticker_click";

    //资源应用次数 包名区分  filter商店
    public static final String EVENT_FUNC_STORE_FILTER_APPLY = "";//"store_filter_apply";
    //资源下载次数 包名区分 filter商店
    public static final String EVENT_FUNC_STORE_FILTER_DOWNLOAD = "";//"store_filter_download";
    public static final String EVENT_FUNC_STORE_FILTER_SHOW = "";//"store_filter_show";
    public static final String EVENT_FUNC_STORE_FILTER_CLICK = "";//"store_filter_click";

    public static final String EVENT_FUNC_STORE_BACKGROUND_APPLY = "";//"store_background_apply";
    public static final String EVENT_FUNC_STORE_BACKGROUND_DOWNLOAD = "";//"store_background_download";
    public static final String EVENT_FUNC_STORE_BACKGROUND_SHOW = "";//"store_background_show";
    public static final String EVENT_FUNC_STORE_BACKGROUND_CLICK = "";//"store_background_click";

    public static final String EVENT_FUNC_STORE_TEMPLATE_APPLY = "";//"store_template_apply";
    public static final String EVENT_FUNC_STORE_TEMPLATE_DOWNLOAD = "";//"store_template_download";
    public static final String EVENT_FUNC_STORE_TEMPLATE_SHOW = "";//"store_template_show";
    public static final String EVENT_FUNC_STORE_TEMPLATE_CLICK = "";//"store_template_click";

    public static final String EVENT_FUNC_STORE_GIF_SHOW = "";//"store_gif_show";
    public static final String EVENT_FUNC_STORE_GIF_APPLY = "";//"store_gif_apply";
    public static final String EVENT_FUNC_STORE_GIF_CLICK = "";//"store_gif_click";
    public static final String EVENT_FUNC_MAIN_GIF_SHOW = "";//"main_gif_show";
    public static final String EVENT_FUNC_MAIN_GIF_APPLY = "";//"main_gif_apply";
    public static final String EVENT_FUNC_MAIN_GIF_CLICK = "";//"main_gif_click";

    //统计所有资源下载次数
    public static final String EVENT_FUNC_STORE_DOWNLOAD = "";//"store_download";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_0 = "";//"download_count_0";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_1_2 = "";//"download_count_1_2";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_3_5 = "";//"download_count_3_5";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_6_9 = "";//"download_count_6_9";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_10_19 = "";//"download_count_10_19";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_20_49 = "";//"download_count_20_49";
    public static final String EVENT_FUNC_STORE_DOWNLOAD_50 = "";//"download_count_50";
    //用于包名拼接
    public static final String EVENT_FUNC_STORE_SICKER_ = "";//"sticker_";
    public static final String EVENT_FUNC_STORE_FILTER_ = "";//"filter_";
    public static final String EVENT_FUNC_STORE_BACKGROUND_ = "";//"background_";
    public static final String EVENT_FUNC_STORE_TEMPLATE_ = "";//"template_";

    /**
     * 分享引导弹窗
     */
    public static final String EVENT_FUNC_SHARE_DIALOG = "";//"share";

    /**
     * 隐私协议授权页
     */
    public static final String EVENT_FUNC_SPLASH_PRIVACY = "";//"mainpage_privacy";
    public static final String EVENT_FUNC_SPLASH_PRIVACY_DISPLAY = "";//"display";
    public static final String EVENT_FUNC_SPLASH_PRIVACY_CONFIRM = "";//"confirm";

    /**
     * 字体选中栏
     */
    public static final String EVENT_FUNC_FONT_BAR_DISPLAY = "";//"edit_font_display";
    public static final String EVENT_FUNC_FONT_BAR_DOWNLOAD = "";//"edit_font_download";
    public static final String EVENT_FUNC_FONT_BAR_CLICK = "";//"edit_font_click";
    public static final String EVENT_FUNC_FONT_BAR_APPLY = "";//"edit_font_apply";

    /**
     * freestyle
     */
     public static final String EVENT_FUNC_FREESTYLE = "";//"freestyle";
     public static final String EVENT_FUNC_FREESTYLE_BACKGROUND = "";//"freestyle_background";
     public static final String EVENT_FUNC_FREESTYLE_STICKER = "";//"freestyle_sticker";
     public static final String EVENT_FUNC_FREESTYLE_FILTER = "";//"freestyle_filter";
     public static final String EVENT_FUNC_FREESTYLE_RATIO = "";//"freestyle_ratio";
     public static final String EVENT_FUNC_FREESTYLE_BORDER = "";//"freestyle_border";
     public static final String EVENT_FUNC_FREESTYLE_IMAGE = "";//"freestyle_image";
     public static final String EVENT_FUNC_FREESTYLE_TEXT = "";//"freestyle_text";
     public static final String EVENT_FUNC_FREESTYLE_DOODLE = "";//"freestyle_doodle";
     public static final String EVENT_FUNC_FREESTYLE_WATERMARK = "";//"freestyle_watermark";
     //二级菜单
     public static final String EVENT_FUNC_FREESTYLE_SECOND_BACK = "";//"freestyle_second_back";
     public static final String EVENT_FUNC_FREESTYLE_SECOND_CROP = "";//"freestyle_second_crop";
     public static final String EVENT_FUNC_FREESTYLE_SECOND_FILTER = "";//"freestyle_second_filter";
     public static final String EVENT_FUNC_FREESTYLE_SECOND_ROTATE = "";//"freestyle_second_rotate";
     public static final String EVENT_FUNC_FREESTYLE_SECOND_MIRROR = "";//"freestyle_second_mirror";
     public static final String EVENT_FUNC_FREESTYLE_SECOND_FLIP = "";//"freestyle_second_flip";
     public static final String EVENT_FUNC_FREESTYLE_SECOND_CHANGE = "";//"freestyle_second_change";

     public static final String EVENT_FUNC_FREESTYLE_SAVED = "";//"_saved";

    /**
     * 编辑状态统计
     */
    // 编辑图片
    public static final String EVENT_FUNC_EDIT_RESULT = "";//"edit_result";
    // collage图片
    public static final String EVENT_FUNC_GRID_RESULT = "";//"grid_result";
    // freestyle
    public static final String EVENT_FUNC_FREESTYLE_RESULT = "";//"freestyle_result";
    // 合成视频
    public static final String EVENT_FUNC_MAKE_VIDEO_RESULT = "";//"make_video_result";
    public static final String EVENT_FUNC_MAKE_VIDEO_ERROR = "";//"make_video_error";
    // ffmpeg处理返回错误
    public static final String EVENT_FUNC_MAKE_VIDEO_ERROR_FFMPEG = "";//"make_video_error_ffmpeg";
    // ffmpeg处理返回"unused DT entry"错误
    public static final String EVENT_FUNC_FFMPEG_DT_ENTRY_ERROR = "";//"make_video_error_dt";
    // ffmpeg处理返回"No such file or directory"错误
    public static final String EVENT_FUNC_FFMPEG_NO_FILE_ERROR = "";//"make_video_error_no_file";
    // 拍照
    public static final String EVENT_FUNC_TAKE_IMG_RESULT = "";//"take_img_result";
    // 拍视频
    public static final String EVENT_FUNC_TAKE_VIDEO_RESULT = "";//"take_video_result";
    // 保存图片失败
    public static final String EVENT_FUNC_SAVE_IMG_ERROR = "";//"save_img_error";
    // 图片预览时，提示broken
    public static final String EVENT_FUNC_PREVIEW_IMG_ERROR = "";//"preview_img_error";

    // key
    public static final String EVENT_FUNC_RESULT = "";//"result";
    public static final String EVENT_FUNC_CMD = "";//"cmd";
    public static final String EVENT_FUNC_ERROR_MSG = "";//"error_msg";
    public static final String EVENT_FUNC_ERROR_PHONE = "";//"phone_info";
    public static final String EVENT_FUNC_RES_SIZE = "";//"res_size";
    public static final String EVENT_FUNC_STORAGE_SIZE = "";//"storage_size";
    public static final String EVENT_FUNC_OUTPUT_FILE_EXIST = "";//"output_exist";
    public static final String EVENT_FUNC_FFMPEG_EXEC = "";//"ffmpeg_exec";
    public static final String EVENT_FUNC_NO_FILE_NAME = "";//"no_file";

    //所有banner的事件
    public static final String EVENT_FUNC_BANNER_ALL_SHOW = "";//"banner_all_show";
    public static final String EVENT_FUNC_BANNER_ALL_CLICK = "";//"banner_all_click";
    public static final String EVENT_FUNC_BANNER_ALL_DOWNLOAD = "";//"banner_all_download";
    public static final String EVENT_FUNC_BANNER_ALL_APPLY = "";//"banner_all_apply";

    //Banner各界面各位置展示、点击及下载（对应到各个子商店位置） 均是事件 如：banner_filter_1_show
    public static final String EVENT_FUNC_BANNER_ = "";//"banner_";
    public static final String EVENT_FUNC_SHOW = "";//"_show";
    public static final String EVENT_FUNC_CLICK = "";//"_click";
    public static final String EVENT_FUNC_DOWNLOAD = "";//"_download";
    public static final String EVENT_FUNC_APPLY = "";//"_apply";

    //设置页统计 均是事件
    public static final String EVENT_FUNC_SETTING_SHOW = "";//"mainpage_settings_show";//设置按钮展示
    public static final String EVENT_FUNC_SETTING_CLICK = "";//"mainpage_settings_click";//设置按钮点击
    public static final String EVENT_FUNC_SETTING_SOUND_CLICK = "";//"mainpage_settings_sound_click";//快门声开关点击
    public static final String EVENT_FUNC_SETTING_SOUND_RATE = "";//"mainpage_settings_sound_rate";//快门声开启用户比例
    public static final String EVENT_FUNC_SETTING_TUTORIAL_CLICK = "";//"mainpage_settings_tutorial_click";//用户手册按钮点击
    public static final String EVENT_FUNC_SETTING_FEEDBACK_CLICK = "";//"mainpage_settings_feedback_click";//用户反馈按钮点击
    public static final String EVENT_FUNC_SETTING_TYF_CLICK = "";//"mainpage_settings_tyf_click";//用户反馈按钮点击
    public static final String EVENT_FUNC_SETTING_IG_CLICK = "";//"mainpage_settings_ig_click";//关注IG按钮点击
    public static final String EVENT_FUNC_SETTING_FB_CLICK = "";//"mainpage_settings_fb_click";//关注FB按钮点击
    public static final String EVENT_FUNC_SETTING_TW_CLICK = "";//"mainpage_settings_tw_click";//关注TW按钮点击
    public static final String EVENT_FUNC_SETTING_MORE_CLICK = "";//"mainpage_settings_more_click";//关注more按钮点击
    public static final String EVENT_FUNC_SETTING_MORE_ABOUT_CLICK = "";//"mainpage_settings_more_about_click";//关注more about按钮点击
    public static final String EVENT_FUNC_SETTING_NOTIFY_CLICK = "";//"mainpage_settings_notif_click";//通知
    public static final String EVENT_FUNC_SETTING_NOTIFY_RATE = "";//"mainpage_settings_notif_rate";//通知

    // 商店内容更新通知
    public static final String EVENT_FUNC_NOTIF_STORE_UPDATE_SHOW = "";//"notif_contentupdate_show";
    public static final String EVENT_FUNC_NOTIF_STORE_UPDATE_CLICK = "";//"notif_contentupdate_click";
    public static final String EVENT_FUNC_NOTIF_STORE_UPDATE_DOWNLOAD = "";//"notif_contentupdate_download_click";
    public static final String EVENT_FUNC_NOTIF_STORE_UPDATE_APPLY = "";//"notif_contentupdate_apply_click";
    public static final String EVENT_FUNC_NOTIF_STORE_UPDATE_SAVE = "";//"notif_contentupdate_save_click";
    public static final String EVENT_FUNC_NOTIF_STORE_UPDATE_SHARE = "";//"notif_contentupdate_share_click";

    //沉默用户激活通知
    public static final String EVENT_FUNC_NOTIF_INACTION_SHOW = "";//"notif_inaction_show";
    public static final String EVENT_FUNC_NOTIF_INACTION_CLICK = "";//"notif_inaction_click";

    //节假日激活通知
    public static final String EVENT_FUNC_NOTIF_HOLIDAY_SHOW = "";//"notif_holiday_show";
    public static final String EVENT_FUNC_NOTIF_HOLIDAY_CLICK = "";//"notif_holiday_click";

    //grid edit freestyle beauty template 子页面事件统计 均是事件
    public static final String EVENT_FUNC_MAINPAGE_ = "";//"mainpage_";
    public static final String MAIN_BTN_COLLAGE = "main";//"collage";
    public static final String MAIN_BTN_EDIT = "";//"edit";
    public static final String MAIN_BTN_FREESTYLE = "";//"freestyle";
    public static final String MAIN_BTN_TEMPLATE = "";//"template";
    public static final String MAIN_BTN_BEAUTY = "";//"beauty";
    public static final String EVENT_FUNC_ALBUM_BACK = "";//"_album_back_click";//相册选择页面返回按钮点击
    public static final String EVENT_FUNC_ALBUM_START = "";//"_ablum_start_click";//相册选择页面开始按钮点击
    public static final String EVENT_FUNC_EDIT_CANCEL = "";//"_edit_cancel_click";//编辑页面取消按钮点击
    public static final String EVENT_FUNC_EDIT_SAVE = "";//"_edit_save_click";//编辑页面保存按钮点击
    public static final String EVENT_FUNC_SAVE_DELETE = "";//"_saved_delete_click";//保存后相册内容详情页删除按钮点击
    public static final String EVENT_FUNC_SAVE_EDIT = "";//"_saved_edit_click";//保存后相册内容详情页编辑按钮点击
    public static final String EVENT_FUNC_SAVE_SHARE = "";//"_saved_share_click";//保存后相册内容详情页分享按钮点击

}

