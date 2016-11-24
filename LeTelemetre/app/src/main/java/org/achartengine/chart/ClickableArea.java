package org.achartengine.chart;

import android.graphics.RectF;

public class ClickableArea {
    private RectF rect;
    private double f0x;
    private double f1y;

    public ClickableArea(RectF rect, double x, double y) {
        this.rect = rect;
        this.f0x = x;
        this.f1y = y;
    }

    public RectF getRect() {
        return this.rect;
    }

    public double getX() {
        return this.f0x;
    }

    public double getY() {
        return this.f1y;
    }
}
