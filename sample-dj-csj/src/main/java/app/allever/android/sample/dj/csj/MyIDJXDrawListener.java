package app.allever.android.sample.dj.csj;

import com.bytedance.sdk.djx.interfaces.listener.IDJXDrawListener;

import java.util.List;
import java.util.Map;


public class MyIDJXDrawListener extends IDJXDrawListener {

    private OnPlayInfoListener onPlayInfoListener;

    public MyIDJXDrawListener(OnPlayInfoListener listener) {
        this.onPlayInfoListener = listener;
    }

    @Override
    public void onDJXRequestSuccess(List<Map<String, Object>> list) {
        super.onDJXRequestSuccess(list);
    }

    @Override
    public void onDJXPageChange(int i, Map<String, Object> map) {
        super.onDJXPageChange(i, map);

    }

    @Override
    public void onDJXVideoPlay(Map<String, Object> map) {
        super.onDJXVideoPlay(map);
        if (map != null) {
            if (onPlayInfoListener != null) {
                long drama_id = (long) map.get("drama_id");
                int ep_index = (int) map.get("index");
                onPlayInfoListener.onDJXVideoPlay(drama_id, ep_index);
            }
        }
    }

    @Override
    public void onDJXVideoPause(Map<String, Object> map) {
        super.onDJXVideoPause(map);
        if (onPlayInfoListener != null) {
            onPlayInfoListener.onDJXVideoPause();
        }
    }

    @Override
    public void onDJXVideoContinue(Map<String, Object> map) {
        super.onDJXVideoContinue(map);
        if (onPlayInfoListener != null) {
            onPlayInfoListener.onDJXVideoContinue();
        }
    }

    @Override
    public void onDJXVideoCompletion(Map<String, Object> map) {
        super.onDJXVideoCompletion(map);
        if (map != null) {
            if (onPlayInfoListener != null) {
                long drama_id = (long) map.get("drama_id");
                int ep_index = (int) map.get("index");
                onPlayInfoListener.onDJXVideoCompletion(drama_id, ep_index);
            }
        }

    }

//
//    public MyIDJXDrawListener() {
//        super();
//    }

    private void requestDJInfo(long id) {
//        EasyHttp.get(lifecycleOwner)
//                .api(new VideoApi(VideoApi.videoInfoApi)
//                        .setVideoInfoParam(null, String.valueOf(id)))
//                .request(new HttpCallbackProxy<HttpData<VideoDetail>>(null) {
//                    @Override
//                    public void onHttpSuccess(@NonNull HttpData<VideoDetail> result) {
//                        if (result != null && result.getData() != null) {
//                            if (holders.containsKey(String.valueOf(id))) {
//                                holders.get(String.valueOf(id)).notifyVideoDetail(result.getData());
//                            }
//                        }
//                    }
//                });
    }

}
