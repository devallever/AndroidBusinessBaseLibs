package com.allever.video.editor.glide;

import android.content.Context;
import android.os.Build;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 *
 */

//@GlideModule
public final class GlideAPPModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDefaultRequestOptions(getDefaultOptions());
    }

    public static RequestOptions getDefaultOptions() {
        RequestOptions options = new RequestOptions()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .dontTransform()
                .dontAnimate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            options = options.disallowHardwareConfig();
        }
        return options;
    }
}
