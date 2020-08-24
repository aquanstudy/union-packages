package com.u8x.controller.view;

import com.u8x.common.Consts;

/**
 * Created by ant on 2018/5/22.
 */
public class CompletableResponse<T> {

    public int code;
    public String reason;

    public T data;

    public CompletableResponse failure(String reason) {
        return failure(Consts.RespCode.FAILURE, reason);
    }

    public CompletableResponse failure(int code, String reason) {
        this.code = code;
        this.reason = reason;

        return this;
    }

    public CompletableResponse success(T data){
        this.code = Consts.RespCode.SUCCESS;
        this.reason = Consts.Tips.SucMsg;
        this.data = data;
        return this;
    }



}
