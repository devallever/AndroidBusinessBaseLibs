package app.allever.android.sample.dj.csj;

/**
 * Created by MI on 2025/5/31
 * Describe:
 */
public interface OnPlayInfoListener {
    void onDJXPageChange();
    void onPlaySpeedBtnClick(String holderKey,String speed);
    void onChangePlaySpeed(String speed);
    void onDJXVideoPlay(long drama_id,int ep_index);
    //视频暂停播放时回调
    void onDJXVideoPause();
    //视频继续播放时回调
    void onDJXVideoContinue();
    void onDJXVideoCompletion(long drama_id,int ep_index);
    void onShareClick();

    void unlockFlowStart();
}
