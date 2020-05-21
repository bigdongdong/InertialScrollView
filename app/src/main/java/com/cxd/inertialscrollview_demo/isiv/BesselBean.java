package com.cxd.inertialscrollview_demo.isiv;

import android.graphics.PointF;
import android.support.annotation.NonNull;

public class BesselBean {
    private PointF vertexPf ;
    private PointF startPf ;
    private PointF endPf ;

    public BesselBean(PointF vertexPf, PointF startPf, PointF endPf) {
        this.vertexPf = vertexPf;
        this.startPf = startPf;
        this.endPf = endPf;
    }

    @NonNull
    @Override
    public String toString() {
        return vertexPf.toString() + startPf.toString() + endPf.toString();
    }

    public PointF getVertexPf() {
        return vertexPf;
    }

    public void setVertexPf(PointF vertexPf) {
        this.vertexPf = vertexPf;
    }

    public PointF getStartPf() {
        return startPf;
    }

    public void setStartPf(PointF startPf) {
        this.startPf = startPf;
    }

    public PointF getEndPf() {
        return endPf;
    }

    public void setEndPf(PointF endPf) {
        this.endPf = endPf;
    }
}
