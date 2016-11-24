package org.achartengine.renderer;

import android.graphics.Typeface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DefaultRenderer implements Serializable {
    public static final int BACKGROUND_COLOR = -16777216;
    public static final int NO_COLOR = 0;
    private static final Typeface REGULAR_TEXT_FONT;
    public static final int TEXT_COLOR = -3355444;
    private boolean mAntialiasing;
    private boolean mApplyBackgroundColor;
    private int mAxesColor;
    private int mBackgroundColor;
    private String mChartTitle;
    private float mChartTitleTextSize;
    private boolean mClickEnabled;
    private boolean mDisplayValues;
    private boolean mExternalZoomEnabled;
    private boolean mFitLegend;
    private boolean mInScroll;
    private int mLabelsColor;
    private float mLabelsTextSize;
    private int mLegendHeight;
    private float mLegendTextSize;
    private int[] mMargins;
    private float mOriginalScale;
    private boolean mPanEnabled;
    private List<SimpleSeriesRenderer> mRenderers;
    private float mScale;
    private boolean mShowAxes;
    private boolean mShowCustomTextGrid;
    private boolean mShowGridX;
    private boolean mShowGridY;
    private boolean mShowLabels;
    private boolean mShowLegend;
    private float mStartAngle;
    private Typeface mTextTypeface;
    private String mTextTypefaceName;
    private int mTextTypefaceStyle;
    private boolean mZoomButtonsVisible;
    private boolean mZoomEnabled;
    private float mZoomRate;
    private int selectableBuffer;

    public DefaultRenderer() {
        this.mChartTitle = "";
        this.mChartTitleTextSize = 15.0f;
        this.mTextTypefaceName = REGULAR_TEXT_FONT.toString();
        this.mTextTypefaceStyle = NO_COLOR;
        this.mShowAxes = true;
        this.mAxesColor = TEXT_COLOR;
        this.mShowLabels = true;
        this.mLabelsColor = TEXT_COLOR;
        this.mLabelsTextSize = 10.0f;
        this.mShowLegend = true;
        this.mLegendTextSize = 12.0f;
        this.mFitLegend = false;
        this.mShowGridX = false;
        this.mShowGridY = false;
        this.mShowCustomTextGrid = false;
        this.mRenderers = new ArrayList();
        this.mAntialiasing = true;
        this.mLegendHeight = NO_COLOR;
        this.mMargins = new int[]{20, 30, 10, 20};
        this.mScale = 1.0f;
        this.mPanEnabled = true;
        this.mZoomEnabled = true;
        this.mZoomButtonsVisible = false;
        this.mZoomRate = 1.5f;
        this.mExternalZoomEnabled = false;
        this.mOriginalScale = this.mScale;
        this.mClickEnabled = false;
        this.selectableBuffer = 15;
        this.mStartAngle = 0.0f;
    }

    static {
        REGULAR_TEXT_FONT = Typeface.create(Typeface.SERIF, NO_COLOR);
    }

    public String getChartTitle() {
        return this.mChartTitle;
    }

    public void setChartTitle(String title) {
        this.mChartTitle = title;
    }

    public float getChartTitleTextSize() {
        return this.mChartTitleTextSize;
    }

    public void setChartTitleTextSize(float textSize) {
        this.mChartTitleTextSize = textSize;
    }

    public void addSeriesRenderer(SimpleSeriesRenderer renderer) {
        this.mRenderers.add(renderer);
    }

    public void addSeriesRenderer(int index, SimpleSeriesRenderer renderer) {
        this.mRenderers.add(index, renderer);
    }

    public void removeSeriesRenderer(SimpleSeriesRenderer renderer) {
        this.mRenderers.remove(renderer);
    }

    public void removeAllRenderers() {
        this.mRenderers.clear();
    }

    public SimpleSeriesRenderer getSeriesRendererAt(int index) {
        return (SimpleSeriesRenderer) this.mRenderers.get(index);
    }

    public int getSeriesRendererCount() {
        return this.mRenderers.size();
    }

    public SimpleSeriesRenderer[] getSeriesRenderers() {
        return (SimpleSeriesRenderer[]) this.mRenderers.toArray(new SimpleSeriesRenderer[NO_COLOR]);
    }

    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
    }

    public boolean isApplyBackgroundColor() {
        return this.mApplyBackgroundColor;
    }

    public void setApplyBackgroundColor(boolean apply) {
        this.mApplyBackgroundColor = apply;
    }

    public int getAxesColor() {
        return this.mAxesColor;
    }

    public void setAxesColor(int color) {
        this.mAxesColor = color;
    }

    public int getLabelsColor() {
        return this.mLabelsColor;
    }

    public void setLabelsColor(int color) {
        this.mLabelsColor = color;
    }

    public float getLabelsTextSize() {
        return this.mLabelsTextSize;
    }

    public void setLabelsTextSize(float textSize) {
        this.mLabelsTextSize = textSize;
    }

    public boolean isShowAxes() {
        return this.mShowAxes;
    }

    public void setShowAxes(boolean showAxes) {
        this.mShowAxes = showAxes;
    }

    public boolean isShowLabels() {
        return this.mShowLabels;
    }

    public void setShowLabels(boolean showLabels) {
        this.mShowLabels = showLabels;
    }

    public boolean isShowGridX() {
        return this.mShowGridX;
    }

    public boolean isShowGridY() {
        return this.mShowGridY;
    }

    public void setShowGridX(boolean showGrid) {
        this.mShowGridX = showGrid;
    }

    public void setShowGridY(boolean showGrid) {
        this.mShowGridY = showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        setShowGridX(showGrid);
        setShowGridY(showGrid);
    }

    public boolean isShowCustomTextGrid() {
        return this.mShowCustomTextGrid;
    }

    public void setShowCustomTextGrid(boolean showGrid) {
        this.mShowCustomTextGrid = showGrid;
    }

    public boolean isShowLegend() {
        return this.mShowLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.mShowLegend = showLegend;
    }

    public boolean isFitLegend() {
        return this.mFitLegend;
    }

    public void setFitLegend(boolean fit) {
        this.mFitLegend = fit;
    }

    public String getTextTypefaceName() {
        return this.mTextTypefaceName;
    }

    public int getTextTypefaceStyle() {
        return this.mTextTypefaceStyle;
    }

    public Typeface getTextTypeface() {
        return this.mTextTypeface;
    }

    public float getLegendTextSize() {
        return this.mLegendTextSize;
    }

    public void setLegendTextSize(float textSize) {
        this.mLegendTextSize = textSize;
    }

    public void setTextTypeface(String typefaceName, int style) {
        this.mTextTypefaceName = typefaceName;
        this.mTextTypefaceStyle = style;
    }

    public void setTextTypeface(Typeface typeface) {
        this.mTextTypeface = typeface;
    }

    public boolean isAntialiasing() {
        return this.mAntialiasing;
    }

    public void setAntialiasing(boolean antialiasing) {
        this.mAntialiasing = antialiasing;
    }

    public float getScale() {
        return this.mScale;
    }

    public float getOriginalScale() {
        return this.mOriginalScale;
    }

    public void setScale(float scale) {
        this.mScale = scale;
    }

    public boolean isZoomEnabled() {
        return this.mZoomEnabled;
    }

    public void setZoomEnabled(boolean enabled) {
        this.mZoomEnabled = enabled;
    }

    public boolean isZoomButtonsVisible() {
        return this.mZoomButtonsVisible;
    }

    public void setZoomButtonsVisible(boolean visible) {
        this.mZoomButtonsVisible = visible;
    }

    public boolean isExternalZoomEnabled() {
        return this.mExternalZoomEnabled;
    }

    public void setExternalZoomEnabled(boolean enabled) {
        this.mExternalZoomEnabled = enabled;
    }

    public float getZoomRate() {
        return this.mZoomRate;
    }

    public boolean isPanEnabled() {
        return this.mPanEnabled;
    }

    public void setPanEnabled(boolean enabled) {
        this.mPanEnabled = enabled;
    }

    public void setZoomRate(float rate) {
        this.mZoomRate = rate;
    }

    public boolean isClickEnabled() {
        return this.mClickEnabled;
    }

    public void setClickEnabled(boolean enabled) {
        this.mClickEnabled = enabled;
    }

    public int getSelectableBuffer() {
        return this.selectableBuffer;
    }

    public void setSelectableBuffer(int buffer) {
        this.selectableBuffer = buffer;
    }

    public int getLegendHeight() {
        return this.mLegendHeight;
    }

    public void setLegendHeight(int height) {
        this.mLegendHeight = height;
    }

    public int[] getMargins() {
        return this.mMargins;
    }

    public void setMargins(int[] margins) {
        this.mMargins = margins;
    }

    public boolean isInScroll() {
        return this.mInScroll;
    }

    public void setInScroll(boolean inScroll) {
        this.mInScroll = inScroll;
    }

    public float getStartAngle() {
        return this.mStartAngle;
    }

    public void setStartAngle(float startAngle) {
        this.mStartAngle = startAngle;
    }

    public boolean isDisplayValues() {
        return this.mDisplayValues;
    }

    public void setDisplayValues(boolean display) {
        this.mDisplayValues = display;
    }
}
