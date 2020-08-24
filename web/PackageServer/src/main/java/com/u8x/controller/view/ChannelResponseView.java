package com.u8x.controller.view;

import com.u8x.common.Consts;

/**
 * Created by ant on 2018/5/29.
 */
public class ChannelResponseView extends ResponseView{

    private int id;

    public static ChannelResponseView buildFail(String reason) {
        ChannelResponseView view = new ChannelResponseView();
        view.code = Consts.RespCode.FAILURE;
        view.reason = reason;

        return view;
    }

    public static ChannelResponseView buildSuccess(int id){
        ChannelResponseView view = new ChannelResponseView();
        view.id = id;
        view.success();
        return view;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
