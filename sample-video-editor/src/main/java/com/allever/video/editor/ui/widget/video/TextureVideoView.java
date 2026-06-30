package com.allever.video.editor.ui.widget.video;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.android.absbase.helper.log.DLog;
import com.allever.video.editor.R;
import com.allever.video.editor.ui.widget.video.ScaleManager.ScaleType;
import com.allever.video.editor.ui.widget.video.ScaleManager.Size;

import java.io.IOException;

/**
 * @author
 */
public class TextureVideoView extends TextureView
        implements TextureView.SurfaceTextureListener,
        Handler.Callback,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = TextureVideoView.class.getSimpleName();

    private volatile int mCurrentState = STATE_IDLE;
    private volatile int mTargetState  = STATE_IDLE;

    private static final int STATE_ERROR              = -1;
    private static final int STATE_IDLE               = 0;
    private static final int STATE_PREPARING          = 1;
    private static final int STATE_PREPARED           = 2;
    private static final int STATE_PLAYING            = 3;
    private static final int STATE_PAUSED             = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;

    private static final int MSG_START = 0x0001;
    private static final int MSG_PAUSE = 0x0004;
    private static final int MSG_STOP = 0x0006;
    private static final int MSG_RESUME = 0x0008;

    private String mAssetsName;
    private Uri mUri;
    private Context mContext;
    private Surface mSurface;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer mNextMediaPlayer;
    private AudioManager mAudioManager;

    private MediaPlayerCallback mMediaPlayerCallback;
    private Handler mHandler;
    private Handler mVideoHandler;

    private boolean mSoundMute;
    private boolean mHasAudio;

    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    private boolean mAutoAdjustSize = true;

    private boolean mAutoPlay = false;
    private int mId;

    private boolean mPlayLooping = false;

    private boolean whetherEnd2Start = false;
    private static final HandlerThread sThread = new HandlerThread("VideoPlayThread");
    static {
        sThread.start();
    }

    public void setPlayLooping(boolean playLooping) {
        this.mPlayLooping = playLooping;
    }

    public boolean isWhetherEnd2Start() {
        return whetherEnd2Start;
    }

    public void setWheterEnd2Start(boolean whetherEnd2Start) {
        this.whetherEnd2Start = whetherEnd2Start;
    }

    public interface MediaPlayerCallback {
        void onPrepared(MediaPlayer mp, int id);
        void onCompletion(MediaPlayer mp, int id);
        void onBufferingUpdate(MediaPlayer mp, int percent, int id);
        void onVideoSizeChanged(MediaPlayer mp, int width, int height, int id);

        boolean onInfo(MediaPlayer mp, int what, int extra, int id);
        boolean onError(MediaPlayer mp, int what, int extra, int id);
    }

    public TextureVideoView(Context context) {
        this(context, null);
    }

    public TextureVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.scaleStyle, 0, 0);
            if (a != null) {
                int scaleType = a.getInt(R.styleable.scaleStyle_scaleType, ScaleType.NONE.ordinal());
                a.recycle();
                mScaleType = ScaleType.values()[scaleType];
            }
        }
        init();
    }

    public void setAutoAdjustSize(boolean auto) {
        mAutoAdjustSize = auto;
    }

    public void setMediaPlayerCallback(MediaPlayerCallback mediaPlayerCallback, int id) {
        mMediaPlayerCallback = mediaPlayerCallback;
        mId = id;
        if (mediaPlayerCallback == null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }

        return -1;
    }

    public int getVideoHeight() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVideoHeight();
        }
        return 0;
    }

    public int getVideoWidth() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getVideoWidth();
        }
        return 0;
    }

    public void refreshVideoLayout() {
        setScaleType(mScaleType);
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
        scaleVideoSize(getVideoWidth(), getVideoHeight());
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    private void scaleVideoSize(int videoWidth, int videoHeight) {
        if (videoWidth == 0 || videoHeight == 0) {
            return;
        }

        Size viewSize = new Size(getWidth(), getHeight());
        Size videoSize = new Size(videoWidth, videoHeight);
        ScaleManager scaleManager = new ScaleManager(viewSize, videoSize);
        final Matrix matrix = scaleManager.getScaleMatrix(mScaleType);
        if (matrix == null) {
            return;
        }

        if(Looper.myLooper() == Looper.getMainLooper()) {
            setTransform(matrix);
        } else {
            mHandler.postAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    setTransform(matrix);
                }
            });
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        synchronized (TextureVideoView.class) {
            switch (msg.what) {
                case MSG_START:
                    openVideo();
                    break;

                case MSG_PAUSE:
                    if(mMediaPlayer != null) {
                        mMediaPlayer.pause();
                    }
                    mCurrentState = STATE_PAUSED;
                    break;

                case MSG_STOP:
                    release(true);
                    break;

                case MSG_RESUME:
                    if(mMediaPlayer != null) {
                        mMediaPlayer.start();
                    }
                    mCurrentState = STATE_PLAYING;
                    break;
            }
        }
        return true;
    }

    private void init() {
        if(!isInEditMode()) {
            mContext = getContext();
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mHandler = new Handler();
            mVideoHandler = new Handler(sThread.getLooper(), this);
            setSurfaceTextureListener(this);
        }
    }

    // release the media player in any state
    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState  = STATE_IDLE;
            }
//            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//            am.abandonAudioFocus(null);
        }
        if (mNextMediaPlayer != null) {
            if (mNextMediaPlayer.isPlaying()) {
                mNextMediaPlayer.stop();
            }
            mNextMediaPlayer.setOnPreparedListener(null);
            mNextMediaPlayer.setOnCompletionListener(null);
            mNextMediaPlayer.setOnSeekCompleteListener(null);
            mNextMediaPlayer.setOnCompletionListener(null);
            mNextMediaPlayer.setOnErrorListener(null);
            mNextMediaPlayer.setOnVideoSizeChangedListener(null);
            mNextMediaPlayer.setOnInfoListener(null);
            mNextMediaPlayer.setOnBufferingUpdateListener(null);
            mNextMediaPlayer.reset();
            mNextMediaPlayer.release();
            mNextMediaPlayer = null;
        }
    }

    private void openVideo() {
        if ((mUri == null && TextUtils.isEmpty(mAssetsName)) || mSurface == null || mTargetState != STATE_PLAYING) {
            // not ready for playback just yet, will try again later
            return;
        }

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false);

        try {
            mMediaPlayer = new MediaPlayer();
            if (isMute()) {
                mute();
            }
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
            if (mUri != null) {
                mMediaPlayer.setDataSource(mContext, mUri);
            } else if (!TextUtils.isEmpty(mAssetsName)) {
                AssetFileDescriptor fd = getContext().getAssets().openFd(mAssetsName);
                mMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (mPlayLooping) {
                mMediaPlayer.setLooping(true);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    mNextMediaPlayer = new MediaPlayer();
                    if (mUri != null) {
                        mNextMediaPlayer.setDataSource(mContext, mUri);
                    } else if (!TextUtils.isEmpty(mAssetsName)) {
                        AssetFileDescriptor fd = getContext().getAssets().openFd(mAssetsName);
                        mNextMediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                    }
                    mNextMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            try {
                                mMediaPlayer.setNextMediaPlayer(mp);
                            } catch (IllegalArgumentException e) {
                                /**
                                 * setNextMediaPlayer可能报failed.: status=0xFFFFFFED，即-19
                                 * 这错误是重复new MediaPlayer且没有及时释放导致的
                                 * 在这里原因不明，网上报错都是在该rom的网上，怀疑与机型有关，故catch
                                 **/
                                e.printStackTrace();
                            }
                        }
                    });
                    mNextMediaPlayer.prepareAsync();
                }
            }
            mMediaPlayer.prepareAsync();

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING;
            mTargetState = STATE_PREPARING;

            mHasAudio = true;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                try {
                    MediaExtractor mediaExtractor = new MediaExtractor();
                    mediaExtractor.setDataSource(mContext, mUri, null);
                    MediaFormat format;
                    for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                        format = mediaExtractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime.startsWith("audio/")) {
                            mHasAudio = true;
                            break;
                        }
                    }
                } catch (Exception ex) {
                    // may be failed to instantiate extractor.
                }
            }

        } catch (IOException | IllegalArgumentException ex) {
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            if (mMediaPlayerCallback != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mMediaPlayerCallback != null) {
                            mMediaPlayerCallback.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0, mId);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        if(mTargetState == STATE_PLAYING) {
            start();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        stop();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void setVideoFilePath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
    }

    public void setVideoAssetsName(String name) {
        mAssetsName = name;
    }

    public void prepare(){
        if(mTargetState != STATE_PLAYING){
            mTargetState = STATE_PLAYING;
            mVideoHandler.obtainMessage(MSG_START).sendToTarget();
        }
    }
    public void start() {
        mTargetState = STATE_PLAYING;

        if (isInPlaybackState()) {
            mVideoHandler.obtainMessage(MSG_STOP).sendToTarget();
        }

        if((!TextUtils.isEmpty(mAssetsName) || mUri != null) && mSurface != null) {
            mVideoHandler.obtainMessage(MSG_START).sendToTarget();
        }
    }

    public void pause() {
        mTargetState = STATE_PAUSED;

        if (isPlaying()) {
            mVideoHandler.obtainMessage(MSG_PAUSE).sendToTarget();
        }
    }

    public void resume() {
        if (mMediaPlayer == null) {
            start();
            return;
        }
        mTargetState = STATE_PLAYING;

        if (!isPlaying()) {
            mVideoHandler.obtainMessage(MSG_RESUME).sendToTarget();
        }
    }

    public void stop() {
        mTargetState = STATE_PLAYBACK_COMPLETED;

        if(isInPlaybackState()) {
            mVideoHandler.obtainMessage(MSG_STOP).sendToTarget();
        }
    }

    public boolean isPlaying() {
        try {
            return isInPlaybackState() && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * 是否自动播放
     * @param mAutoPlay
     */
    public void setAutoPlay(boolean mAutoPlay) {
        this.mAutoPlay = mAutoPlay;
    }
    public void mute() {
        if(mMediaPlayer != null) {
            mMediaPlayer.setVolume(0, 0);
            mSoundMute = true;
        }
    }

    public void unMute() {
        if (mAudioManager != null && mMediaPlayer != null) {
            int max = 100;
            int audioVolume = 100;
            double numerator = max - audioVolume > 0 ? Math.log(max - audioVolume) : 0;
            float volume = (float) (1 - (numerator / Math.log(max)));
            mMediaPlayer.setVolume(volume, volume);
            mSoundMute = false;
        }
    }

    public boolean isMute() {
        return mSoundMute;
    }

    public boolean isHasAudio() {
        return mHasAudio;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    @Override
    public void onCompletion(final MediaPlayer mp) {
        mCurrentState = STATE_PLAYBACK_COMPLETED;
        mTargetState = STATE_PLAYBACK_COMPLETED;
        if (mMediaPlayerCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //播放完成从头开始，
                    if (whetherEnd2Start) {
                        seekTo(0);
                    }
                    if(mMediaPlayerCallback != null) {
                        mMediaPlayerCallback.onCompletion(mp, mId);
                    }
                }
            });
        }
    }

    @Override
    public boolean onError(final MediaPlayer mp, final int what, final int extra) {
        mCurrentState = STATE_ERROR;
        mTargetState = STATE_ERROR;
        if (mMediaPlayerCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mMediaPlayerCallback != null) {
                        mMediaPlayerCallback.onError(mp, what, extra, mId);
                    }
                }
            });
        }
        return true ;
    }

    @Override
    public void onPrepared(final MediaPlayer mp) {
        if (mTargetState != STATE_PREPARING || mCurrentState != STATE_PREPARING) {
            return;
        }

        mCurrentState = STATE_PREPARED;
        mTargetState = STATE_PREPARED;

        if(mAutoPlay){
            if (isInPlaybackState()) {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
                mTargetState = STATE_PLAYING;
            }

        }
        if (mMediaPlayerCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mMediaPlayerCallback != null) {
                        mMediaPlayerCallback.onPrepared(mp, mId);
                    }
                }
            });
        }
    }

    @Override
    public void onVideoSizeChanged(final MediaPlayer mp, final int width, final int height) {
        if (mAutoAdjustSize) {
            scaleVideoSize(width, height);
        }
        if (mMediaPlayerCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mMediaPlayerCallback != null) {
                        mMediaPlayerCallback.onVideoSizeChanged(mp, width, height, mId);
                    }
                }
            });
        }
    }

    @Override
    public void onBufferingUpdate(final MediaPlayer mp, final int percent) {
        if (mMediaPlayerCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mMediaPlayerCallback != null) {
                        mMediaPlayerCallback.onBufferingUpdate(mp, percent, mId);
                    }
                }
            });
        }
    }

    @Override
    public boolean onInfo(final MediaPlayer mp, final int what, final int extra) {
        if (mMediaPlayerCallback != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mMediaPlayerCallback != null) {
                        mMediaPlayerCallback.onInfo(mp, what, extra, mId);
                    }
                }
            });
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    public void seekTo(int time) {
        if (mMediaPlayer == null) {
            prepare();
            return;
        }
        try {
            mMediaPlayer.seekTo(time);
        } catch (Exception e) {
            DLog.printStackTrace(e);
        }
    }
}
