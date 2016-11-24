package org.achartengine.model;

import java.util.ArrayList;
import java.util.List;
import org.achartengine.util.MathHelper;

public class XYValueSeries extends XYSeries {
    private double mMaxValue;
    private double mMinValue;
    private List<Double> mValue;

    public XYValueSeries(String title) {
        super(title);
        this.mValue = new ArrayList();
        this.mMinValue = MathHelper.NULL_VALUE;
        this.mMaxValue = -1.7976931348623157E308d;
    }

    public synchronized void add(double x, double y, double value) {
        super.add(x, y);
        this.mValue.add(Double.valueOf(value));
        updateRange(value);
    }

    private void initRange() {
        this.mMinValue = MathHelper.NULL_VALUE;
        this.mMaxValue = MathHelper.NULL_VALUE;
        int length = getItemCount();
        for (int k = 0; k < length; k++) {
            updateRange(getValue(k));
        }
    }

    private void updateRange(double value) {
        this.mMinValue = Math.min(this.mMinValue, value);
        this.mMaxValue = Math.max(this.mMaxValue, value);
    }

    public synchronized void add(double x, double y) {
        add(x, y, 0.0d);
    }

    public synchronized void remove(int index) {
        super.remove(index);
        double removedValue = ((Double) this.mValue.remove(index)).doubleValue();
        if (removedValue == this.mMinValue || removedValue == this.mMaxValue) {
            initRange();
        }
    }

    public synchronized void clear() {
        super.clear();
        this.mValue.clear();
        initRange();
    }

    public synchronized double getValue(int index) {
        return ((Double) this.mValue.get(index)).doubleValue();
    }

    public double getMinValue() {
        return this.mMinValue;
    }

    public double getMaxValue() {
        return this.mMaxValue;
    }
}
