package com.allever.video.editor.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Test {

    public void inputStream2File(InputStream ins, File outFile){
        try {
            OutputStream outStream = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = ins.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            ins.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
