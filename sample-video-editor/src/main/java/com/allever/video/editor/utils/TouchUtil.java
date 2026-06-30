package com.allever.video.editor.utils;


import com.android.absbase.App;
import com.android.absbase.utils.DeviceUtils;

public class TouchUtil {
	/**
	 * 用于移除那些比较少的移动 防止抖动
	 */
	public static final int OFFSET = DeviceUtils.dip2px(App.getContext(), 3);
	
	/**
	 * 用于小Button增加点击区域
	 */
	public static final int TOUCH_BUTTON_OFFSET = DeviceUtils.dip2px(App.getContext(), 4);

	/**
	 * 操作上下左右拉伸的半径
	 */
	public static final int TOUCH_LINE_OFFSET = DeviceUtils.dip2px(App.getContext(), 8);
}
