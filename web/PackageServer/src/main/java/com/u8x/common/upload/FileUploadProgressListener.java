package com.u8x.common.upload;

import org.apache.commons.fileupload.ProgressListener;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

/**
 * Created by ant on 2018/4/6.
 */
@Component
public class FileUploadProgressListener implements ProgressListener {

    private HttpSession session;

    @Override
    public void update(long bytesRead, long contentLength, int items) {
        //设置上传进度
        Progress progress = new Progress(bytesRead, contentLength, items);
        //将上传进度保存到session中
        session.setAttribute("progress", progress);
    }

    public void setSession(HttpSession session){
        this.session = session;
    }
}
