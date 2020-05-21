package com.cxd.inertialscrollview_demo.isiv;

import android.graphics.Point;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import java.io.Serializable;

public class ItemBean implements Serializable {
    private Object img ;
    private String text ;
    private int[] colors ;
    private float angle ;
    private BesselBean bgBessel;  //扇形背景
    private BesselBean textBessel; //文字

    public ItemBean(Object img, String text, int[] colors, float angle) {
        this.img = img;
        this.text = text;
        this.colors = colors;
        this.angle = angle;
    }

    public void setBgBessel(BesselBean bgBessel) {
        this.bgBessel = bgBessel;
    }

    public void setTextBessel(BesselBean textBessel) {
        this.textBessel = textBessel;
    }

    public Object getImg() {
        return img;
    }

    public String getText() {
        return text;
    }

    public int[] getColors() {
        return colors;
    }

    public float getAngle() {
        return angle;
    }

    public BesselBean getBgBessel() {
        return bgBessel;
    }

    public BesselBean getTextBessel() {
        return textBessel;
    }
}