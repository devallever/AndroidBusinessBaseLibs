package com.hd.calculator.app.business;

import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.TaxType;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.TaxEntity;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaxManager {
    //static inner class
    private static class TaxManagerHolder {
        public static TaxManager instance = new TaxManager();
    }

    public static TaxManager getIns() {
        return TaxManagerHolder.instance;
    }


    private final Map<Integer, TaxEntity> mTaxMap = new HashMap<>();

    public void updateTaxData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<TaxEntity> taxList = DataBaseRepository.getInstance().getTaxList();
            for (TaxEntity taxEntity : taxList) {
                mTaxMap.put(taxEntity.getType(), taxEntity);
            }
        });
    }

    /**
     * 获取对应类型的税 0.02,
     *
     * @param orderType
     * @param dishesFirstType
     * @return
     */
    public float getTaxRate(int orderType, int dishesFirstType) {
        TaxEntity taxEntity = null;
        if (orderType == OrderType.ORDER_TYPE_IN_HOUSE && dishesFirstType == DishesFirstSortType.DRINK) {
            taxEntity = mTaxMap.get(TaxType.DRINK_IN_HOUSE_RATE);
        } else if (orderType == OrderType.ORDER_TYPE_IN_HOUSE && dishesFirstType == DishesFirstSortType.FOOD) {
            taxEntity = mTaxMap.get(TaxType.FOOD_IN_HOUSE_RATE);
        } else if (orderType == OrderType.ORDER_TYPE_TAKE_OUT && dishesFirstType == DishesFirstSortType.DRINK) {
            taxEntity = mTaxMap.get(TaxType.DRINK_TAKEOUT_RATE);
        } else if (orderType == OrderType.ORDER_TYPE_TAKE_OUT && dishesFirstType == DishesFirstSortType.FOOD) {
            taxEntity = mTaxMap.get(TaxType.FOOD_TAKEOUT_RATE);
        }

        if (taxEntity == null) {
            return 0;
        }

        return taxEntity.getRate();
    }

    //getTaxSign
    public String getTaxSign(int orderType, long dishesFirstType) {
        TaxEntity taxEntity = null;
        if (orderType == OrderType.ORDER_TYPE_IN_HOUSE && dishesFirstType == DishesFirstSortType.DRINK) {
            taxEntity = mTaxMap.get(TaxType.DRINK_IN_HOUSE_RATE);
        } else if (orderType == OrderType.ORDER_TYPE_IN_HOUSE && dishesFirstType == DishesFirstSortType.FOOD) {
            taxEntity = mTaxMap.get(TaxType.FOOD_IN_HOUSE_RATE);
        } else if (orderType == OrderType.ORDER_TYPE_TAKE_OUT && dishesFirstType == DishesFirstSortType.DRINK) {
            taxEntity = mTaxMap.get(TaxType.DRINK_TAKEOUT_RATE);
        } else if (orderType == OrderType.ORDER_TYPE_TAKE_OUT && dishesFirstType == DishesFirstSortType.FOOD) {
            taxEntity = mTaxMap.get(TaxType.FOOD_TAKEOUT_RATE);
        }

        if (taxEntity == null) {
            return "";
        }

        return taxEntity.getSign();
    }

    public String getSingRate(int orderType, int dishesFirstType) {
        return getTaxSign(orderType, dishesFirstType) + getTaxRate(orderType, dishesFirstType) * 100 + "%";
    }

    public String getSingRate(int taxType) {
        if (taxType == TaxType.DRINK_IN_HOUSE_RATE) {
            return getTaxSign(OrderType.ORDER_TYPE_IN_HOUSE, DishesFirstSortType.DRINK) + " " +  getTaxRate(OrderType.ORDER_TYPE_IN_HOUSE, DishesFirstSortType.DRINK) * 100 + "%";
        } else if (taxType == TaxType.DRINK_TAKEOUT_RATE) {
            return getTaxSign(OrderType.ORDER_TYPE_TAKE_OUT, DishesFirstSortType.DRINK) + " " +  getTaxRate(OrderType.ORDER_TYPE_TAKE_OUT, DishesFirstSortType.DRINK) * 100 + "%";
        } else if (taxType == TaxType.FOOD_IN_HOUSE_RATE) {
            return getTaxSign(OrderType.ORDER_TYPE_IN_HOUSE, DishesFirstSortType.FOOD) + " " +  getTaxRate(OrderType.ORDER_TYPE_IN_HOUSE, DishesFirstSortType.FOOD) * 100 + "%";
        } else if (taxType == TaxType.FOOD_TAKEOUT_RATE) {
            return getTaxSign(OrderType.ORDER_TYPE_TAKE_OUT, DishesFirstSortType.FOOD) + " " + getTaxRate(OrderType.ORDER_TYPE_TAKE_OUT, DishesFirstSortType.FOOD) * 100 + "%";
        } else {
            return "";
        }
    }

    public float getTaxRate(int taxType) {
        if (taxType == TaxType.DRINK_IN_HOUSE_RATE) {
            return getTaxRate(OrderType.ORDER_TYPE_IN_HOUSE, DishesFirstSortType.DRINK);
        } else if (taxType == TaxType.DRINK_TAKEOUT_RATE) {
            return getTaxRate(OrderType.ORDER_TYPE_TAKE_OUT, DishesFirstSortType.DRINK);
        } else if (taxType == TaxType.FOOD_IN_HOUSE_RATE) {
            return getTaxRate(OrderType.ORDER_TYPE_IN_HOUSE, DishesFirstSortType.FOOD) ;
        } else if (taxType == TaxType.FOOD_TAKEOUT_RATE) {
            return getTaxRate(OrderType.ORDER_TYPE_TAKE_OUT, DishesFirstSortType.FOOD);
        } else {
            return 0;
        }
    }
}
