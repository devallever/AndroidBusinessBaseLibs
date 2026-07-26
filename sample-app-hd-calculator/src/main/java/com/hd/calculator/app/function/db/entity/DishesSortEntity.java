package com.hd.calculator.app.function.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dishes_sort")
public class DishesSortEntity {
    //id
    @PrimaryKey(autoGenerate = true)
    private long id;
    //服务端sortId
    private long sortId;
    private long parentSortId;
    //name
    private String name;
    //level
    private int level;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getSortId() {
        return sortId;
    }

    public void setSortId(long sortId) {
        this.sortId = sortId;
    }

    public long getParentSortId() {
        return parentSortId;
    }

    public void setParentSortId(long parentSortId) {
        this.parentSortId = parentSortId;
    }
}
