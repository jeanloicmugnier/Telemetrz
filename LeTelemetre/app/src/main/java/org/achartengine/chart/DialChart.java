package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DialRenderer;
import org.achartengine.renderer.DialRenderer.Type;
import org.achartengine.util.MathHelper;

public class DialChart extends RoundChart {
    private static final int NEEDLE_RADIUS = 10;
    private DialRenderer mRenderer;

    public DialChart(CategorySeries dataset, DialRenderer renderer) {
        super(dataset, renderer);
        this.mRenderer = renderer;
    }

    public void draw(Canvas canvas, int x, int y, int width, int height, Paint paint) {
        int i;
        int count;
        paint.setAntiAlias(this.mRenderer.isAntialiasing());
        paint.setStyle(Style.FILL);
        paint.setTextSize(this.mRenderer.getLabelsTextSize());
        int legendSize = getLegendSize(this.mRenderer, height / 5, 0.0f);
        int left = x;
        int top = y;
        int right = x + width;
        int sLength = this.mDataset.getItemCount();
        String[] titles = new String[sLength];
        for (i = 0; i < sLength; i++) {
            titles[i] = this.mDataset.getCategory(i);
        }
        if (this.mRenderer.isFitLegend()) {
            legendSize = drawLegend(canvas, this.mRenderer, titles, left, right, y, width, height, legendSize, paint, true);
        }
        int bottom = (y + height) - legendSize;
        drawBackground(this.mRenderer, canvas, x, y, width, height, paint, false, 0);
        int radius = (int) ((((double) Math.min(Math.abs(right - left), Math.abs(bottom - top))) * 0.35d) * ((double) this.mRenderer.getScale()));
        if (this.mCenterX == Integer.MAX_VALUE) {
            this.mCenterX = (left + right) / 2;
        }
        if (this.mCenterY == Integer.MAX_VALUE) {
            this.mCenterY = (bottom + top) / 2;
        }
        float shortRadius = ((float) radius) * 0.9f;
        float longRadius = ((float) radius) * 1.1f;
        double min = this.mRenderer.getMinValue();
        double max = this.mRenderer.getMaxValue();
        double angleMin = this.mRenderer.getAngleMin();
        double angleMax = this.mRenderer.getAngleMax();
        if (!(this.mRenderer.isMinValueSet() && this.mRenderer.isMaxValueSet())) {
            count = this.mRenderer.getSeriesRendererCount();
            for (i = 0; i < count; i++) {
                double value = this.mDataset.getValue(i);
                if (!this.mRenderer.isMinValueSet()) {
                    min = Math.min(min, value);
                }
                if (!this.mRenderer.isMaxValueSet()) {
                    max = Math.max(max, value);
                }
            }
        }
        if (min == max) {
            min *= 0.5d;
            max *= 1.5d;
        }
        paint.setColor(this.mRenderer.getLabelsColor());
        double minorTicks = this.mRenderer.getMinorTicksSpacing();
        double majorTicks = this.mRenderer.getMajorTicksSpacing();
        if (minorTicks == MathHelper.NULL_VALUE) {
            minorTicks = (max - min) / 30.0d;
        }
        if (majorTicks == MathHelper.NULL_VALUE) {
            majorTicks = (max - min) / 10.0d;
        }
        drawTicks(canvas, min, max, angleMin, angleMax, this.mCenterX, this.mCenterY, (double) longRadius, (double) radius, minorTicks, paint, false);
        drawTicks(canvas, min, max, angleMin, angleMax, this.mCenterX, this.mCenterY, (double) longRadius, (double) shortRadius, majorTicks, paint, true);
        count = this.mRenderer.getSeriesRendererCount();
        for (i = 0; i < count; i++) {
            double angle = getAngleForValue(this.mDataset.getValue(i), angleMin, angleMax, min, max);
            paint.setColor(this.mRenderer.getSeriesRendererAt(i).getColor());
            drawNeedle(canvas, angle, this.mCenterX, this.mCenterY, (double) shortRadius, this.mRenderer.getVisualTypeForIndex(i) == Type.ARROW, paint);
        }
        drawLegend(canvas, this.mRenderer, titles, left, right, y, width, height, legendSize, paint, false);
        drawTitle(canvas, x, y, width, paint);
    }

    private double getAngleForValue(double value, double minAngle, double maxAngle, double min, double max) {
        return Math.toRadians((((value - min) * (maxAngle - minAngle)) / (max - min)) + minAngle);
    }

    private void drawTicks(Canvas canvas, double min, double max, double minAngle, double maxAngle, int centerX, int centerY, double longRadius, double shortRadius, double ticks, Paint paint, boolean labels) {
        double i = min;
        while (i <= max) {
            double angle = getAngleForValue(i, minAngle, maxAngle, min, max);
            double sinValue = Math.sin(angle);
            double cosValue = Math.cos(angle);
            int x1 = Math.round(((float) centerX) + ((float) (shortRadius * sinValue)));
            int y1 = Math.round(((float) centerY) + ((float) (shortRadius * cosValue)));
            int x2 = Math.round(((float) centerX) + ((float) (longRadius * sinValue)));
            Canvas canvas2 = canvas;
            canvas2.drawLine((float) x1, (float) y1, (float) x2, (float) Math.round(((float) centerY) + ((float) (longRadius * cosValue))), paint);
            if (labels) {
                paint.setTextAlign(Align.LEFT);
                if (x1 <= x2) {
                    paint.setTextAlign(Align.RIGHT);
                }
                String text = i + "";
                if (Math.round(i) == ((long) i)) {
                    text = ((long) i) + "";
                }
                canvas.drawText(text, (float) x1, (float) y1, paint);
            }
            i += ticks;
        }
    }

    private void drawNeedle(Canvas canvas, double angle, int centerX, int centerY, double radius, boolean arrow, Paint paint) {
        float[] points;
        double diff = Math.toRadians(90.0d);
        int needleSinValue = (int) (10.0d * Math.sin(angle - diff));
        int needleCosValue = (int) (10.0d * Math.cos(angle - diff));
        int needleCenterX = centerX + ((int) (Math.sin(angle) * radius));
        int needleCenterY = centerY + ((int) (Math.cos(angle) * radius));
        if (arrow) {
            int arrowBaseX = centerX + ((int) ((0.85d * radius) * Math.sin(angle)));
            int arrowBaseY = centerY + ((int) ((0.85d * radius) * Math.cos(angle)));
            points = new float[]{(float) (arrowBaseX - needleSinValue), (float) (arrowBaseY - needleCosValue), (float) needleCenterX, (float) needleCenterY, (float) (arrowBaseX + needleSinValue), (float) (arrowBaseY + needleCosValue)};
            float width = paint.getStrokeWidth();
            paint.setStrokeWidth(5.0f);
            canvas.drawLine((float) centerX, (float) centerY, (float) needleCenterX, (float) needleCenterY, paint);
            paint.setStrokeWidth(width);
        } else {
            points = new float[]{(float) (centerX - needleSinValue), (float) (centerY - needleCosValue), (float) needleCenterX, (float) needleCenterY, (float) (centerX + needleSinValue), (float) (centerY + needleCosValue)};
        }
        drawPath(canvas, points, paint, true);
    }
}
