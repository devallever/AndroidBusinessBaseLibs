package com.hd.calculator.app.function.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 桌台
 * 防止和table关键字冲突加了个s[🤦‍♂️]
 */
@Entity(tableName = "tables")
public class TableEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    //tableId
    private long tableId;

    //code
    private int code;

    //name
    @Deprecated
    private String name;

    //zone
    private int zone;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getZone() {
        return zone;
    }

    public void setZone(int zone) {
        this.zone = zone;
    }
}
