package com.u8x.dao.entity;

/**
 * 渠道级别插件配置
 *
 * 如果游戏级别有某插件， 渠道配置中无该插件配置，那么打包该渠道时，默认有该插件
 * 如果游戏级别有某插件，渠道配置中有该插件配置，那么打包时，使用该渠道中的参数配置
 * 如果游戏级别有某插件，渠道配置中有该插件配置中该插件状态是禁用，那么打包时不会打入该插件
 * Created by ant on 2018/6/19.
 */
public class ChannelPlugin {

    private Integer id;

    private Integer channelID;  //所属渠道

    private Integer gameID;     //所属游戏

    private String sdkName;     //插件目录名称

    private String params;      //插件参数配置(key value的json格式)

    private Integer state;      //插件状态(0：禁用;1:启用)

    private Integer extend;     //是否继承游戏中插件配置(0:直接使用游戏配置;1:使用渠道自己的配置)

    private PluginMaster master;        //所属master

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getExtend() {
        return extend;
    }

    public void setExtend(Integer extend) {
        this.extend = extend;
    }

    public PluginMaster getMaster() {
        return master;
    }

    public void setMaster(PluginMaster master) {
        this.master = master;
    }
}
