package com.u8x.controller.view;

/**
 * Created by Administrator on 2017/5/14.
 */
public class ObjectView<T> extends ResponseView {

    private T object;

    public ObjectView(){

    }

    public ObjectView(T object) {
    	this.object = object;
    	this.success();
    }

    public ObjectView<T> fromObject(T object){
        this.object = object;
        this.success();
        return this;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
