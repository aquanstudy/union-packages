package com.u8x.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

public class AdminRole implements Serializable{

    private static final long serialVersionUID = 7047120944774161565L;

    private Integer id;             //ID,唯一，主键
    private String roleName;            //权限角色名称
    private String roleDesc;            //权限角色描述
    private String permission;      //权限

    private String createTime;        //创建时间
    private Integer creatorID;        //创建人(管理员)
    private Integer topRole;        //是否为最高权限(如果为1，则所有功能，所有游戏都可以管理)

    public boolean hasPermission(int id){

        if(permission == null || permission.trim().length() == 0) return false;

        String[] ps = permission.split(",");
        for(String p : ps){
            if(p.trim().length() > 0 && Integer.valueOf(p) == id){
                return true;
            }
        }

        return false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public Integer getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(Integer creatorID) {
        this.creatorID = creatorID;
    }

    public Integer getTopRole() {
        return topRole;
    }

    public void setTopRole(Integer topRole) {
        this.topRole = topRole;
    }
}
