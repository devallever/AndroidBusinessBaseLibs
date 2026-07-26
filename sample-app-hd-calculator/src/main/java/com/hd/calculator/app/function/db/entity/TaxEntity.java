package com.hd.calculator.app.function.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.hd.calculator.app.constant.TaxType;

@Entity(tableName = "tax")
public class TaxEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    //rate
    private float rate;
    //sign
    private String sign;
    //type //TaxType
    private int type;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
