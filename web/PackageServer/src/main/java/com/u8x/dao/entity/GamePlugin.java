package com.u8x.dao.entity;

/**
 * 游戏级别通用插件配置
 * Created by ant on 2018/6/19.
 */
public class GamePlugin {

    private Integer id;         //唯一ID

    private Integer gameID;     //所属游戏ID

    private String sdkName;     //插件目录名称

    private String params;      //插件参数配置(key value的json格式)

    private PluginMaster master;        //所属master

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public String getSdkName() {
        return sdkName;
    }

    public void setSdkName(String sdkName) {
        this.sdkName = sdkName;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public PluginMaster getMaster() {
        return master;
    }

    public void setMaster(PluginMaster master) {
        this.master = master;
    }
}
