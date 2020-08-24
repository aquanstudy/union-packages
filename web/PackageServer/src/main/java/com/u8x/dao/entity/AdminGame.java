package com.u8x.dao.entity;

import java.io.Serializable;

/**
 * 管理员账号和游戏对应关系
 * 该对象暂时没用
 * Created by Administrator on 2017/5/16.
 */
public class AdminGame implements Serializable{

    private static final long serialVersionUID = 8492858682939642696L;

    private Integer id;
    private Integer adminID;
    private String gameID;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAdminID() {
        return adminID;
    }

    public void setAdminID(Integer adminID) {
        this.adminID = adminID;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }
}
