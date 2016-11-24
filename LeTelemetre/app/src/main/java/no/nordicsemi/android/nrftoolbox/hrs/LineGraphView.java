package no.nordicsemi.android.nrftoolbox.hrs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Point;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

public class LineGraphView {
    private static LineGraphView mInstance;
    private XYMultipleSeriesDataset mDataset;
    private XYMultipleSeriesRenderer mMultiRenderer;
    private XYSeriesRenderer mRenderer;
    private TimeSeries mSeries;

    static {
        mInstance = null;
    }

    public static synchronized LineGraphView getLineGraphView() {
        LineGraphView lineGraphView;
        synchronized (LineGraphView.class) {
            if (mInstance == null) {
                mInstance = new LineGraphView();
            }
            lineGraphView = mInstance;
        }
        return lineGraphView;
    }

    public LineGraphView() {
        this.mSeries = new TimeSeries("Heart Rate");
        this.mRenderer = new XYSeriesRenderer();
        this.mDataset = new XYMultipleSeriesDataset();
        this.mMultiRenderer = new XYMultipleSeriesRenderer();
        this.mDataset.addSeries(this.mSeries);
        this.mRenderer.setColor(DefaultRenderer.BACKGROUND_COLOR);
        this.mRenderer.setPointStyle(PointStyle.SQUARE);
        this.mRenderer.setFillPoints(true);
        XYMultipleSeriesRenderer renderer = this.mMultiRenderer;
        renderer.setBackgroundColor(0);
        renderer.setMargins(new int[]{50, 65, 40, 5});
        renderer.setMarginsColor(Color.argb(0, 1, 1, 1));
        renderer.setAxesColor(DefaultRenderer.BACKGROUND_COLOR);
        renderer.setAxisTitleTextSize(24.0f);
        renderer.setShowGrid(true);
        renderer.setGridColor(DefaultRenderer.TEXT_COLOR);
        renderer.setLabelsColor(DefaultRenderer.BACKGROUND_COLOR);
        renderer.setYLabelsColor(0, -12303292);
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setYLabelsPadding(4.0f);
        renderer.setXLabelsColor(-12303292);
        renderer.setLabelsTextSize(20.0f);
        renderer.setLegendTextSize(20.0f);
        renderer.setPanEnabled(false, false);
        renderer.setZoomEnabled(false, false);
        renderer.setXTitle("    Time (seconds)");
        renderer.setYTitle("               BPM");
        renderer.addSeriesRenderer(this.mRenderer);
    }

    public GraphicalView getView(Context context) {
        return ChartFactory.getLineChartView(context, this.mDataset, this.mMultiRenderer);
    }

    public void addValue(Point p) {
        this.mSeries.add((double) p.x, (double) p.y);
    }

    public void clearGraph() {
        this.mSeries.clear();
    }
}
