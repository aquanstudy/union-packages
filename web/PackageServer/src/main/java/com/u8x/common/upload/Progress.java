package com.u8x.common.upload;

/**
 * Created by ant on 2018/4/6.
 */
public class Progress {

    private long bytesRead;

    private long contentLength;

    private long items;

    public Progress(long bytesRead, long contentLength, long items){
        this.bytesRead = bytesRead;
        this.contentLength = contentLength;
        this.items = items;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getItems() {
        return items;
    }

    public void setItems(long items) {
        this.items = items;
    }
}
