package com.u8x.controller.view;

import com.u8x.common.Consts;

/**
 * Created by ant on 2018/5/22.
 */
public class ChannelParamsView<T> extends ListView<T>{

    private String tip;         //参数配置tip

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public static ChannelParamsView buildFailure(String reason) {
        ChannelParamsView view = new ChannelParamsView();
        view.failure(Consts.RespCode.FAILURE, reason);
        return view;
    }
}
