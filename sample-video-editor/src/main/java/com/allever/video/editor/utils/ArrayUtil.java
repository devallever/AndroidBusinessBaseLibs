package com.allever.video.editor.utils;

import java.util.ArrayList;

public class ArrayUtil {

    public static <T> ArrayList<T> copyArray(ArrayList<T> data){
        ArrayList<T> result = new ArrayList<T>();
        int l = data.size();
        for(int i = 0 ; i < l ; i++){
            result.add(data.get(i));
        }
        return result;
    }
}
