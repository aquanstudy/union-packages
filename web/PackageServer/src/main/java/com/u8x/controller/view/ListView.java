package com.u8x.controller.view;

import com.u8x.common.Consts;

import java.util.List;

/**
 * Created by ant on 2017/5/20.
 */
public class ListView<T> extends ResponseView{

    private List<T> data;

    public ListView<T> fromList(List<T> lst){
        this.data = lst;
        this.success();
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public static ListView buildFailure(String reason) {
        ListView view = new ListView();
        view.failure(Consts.RespCode.FAILURE, reason);
        return view;
    }
}
