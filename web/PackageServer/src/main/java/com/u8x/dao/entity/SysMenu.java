package com.u8x.dao.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 系统功能表
 * Created by ant on 2016/7/28.
 */

public class SysMenu implements Serializable{

    private static final long serialVersionUID = 9153413376571997243L;

    private Integer id;             //ID,唯一，主键
    private int parentID;       //所属父节点
    private String name;            //名称
    private String path;            //相对url
    private String createTime;        //创建时间
    private String iconClass;       //icon css name

    private List<SysMenu> children; //子功能


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<SysMenu> getChildren() {
        return children;
    }

    public void setChildren(List<SysMenu> children) {
        this.children = children;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }
}
