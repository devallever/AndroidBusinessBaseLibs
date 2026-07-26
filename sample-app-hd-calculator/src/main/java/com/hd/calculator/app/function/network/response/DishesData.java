package com.hd.calculator.app.function.network.response;

public class DishesData {
    private long id;
    private String course;
    //自助餐（德语）
    private String buffetDe;
    private String buffetZh;
    //自助餐类型:
    private String buffetType;
    //堂食税率
    private float taxInhouse;
    //外卖税率
    private float taxTakeout;
    //打印地点
    private String printLocation;
    //菜品编号
    private String plu;
    //菜牌名称（德语）
    private String itemDe;
    private String itemZh;
    //描述
    private String description;
    //菜????
    private String extraItem;
    //1份的数量
    private float qtyPerUnit;
    //单位 应该是String吧
    private String unit;
    private float price;
    private boolean enablePrint;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getBuffetDe() {
        return buffetDe;
    }

    public void setBuffetDe(String buffetDe) {
        this.buffetDe = buffetDe;
    }

    public String getBuffetZh() {
        return buffetZh;
    }

    public void setBuffetZh(String buffetZh) {
        this.buffetZh = buffetZh;
    }

    public String getBuffetType() {
        return buffetType;
    }

    public void setBuffetType(String buffetType) {
        this.buffetType = buffetType;
    }

    public float getTaxInhouse() {
        return taxInhouse;
    }

    public void setTaxInhouse(float taxInhouse) {
        this.taxInhouse = taxInhouse;
    }

    public float getTaxTakeout() {
        return taxTakeout;
    }

    public void setTaxTakeout(float taxTakeout) {
        this.taxTakeout = taxTakeout;
    }

    public String getPrintLocation() {
        return printLocation;
    }

    public void setPrintLocation(String printLocation) {
        this.printLocation = printLocation;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public String getItemDe() {
        return itemDe;
    }

    public void setItemDe(String itemDe) {
        this.itemDe = itemDe;
    }

    public String getItemZh() {
        return itemZh;
    }

    public void setItemZh(String itemZh) {
        this.itemZh = itemZh;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtraItem() {
        return extraItem;
    }

    public void setExtraItem(String extraItem) {
        this.extraItem = extraItem;
    }

    public float getQtyPerUnit() {
        return qtyPerUnit;
    }

    public void setQtyPerUnit(float qtyPerUnit) {
        this.qtyPerUnit = qtyPerUnit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isEnablePrint() {
        return enablePrint;
    }

    public void setEnablePrint(boolean enablePrint) {
        this.enablePrint = enablePrint;
    }
}