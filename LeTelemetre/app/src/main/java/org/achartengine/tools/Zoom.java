package org.achartengine.tools;

import java.util.ArrayList;
import java.util.List;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.RoundChart;
import org.achartengine.chart.XYChart;
import org.achartengine.renderer.DefaultRenderer;

public class Zoom extends AbstractTool {
    public static final int ZOOM_AXIS_X = 1;
    public static final int ZOOM_AXIS_XY = 0;
    public static final int ZOOM_AXIS_Y = 2;
    private boolean limitsReachedX;
    private boolean limitsReachedY;
    private boolean mZoomIn;
    private List<ZoomListener> mZoomListeners;
    private float mZoomRate;

    public Zoom(AbstractChart chart, boolean in, float rate) {
        super(chart);
        this.mZoomListeners = new ArrayList();
        this.limitsReachedX = false;
        this.limitsReachedY = false;
        this.mZoomIn = in;
        setZoomRate(rate);
    }

    public void setZoomRate(float rate) {
        this.mZoomRate = rate;
    }

    public void apply(int zoom_axis) {
        if (this.mChart instanceof XYChart) {
            int scales = this.mRenderer.getScalesCount();
            for (int i = ZOOM_AXIS_XY; i < scales; i += ZOOM_AXIS_X) {
                double minX;
                double minY;
                double[] range = getRange(i);
                checkRange(range, i);
                double[] limits = this.mRenderer.getZoomLimits();
                double centerX = (range[ZOOM_AXIS_XY] + range[ZOOM_AXIS_X]) / 2.0d;
                double centerY = (range[ZOOM_AXIS_Y] + range[3]) / 2.0d;
                double newWidth = range[ZOOM_AXIS_X] - range[ZOOM_AXIS_XY];
                double newHeight = range[3] - range[ZOOM_AXIS_Y];
                double newXMin = centerX - (newWidth / 2.0d);
                double newXMax = centerX + (newWidth / 2.0d);
                double newYMin = centerY - (newHeight / 2.0d);
                double newYMax = centerY + (newHeight / 2.0d);
                if (i == 0) {
                    boolean z = limits != null && (newXMin <= limits[ZOOM_AXIS_XY] || newXMax >= limits[ZOOM_AXIS_X]);
                    this.limitsReachedX = z;
                    z = limits != null && (newYMin <= limits[ZOOM_AXIS_Y] || newYMax >= limits[3]);
                    this.limitsReachedY = z;
                }
                if (this.mZoomIn) {
                    if (this.mRenderer.isZoomXEnabled() && ((zoom_axis == ZOOM_AXIS_X || zoom_axis == 0) && (!this.limitsReachedX || this.mZoomRate >= 1.0f))) {
                        newWidth /= (double) this.mZoomRate;
                    }
                    if (this.mRenderer.isZoomYEnabled() && ((zoom_axis == ZOOM_AXIS_Y || zoom_axis == 0) && (!this.limitsReachedY || this.mZoomRate >= 1.0f))) {
                        newHeight /= (double) this.mZoomRate;
                    }
                } else {
                    if (this.mRenderer.isZoomXEnabled() && !this.limitsReachedX && (zoom_axis == ZOOM_AXIS_X || zoom_axis == 0)) {
                        newWidth *= (double) this.mZoomRate;
                    }
                    if (this.mRenderer.isZoomYEnabled() && !this.limitsReachedY && (zoom_axis == ZOOM_AXIS_Y || zoom_axis == 0)) {
                        newHeight *= (double) this.mZoomRate;
                    }
                }
                if (limits != null) {
                    minX = Math.min(this.mRenderer.getZoomInLimitX(), limits[ZOOM_AXIS_X] - limits[ZOOM_AXIS_XY]);
                    minY = Math.min(this.mRenderer.getZoomInLimitY(), limits[3] - limits[ZOOM_AXIS_Y]);
                } else {
                    minX = this.mRenderer.getZoomInLimitX();
                    minY = this.mRenderer.getZoomInLimitY();
                }
                newWidth = Math.max(newWidth, minX);
                newHeight = Math.max(newHeight, minY);
                if (this.mRenderer.isZoomXEnabled() && (zoom_axis == ZOOM_AXIS_X || zoom_axis == 0)) {
                    setXRange(centerX - (newWidth / 2.0d), centerX + (newWidth / 2.0d), i);
                }
                if (this.mRenderer.isZoomYEnabled() && (zoom_axis == ZOOM_AXIS_Y || zoom_axis == 0)) {
                    setYRange(centerY - (newHeight / 2.0d), centerY + (newHeight / 2.0d), i);
                }
            }
        } else {
            DefaultRenderer renderer = ((RoundChart) this.mChart).getRenderer();
            if (this.mZoomIn) {
                renderer.setScale(renderer.getScale() * this.mZoomRate);
            } else {
                renderer.setScale(renderer.getScale() / this.mZoomRate);
            }
        }
        notifyZoomListeners(new ZoomEvent(this.mZoomIn, this.mZoomRate));
    }

    private synchronized void notifyZoomListeners(ZoomEvent e) {
        for (ZoomListener listener : this.mZoomListeners) {
            listener.zoomApplied(e);
        }
    }

    public synchronized void notifyZoomResetListeners() {
        for (ZoomListener listener : this.mZoomListeners) {
            listener.zoomReset();
        }
    }

    public synchronized void addZoomListener(ZoomListener listener) {
        this.mZoomListeners.add(listener);
    }

    public synchronized void removeZoomListener(ZoomListener listener) {
        this.mZoomListeners.remove(listener);
    }
}
