package com.allever.video.editor.function.share;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.android.absbase.App;

import java.io.IOException;
import java.io.InputStream;

public class ShareImageDialogHelp {

    //图片转换为 1:1
    public static Uri transferForIns(Uri uri) {
        try {
            InputStream input = App.getContext().getContentResolver().openInputStream(uri);
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();
            int originWidth = onlyBoundsOptions.outWidth;
            int originHeight = onlyBoundsOptions.outHeight;
            float ratio = originWidth * 1.0f / originHeight;
            if (ratio == 1) {
                return uri;
            } else if (ratio > 1) {
                input = App.getContext().getContentResolver().openInputStream(uri);
                onlyBoundsOptions.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
                input.close();
                Bitmap newBitmap = Bitmap.createBitmap(originWidth, originWidth, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newBitmap);
                canvas.drawColor(Color.WHITE);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setFilterBitmap(true);
                canvas.drawBitmap(bitmap, 0, (originWidth - originHeight) / 2, paint);
                bitmap.recycle();
                Uri parse = Uri.parse(MediaStore.Images.Media.insertImage(App.getContext().getContentResolver(), newBitmap, null, null));
                return parse;
            } else {
                input = App.getContext().getContentResolver().openInputStream(uri);
                onlyBoundsOptions.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
                input.close();
                Bitmap newBitmap = Bitmap.createBitmap(originHeight, originHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newBitmap);
                canvas.drawColor(Color.WHITE);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setFilterBitmap(true);
                canvas.drawBitmap(bitmap, (originHeight - originWidth) / 2, 0, paint);
                bitmap.recycle();
                String newUrl = MediaStore.Images.Media.insertImage(App.getContext().getContentResolver(), newBitmap, null, null);
                Uri parse = uri;
                if (!TextUtils.isEmpty(newUrl)) {
                    parse = Uri.parse(newUrl);
                }
                return parse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
