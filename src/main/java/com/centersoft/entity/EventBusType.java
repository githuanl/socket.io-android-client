package com.centersoft.entity;

/**
 * Created by liudong on 2017/7/5.
 */

public class EventBusType<E> {

    private E e;

    private String tag;

    public EventBusType(String tag, E e) {
        this.e = e;
        this.tag = tag;
    }

    public E getE() {
        return e;
    }

    public void setE(E e) {
        this.e = e;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
