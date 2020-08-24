package com.u8x.dao.entity;

import java.io.Serializable;

/**
 * 签名信息
 * Created by ant on 2018/3/23.
 */
public class Keystore implements Serializable{


    private static final long serialVersionUID = -4890394111713246142L;


    private Integer id;             //唯一ID

    private Integer gameID;         //游戏ID

    private String name;            //签名名称

    private String filePath;        //签名文件路径

    private String password;        //签名密码

    private String aliasName;       //别名

    private String aliasPwd;        //别名密码


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getAliasPwd() {
        return aliasPwd;
    }

    public void setAliasPwd(String aliasPwd) {
        this.aliasPwd = aliasPwd;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }
}
