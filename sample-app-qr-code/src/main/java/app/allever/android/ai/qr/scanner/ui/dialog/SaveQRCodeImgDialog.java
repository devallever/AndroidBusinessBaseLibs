package app.allever.android.ai.qr.scanner.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;

import com.android.absbase.utils.ToastUtils;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.allever.app.qr.code.scaner.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.allever.android.lib.core.helper.MediaHelperKt;

@SuppressLint("ValidFragment")
public class SaveQRCodeImgDialog extends DialogFragment {

    private OnSaveQrCodeListener mListener;

    private Bitmap bitmap;

    private Context mContext;

    @SuppressLint("ValidFragment")
    public SaveQRCodeImgDialog(Context context, Bitmap bitmap, OnSaveQrCodeListener onSaveQRCodeListener){
        this.mListener = onSaveQRCodeListener;
        this.bitmap = bitmap;
        this.mContext = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.qr_dialog_save_qr_code_image,null);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();

        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        TextView tvConfirm = view.findViewById(R.id.tv_confirm);
        final EditText etFileName = view.findViewById(R.id.et_file_name);

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName = format.format(date);
        etFileName.setText(fileName);
        etFileName.setSelection(fileName.length());

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) mListener.onCancelClick(SaveQRCodeImgDialog.this);
            }
        });

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null){
                    String fileName = etFileName.getText().toString();
                    if (TextUtils.isEmpty(fileName)){
                        ToastUtils.INSTANCE.show(getString(R.string.qr_input_file_name));
                    }else {
                        //java调用kotlin的扩展方法
                        //saveToAlbum(App.context, fileName, App.context.getString(R.string.app_name))
                        MediaHelperKt.saveToAlbum(bitmap, mContext, fileName + ".jpg", ContextCompat.getString(mContext, R.string.qr_app_name), 75);
//                        String resultFileName = saveFile(bitmap, fileName + ".jpg");
                        mListener.onConfirmClick(SaveQRCodeImgDialog.this, fileName + ".jpg");
                    }

                }
            }
        });

        return alertDialog;
    }

    private String saveFile(Bitmap bitmap, String fileName) {
        if (bitmap == null) return "";
        String dir = Environment.getExternalStorageDirectory() + "/qrcode";
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File imageFile = new File(dir, fileName);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imageFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile.getPath();
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public interface OnSaveQrCodeListener {
        void onCancelClick(DialogFragment dialog);
        void onConfirmClick(DialogFragment dialog, String fileName);
    }
}
