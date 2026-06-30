package com.allever.video.editor.function.timeline;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.IntDef;

import com.allever.video.editor.function.editor.bean.EffectListBean;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Vector;


/**
 * 视频时间线控制器
 *
 * @author dell
 */
public class TimeLineController {
    /**
     * 监听器列表
     */
    private final Vector<TimeDispatchEventByControllerListener> mListeners = new Vector<>();
    /**
     * 帧速度
     * 60帧/秒
     */
    private static final int FRAME_RATE = 60;

    /**
     * 帧间隔
     * 即 1s/60 约等 16ms
     */
    public  static final Long FRAME_TIME = 1_000L / FRAME_RATE;

    /**
     * 当前播放状态
     * 0，未开始
     * 1，开始播放
     * 2，播放中
     * 3，暂停
     * 4  播放完成
     */
    @PlayState
    private int mCurrentPlayState = PlayState.PAUSE;
    /**
     * 前一个播放状态
     */
    @PlayState
    private int mPrevPlayState = PlayState.PAUSE;

    @IntDef(value = {
            PlayState.NONE_PLAY,
            PlayState.START_PLAY,
            PlayState.PLAYING,
            PlayState.PAUSE,
            PlayState.END_PLAY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayState {
        int NONE_PLAY = 0;
        int START_PLAY = 1;
        int PLAYING = 2;
        int PAUSE = 3;
        int END_PLAY = 4;
    }

    /**
     * 总时长（真正的）
     */
    private long mReallyTotalTime;

    /**
     * 当前时间轴的位置(从视频起点开始计算)
     */
    private long currentTimelineIndex;

    /**
     * 播放状态中前一个时间戳
     */
    private long prevTimeMillis = 0L;

    /**
     * 时间轴上所有的媒体资源
     */
    private final EffectListBean mEffectList;


    /**
     * 分发事件
     */
    private Handler mHandler;

    public TimeLineController(Context context, EffectListBean beans) {
        mEffectList = beans;
        mHandler = new Handler(context.getMainLooper());
        if(beans != null){
            setEffectListBean(beans);
        }
    }

    /**
     * 设置数据
     * @param beans
     */
    public void setEffectListBean(EffectListBean beans) {
        this.mEffectList.clear();
        this.mEffectList.addAll(beans.getBeans());
        calculatedTotalTime();
    }

    public void update() {
        calculatedTotalTime();
    }


    /**
     * 计算时间并回调
     */
    private void dispatch() {
        switch (mCurrentPlayState) {
            case PlayState.NONE_PLAY:
                break;
            case PlayState.START_PLAY:
                fixDispatch(true);
                break;
            case PlayState.PLAYING:
                break;
            case PlayState.PAUSE:
                break;
            case PlayState.END_PLAY:
                break;
            default:
                break;
        }
    }

    /**
     * 处理分发
     * @param auto  自动
     */
    private void fixDispatch(boolean auto) {
//        SystemUtils.log("auto " + auto +", currentTimelineIndex "+ currentTimelineIndex );
        long currentTimeMillis = System.currentTimeMillis();
        if (prevTimeMillis > 0) {
            currentTimelineIndex += currentTimeMillis - prevTimeMillis;
        }
        long offset = currentTimelineIndex - mReallyTotalTime;
        if (offset < 0) {
            frameAtTime(0, currentTimelineIndex, auto);
        } else if (offset < FRAME_TIME){
            currentTimelineIndex = mReallyTotalTime;
            frameAtTime(0, mReallyTotalTime, auto);
        }else {
            playFrameEnd();
        }
        prevTimeMillis = currentTimeMillis;
    }

    private void playFrameStart() {
        ArrayList<TimeDispatchEventByControllerListener> listeners = new ArrayList<>(mListeners);
        for (int i = 0; i < listeners.size(); i++) {
            TimeDispatchEventByControllerListener listener = listeners.get(i);
            if (listener != null) {
                listener.playFrameStart();
            }
        }
    }

    private void playFramePause() {
        ArrayList<TimeDispatchEventByControllerListener> listeners = new ArrayList<>(mListeners);
        for (int i = 0; i < listeners.size(); i++) {
            TimeDispatchEventByControllerListener listener = listeners.get(i);
            if (listener != null) {
                listener.playFramePause();
            }
        }
    }

    /**
     * 滚动的时候再启动时间轴
     */
    public void  onTimelineStart() {
        mCurrentPlayState = mPrevPlayState;
        toggle();
    }
    /**
     * 滚动时间轴
     */
    public void onTimelineOffset(long timeOffset) {
        offset(timeOffset);
    }
    /**
     * 滚动的时候暂停时间轴
     */
    public void  onTimelinePause() {
        mPrevPlayState = mCurrentPlayState;
        mCurrentPlayState = PlayState.PAUSE;
        toggle();
    }
    private void frameAtTime(long currentPlayTimeReferenceOffset, long currentPlayTimeReferenceStart, boolean auto) {
        ArrayList<TimeDispatchEventByControllerListener> listeners = new ArrayList<>(mListeners);
        for (int i = 0; i < listeners.size(); i++) {
            TimeDispatchEventByControllerListener listener = listeners.get(i);
            if (listener != null) {
                listener.frameAtTime(currentPlayTimeReferenceOffset, currentPlayTimeReferenceStart,auto );
            }
        }
    }
    private void playFrameEnd() {
        mCurrentPlayState = PlayState.END_PLAY;
        mHandler.removeCallbacksAndMessages(null);
        ArrayList<TimeDispatchEventByControllerListener> listeners = new ArrayList<>(mListeners);
        for (int i = 0; i < listeners.size(); i++) {
            TimeDispatchEventByControllerListener listener = listeners.get(i);
            if (listener != null) {
                listener.playFrameEnd();
            }
        }
    }

    public boolean isPlaying() {
        return mCurrentPlayState == PlayState.START_PLAY;
    }

    public boolean isNotPause() {
        return mCurrentPlayState != PlayState.PAUSE;
    }


    /**
     * 启动时间线（实际是改变状态）
     */
    public void start(boolean autoPlay) {
        //播放完成再次点击播放会重置时间线
        if(autoPlay && mReallyTotalTime - currentTimelineIndex < 0){
            resetTimeline();
        }
        if(mCurrentPlayState == PlayState.START_PLAY){
            return;
        }
        prevTimeMillis = 0;
        if (autoPlay) {
            mCurrentPlayState = PlayState.START_PLAY;
            playFrameStart();
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dispatch();
                    mHandler.postDelayed(this, FRAME_TIME);
                }
            });
        }
    }
    public void toggle(){
        if(mCurrentPlayState == PlayState.PAUSE){
            pause();
        }else{
            prevTimeMillis = 0;
            mCurrentPlayState = PlayState.START_PLAY;
            playFrameStart();
            mHandler.removeCallbacksAndMessages(null);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dispatch();
                    mHandler.postDelayed(this, FRAME_TIME);
                }
            });
        }
    }
    public void pause(){
        mCurrentPlayState = PlayState.PAUSE;
        mHandler.removeCallbacksAndMessages(null);
        playFramePause();
    }

    /**
     * 移动时间轴
     * @param timeOffset 偏移量
     */
    public void offset(long timeOffset) {
        prevTimeMillis = 0;
        currentTimelineIndex += timeOffset;
        if(currentTimelineIndex < 0 ){
            currentTimelineIndex = 0;
        }
        if(currentTimelineIndex >= mReallyTotalTime){
            currentTimelineIndex = mReallyTotalTime;
        }
        fixDispatch(false);
    }


    /**
     * 重置时间线
     */
    private void resetTimeline(){
        currentTimelineIndex = 0;
        prevTimeMillis = 0;
        mCurrentPlayState = PlayState.NONE_PLAY;
    }
    /**
     * 计算媒体资源的总时长
     */
    private void calculatedTotalTime() {
        mReallyTotalTime = mEffectList.getTotalDuration();
        if (currentTimelineIndex >= mReallyTotalTime) {
            currentTimelineIndex = mReallyTotalTime;
        }
    }


    public void removeListener(TimeDispatchEventByControllerListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    public void addListener(TimeDispatchEventByControllerListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public EffectListBean getMediaList() {
        return mEffectList;
    }

    /**
     * 设置媒体资源
     *
     * @param mMediaList
     * @return
     */
    public TimeLineController setMediaList(EffectListBean mMediaList) {
        this.mEffectList.addAll(mMediaList.getBeans());
        calculatedTotalTime();
        return this;
    }

    public long getReallyTotalTime() {
        return mReallyTotalTime;
    }

    /**
     * 总时长
     *
     * @param mReallyTotalTime
     */
    public TimeLineController setReallyTotalTime(long mReallyTotalTime) {
        this.mReallyTotalTime = mReallyTotalTime;
        return this;
    }


    public long getCurrentTimelineIndex() {
        return currentTimelineIndex;
    }

    /**
     * 清除监听
     */
    public void clearListener() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mListeners.clear();
    }

    public void destroy() {
        clearListener();
    }

    /**
     * 时间轴每隔16ms分发事件
     */
    public interface TimeDispatchEventByControllerListener {
        /**
         * 时间轴播放开始
         */
        void playFrameStart();

        void playFramePause();
        /**
         * 此时应当显示相应的帧
         *  @param currentPlayTimeReferenceOffset 当前视频的播放时间戳间距  参照物offset
         * @param currentPlayTimeReferenceStart
         * @param auto 手动滑动时间线 false
         */
        void frameAtTime(long currentPlayTimeReferenceOffset, long currentPlayTimeReferenceStart, boolean auto);

        /**
         * 时间轴播放结束
         */
        void playFrameEnd();
    }
}
