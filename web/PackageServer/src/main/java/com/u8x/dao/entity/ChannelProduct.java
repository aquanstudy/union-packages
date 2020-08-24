package com.u8x.dao.entity;

/**
 * 渠道后台商品和游戏商品ID映射关系
 * Created by ant on 2018/5/15.
 */
public class ChannelProduct {

    private Integer id;                 //唯一ID

    private String gameProductID;       //游戏商品ID

    private String channelProductID;   //该游戏商品对应在渠道后台商品对应的ID

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGameProductID() {
        return gameProductID;
    }

    public void setGameProductID(String gameProductID) {
        this.gameProductID = gameProductID;
    }

    public String getChannelProductID() {
        return channelProductID;
    }

    public void setChannelProductID(String channelProductID) {
        this.channelProductID = channelProductID;
    }
}
