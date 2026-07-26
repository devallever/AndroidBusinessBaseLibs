package com.hd.calculator.app.business;

import java.util.HashSet;
import java.util.Set;

public class BuffetManager {

    // inner static class
    private static class BuffetManagerHolder {
        public static final BuffetManager instance = new BuffetManager();
    }

    // singleton
    public static BuffetManager getIns() {
        return BuffetManagerHolder.instance;
    }

    private Set<String> adultDishesCodeSet = new HashSet<>();
    private Set<String> childDishesCodeSet = new HashSet<>();
    private Set<String> babyDishesNameSet = new HashSet<>();
    private BuffetManager() {
        //E
        adultDishesCodeSet.add("801");
        adultDishesCodeSet.add("804");
        adultDishesCodeSet.add("807");
        adultDishesCodeSet.add("831");

        //K
        childDishesCodeSet.add("802");
        childDishesCodeSet.add("805");
        childDishesCodeSet.add("808");
        childDishesCodeSet.add("811");
        childDishesCodeSet.add("814");
        childDishesCodeSet.add("832");

        //B
        babyDishesNameSet.add("803");
        babyDishesNameSet.add("806");
        babyDishesNameSet.add("809");
        babyDishesNameSet.add("812");
        babyDishesNameSet.add("815");
    }

    public boolean isAdultDishes(String dishesCode) {
        return adultDishesCodeSet.contains(dishesCode);
    }

    public boolean isChildDishes(String dishesCode) {
        return childDishesCodeSet.contains(dishesCode);
    }

    public boolean isBabyDishes(String dishesCode) {
        return babyDishesNameSet.contains(dishesCode);
    }
}
