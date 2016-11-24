package org.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.media.TransportMediator;
import java.util.List;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.Zoom;

public class ScatterChart extends XYChart {
    private static final int SHAPE_WIDTH = 10;
    private static final float SIZE = 3.0f;
    public static final String TYPE = "Scatter";
    private float size;

    /* renamed from: org.achartengine.chart.ScatterChart.1 */
    static /* synthetic */ class C01561 {
        static final /* synthetic */ int[] $SwitchMap$org$achartengine$chart$PointStyle;

        static {
            $SwitchMap$org$achartengine$chart$PointStyle = new int[PointStyle.values().length];
            try {
                $SwitchMap$org$achartengine$chart$PointStyle[PointStyle.X.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$org$achartengine$chart$PointStyle[PointStyle.CIRCLE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$org$achartengine$chart$PointStyle[PointStyle.TRIANGLE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$org$achartengine$chart$PointStyle[PointStyle.SQUARE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$org$achartengine$chart$PointStyle[PointStyle.DIAMOND.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$org$achartengine$chart$PointStyle[PointStyle.POINT.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    ScatterChart() {
        this.size = SIZE;
    }

    public ScatterChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        super(dataset, renderer);
        this.size = SIZE;
        this.size = renderer.getPointSize();
    }

    protected void setDatasetRenderer(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
        super.setDatasetRenderer(dataset, renderer);
        this.size = renderer.getPointSize();
    }

    public void drawSeries(Canvas canvas, Paint paint, List<Float> points, SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
        XYSeriesRenderer renderer = (XYSeriesRenderer) seriesRenderer;
        paint.setColor(renderer.getColor());
        float stroke = paint.getStrokeWidth();
        if (renderer.isFillPoints()) {
            paint.setStyle(Style.FILL);
        } else {
            paint.setStrokeWidth(renderer.getPointStrokeWidth());
            paint.setStyle(Style.STROKE);
        }
        int length = points.size();
        int i;
        float[] path;
        switch (C01561.$SwitchMap$org$achartengine$chart$PointStyle[renderer.getPointStyle().ordinal()]) {
            case Zoom.ZOOM_AXIS_X /*1*/:
                paint.setStrokeWidth(renderer.getPointStrokeWidth());
                for (i = 0; i < length; i += 2) {
                    drawX(canvas, paint, ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue());
                }
                break;
            case Zoom.ZOOM_AXIS_Y /*2*/:
                for (i = 0; i < length; i += 2) {
                    drawCircle(canvas, paint, ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue());
                }
                break;
            case BleProfileService.STATE_DISCONNECTING /*3*/:
                path = new float[6];
                for (i = 0; i < length; i += 2) {
                    drawTriangle(canvas, paint, path, ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue());
                }
                break;
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                for (i = 0; i < length; i += 2) {
                    drawSquare(canvas, paint, ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue());
                }
                break;
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                path = new float[8];
                for (i = 0; i < length; i += 2) {
                    drawDiamond(canvas, paint, path, ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue());
                }
                break;
            //case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
              //  for (i = 0; i < length; i += 2) {
                //    canvas.drawPoint(((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue(), paint);
                //}
                //break;
        }
        paint.setStrokeWidth(stroke);
    }

    protected ClickableArea[] clickableAreasForPoints(List<Float> points, List<Double> values, float yAxisValue, int seriesIndex, int startIndex) {
        int length = points.size();
        ClickableArea[] ret = new ClickableArea[(length / 2)];
        for (int i = 0; i < length; i += 2) {
            int selectableBuffer = this.mRenderer.getSelectableBuffer();
            ret[i / 2] = new ClickableArea(new RectF(((Float) points.get(i)).floatValue() - ((float) selectableBuffer), ((Float) points.get(i + 1)).floatValue() - ((float) selectableBuffer), ((float) selectableBuffer) + ((Float) points.get(i)).floatValue(), ((Float) points.get(i + 1)).floatValue() + ((float) selectableBuffer)), ((Double) values.get(i)).doubleValue(), ((Double) values.get(i + 1)).doubleValue());
        }
        return ret;
    }

    public int getLegendShapeWidth(int seriesIndex) {
        return SHAPE_WIDTH;
    }

    public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y, int seriesIndex, Paint paint) {
        if (((XYSeriesRenderer) renderer).isFillPoints()) {
            paint.setStyle(Style.FILL);
        } else {
            paint.setStyle(Style.STROKE);
        }
        switch (C01561.$SwitchMap$org$achartengine$chart$PointStyle[((XYSeriesRenderer) renderer).getPointStyle().ordinal()]) {
            case Zoom.ZOOM_AXIS_X /*1*/:
                drawX(canvas, paint, x + 10.0f, y);
            case Zoom.ZOOM_AXIS_Y /*2*/:
                drawCircle(canvas, paint, x + 10.0f, y);
            case BleProfileService.STATE_DISCONNECTING /*3*/:
                drawTriangle(canvas, paint, new float[6], x + 10.0f, y);
            case TransportMediator.FLAG_KEY_MEDIA_PLAY /*4*/:
                drawSquare(canvas, paint, x + 10.0f, y);
            case WearableExtender.SIZE_FULL_SCREEN /*5*/:
                drawDiamond(canvas, paint, new float[8], x + 10.0f, y);
            //case FragmentManagerImpl.ANIM_STYLE_FADE_EXIT /*6*/:
            //    canvas.drawPoint(x + 10.0f, y, paint);
            default:
        }
    }

    private void drawX(Canvas canvas, Paint paint, float x, float y) {
        canvas.drawLine(x - this.size, y - this.size, x + this.size, y + this.size, paint);
        canvas.drawLine(x + this.size, y - this.size, x - this.size, y + this.size, paint);
    }

    private void drawCircle(Canvas canvas, Paint paint, float x, float y) {
        canvas.drawCircle(x, y, this.size, paint);
    }

    private void drawTriangle(Canvas canvas, Paint paint, float[] path, float x, float y) {
        path[0] = x;
        path[1] = (y - this.size) - (this.size / 2.0f);
        path[2] = x - this.size;
        path[3] = this.size + y;
        path[4] = this.size + x;
        path[5] = path[3];
        drawPath(canvas, path, paint, true);
    }

    private void drawSquare(Canvas canvas, Paint paint, float x, float y) {
        canvas.drawRect(x - this.size, y - this.size, x + this.size, y + this.size, paint);
    }

    private void drawDiamond(Canvas canvas, Paint paint, float[] path, float x, float y) {
        path[0] = x;
        path[1] = y - this.size;
        path[2] = x - this.size;
        path[3] = y;
        path[4] = x;
        path[5] = this.size + y;
        path[6] = this.size + x;
        path[7] = y;
        drawPath(canvas, path, paint, true);
    }

    public String getChartType() {
        return TYPE;
    }
}
