package com.allever.video.editor.ui.widget.gesture;

import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.Nullable;
import android.view.View;

import com.allever.video.editor.function.editor.bean.EffectBean;

import org.jetbrains.annotations.NotNull;


public interface IContentView {
    /**
     * 获取相应的特效View
     *
     * @param bean
     * @return
     */
    @Nullable
    View getEffectView(@Nullable EffectBean bean);

    void removeEffectView(@NotNull EffectBean bean);

    void updateState();

    void invalidateSelf();

    /**
     * 获取子view的相对位置, 相对裁剪区域(输出视频实际位置大小的区域)
     * @param bean
     * @return
     */
    RectF getEffectViewRect(@Nullable EffectBean bean);

    Rect getVideoRect();
}
