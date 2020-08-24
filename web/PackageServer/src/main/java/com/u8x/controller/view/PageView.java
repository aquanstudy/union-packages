package com.u8x.controller.view;

import java.util.List;

/**
 * Created by ant on 2017/7/30.
 */
public class PageView<T> extends ResponseView {

    private List<T> data;
    private int draw;
    private int recordsFiltered;
    private int recordsTotal;

    public PageView<T> fromList(List<T> lst, int draw, int recordsFiltered, int recordsTotal){
        this.data = lst;
        this.draw = draw;
        this.recordsFiltered = recordsFiltered;
        this.recordsTotal = recordsTotal;
        this.success();
        return this;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;
    }
}
