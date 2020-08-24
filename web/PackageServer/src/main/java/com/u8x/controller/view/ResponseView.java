package com.u8x.controller.view;

import com.u8x.common.Consts;

/**
 * Created by guofeng.qin on 2017/2/20.
 */
public class ResponseView {
    public int code;
    public String reason;

    public ResponseView failure(String reason) {
        return failure(Consts.RespCode.FAILURE, reason);
    }

    public ResponseView failure(int code, String reason) {
        this.code = code;
        this.reason = reason;

        return this;
    }

    public ResponseView success(){
        this.code = Consts.RespCode.SUCCESS;
        this.reason = Consts.Tips.SucMsg;
        return this;
    }
}
