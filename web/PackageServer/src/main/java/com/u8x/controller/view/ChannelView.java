package com.u8x.controller.view;

import com.u8x.common.Consts;
import com.u8x.dao.entity.*;

import java.util.List;

/**
 * Created by ant on 2018/6/10.
 */
public class ChannelView extends ResponseView{

    private Game game;              //所属游戏配置

    private Channel channel;        //渠道基本配置信息

    private List<ChannelParamMeta> metas;    //渠道参数配置项和值

    private List<ChannelProduct> products;    //商品映射关系条目

    private ChannelMaster master;            //渠道参数Master

//    private List<ChannelPlugin> plugins;     //渠道插件


    public static ChannelView buildFailure(String reason) {
        ChannelView view = new ChannelView();
        view.code = Consts.RespCode.FAILURE;
        view.reason = reason;

        return view;
    }

    public static ChannelView buildSuccess(Game game, Channel channel, List<ChannelParamMeta> metas, List<ChannelProduct> products, ChannelMaster master){
        ChannelView view = new ChannelView();
        view.game = game;
        view.channel = channel;
        view.metas = metas;
        view.products = products;
        view.master = master;
        view.success();
        return view;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public List<ChannelParamMeta> getMetas() {
        return metas;
    }

    public void setMetas(List<ChannelParamMeta> metas) {
        this.metas = metas;
    }

    public ChannelMaster getMaster() {
        return master;
    }

    public void setMaster(ChannelMaster master) {
        this.master = master;
    }

    public List<ChannelProduct> getProducts() {
        return products;
    }

    public void setProducts(List<ChannelProduct> products) {
        this.products = products;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

}
