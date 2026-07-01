//package com.allever.video.editor.function.ad;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import android.text.TextUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 信息流等广告的位置信息
// *
// */
//public class AdFlowPosition {
//    ArrayList<Position> mPositions = new ArrayList<>();
//    int mBannerCount = 0;
//    int mIconCount = 0;
//
//    /**
//     * 创建一个广告信息流位置信息
//     *
//     * @param str：如1:2,3,4:5 代表第一行第二列，第三行，第四行第5列
//     * @return
//     */
//    public static AdFlowPosition create(@Nullable String str) {
//        AdFlowPosition adFlowPosition = new AdFlowPosition();
//        if (TextUtils.isEmpty(str)) {
//            return adFlowPosition;
//        }
//        String[] units = str.trim().split("\\s*,\\s*");
//        for (int i = 0; i < units.length; i++) {
//            String[] rowAndCol = units[i].split("\\s*:\\s*");
//            Position position = new Position();
//            if (rowAndCol.length > 0) {
//                position.row = string2int(rowAndCol[0], -1);
//                if (rowAndCol.length > 1) {
//                    position.col = string2int(rowAndCol[1], -1);
//                }
//            }
//            if (position.isValid()) {
//                if (position.isBanner()) {
//                    adFlowPosition.mBannerCount++;
//                } else if (position.isIcon()) {
//                    adFlowPosition.mIconCount++;
//                }
//                adFlowPosition.mPositions.add(position);
//            }
//        }
//        return adFlowPosition;
//    }
//
//    public static AdFlowPosition getDefault() {
//        return create("2,4");
//    }
//
//    private static int string2int(@NonNull String valueStr, int defaultValue) {
//        int value = defaultValue;
//        try {
//            value = Integer.parseInt(valueStr.trim());
//        } catch (Exception ignored) {
//
//        }
//        return value;
//    }
//
//    public final List<Position> getPositions() {
//        return mPositions;
//    }
//
//    public int size() {
//        return mPositions.size();
//    }
//
//    public int getBannerCount() {
//        return mBannerCount;
//    }
//
//    public int getIconCount() {
//        return mIconCount;
//    }
//
//    @NonNull
//    public final List<Position> getBannerPositions() {
//        List<Position> positions = new ArrayList<>();
//        for (Position pos : mPositions) {
//            if (pos.isBanner()) {
//                positions.add(pos);
//            }
//        }
//        return positions;
//    }
//
//    @NonNull
//    public final List<Position> getIconPositions() {
//        List<Position> positions = new ArrayList<>();
//        for (Position pos : mPositions) {
//            if (pos.isIcon()) {
//                positions.add(pos);
//            }
//        }
//        return positions;
//    }
//
//
//    public static class Position {
//        int row = -1;
//        int col = -1;
//
//        public int getRow() {
//            return row;
//        }
//
//        public int getCol() {
//            return col;
//        }
//
//        public boolean isValid() {
//            return row != -1;
//        }
//
//        public boolean isBanner() {
//            return row != -1 && col == -1;
//        }
//
//        public boolean isIcon() {
//            return row != -1 && col != -1;
//        }
//    }
//}
