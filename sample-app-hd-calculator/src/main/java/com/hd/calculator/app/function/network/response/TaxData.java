package com.hd.calculator.app.function.network.response;

public class TaxData {
    private RateData drinksTakeoutTax;
    private RateData foodTakeoutTax;
    private RateData drinksInhausTax;
    private RateData foodInhausTax;

    public RateData getDrinksTakeoutTax() {
        return drinksTakeoutTax;
    }

    public void setDrinksTakeoutTax(RateData drinksTakeoutTax) {
        this.drinksTakeoutTax = drinksTakeoutTax;
    }

    public RateData getFoodTakeoutTax() {
        return foodTakeoutTax;
    }

    public void setFoodTakeoutTax(RateData foodTakeoutTax) {
        this.foodTakeoutTax = foodTakeoutTax;
    }

    public RateData getDrinksInhausTax() {
        return drinksInhausTax;
    }

    public void setDrinksInhausTax(RateData drinksInhausTax) {
        this.drinksInhausTax = drinksInhausTax;
    }

    public RateData getFoodInhausTax() {
        return foodInhausTax;
    }

    public void setFoodInhausTax(RateData foodInhausTax) {
        this.foodInhausTax = foodInhausTax;
    }
}