package org.achartengine.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.achartengine.util.MathHelper;

public class DialRenderer extends DefaultRenderer {
    private double mAngleMax;
    private double mAngleMin;
    private double mMajorTickSpacing;
    private double mMaxValue;
    private double mMinValue;
    private double mMinorTickSpacing;
    private List<Type> mVisualTypes;

    public enum Type {
        NEEDLE,
        ARROW
    }

    public DialRenderer() {
        this.mAngleMin = 330.0d;
        this.mAngleMax = 30.0d;
        this.mMinValue = MathHelper.NULL_VALUE;
        this.mMaxValue = -1.7976931348623157E308d;
        this.mMinorTickSpacing = MathHelper.NULL_VALUE;
        this.mMajorTickSpacing = MathHelper.NULL_VALUE;
        this.mVisualTypes = new ArrayList();
    }

    public double getAngleMin() {
        return this.mAngleMin;
    }

    public void setAngleMin(double min) {
        this.mAngleMin = min;
    }

    public double getAngleMax() {
        return this.mAngleMax;
    }

    public void setAngleMax(double max) {
        this.mAngleMax = max;
    }

    public double getMinValue() {
        return this.mMinValue;
    }

    public void setMinValue(double min) {
        this.mMinValue = min;
    }

    public boolean isMinValueSet() {
        return this.mMinValue != MathHelper.NULL_VALUE;
    }

    public double getMaxValue() {
        return this.mMaxValue;
    }

    public void setMaxValue(double max) {
        this.mMaxValue = max;
    }

    public boolean isMaxValueSet() {
        return this.mMaxValue != -1.7976931348623157E308d;
    }

    public double getMinorTicksSpacing() {
        return this.mMinorTickSpacing;
    }

    public void setMinorTicksSpacing(double spacing) {
        this.mMinorTickSpacing = spacing;
    }

    public double getMajorTicksSpacing() {
        return this.mMajorTickSpacing;
    }

    public void setMajorTicksSpacing(double spacing) {
        this.mMajorTickSpacing = spacing;
    }

    public Type getVisualTypeForIndex(int index) {
        if (index < this.mVisualTypes.size()) {
            return (Type) this.mVisualTypes.get(index);
        }
        return Type.NEEDLE;
    }

    public void setVisualTypes(Type[] types) {
        this.mVisualTypes.clear();
        this.mVisualTypes.addAll(Arrays.asList(types));
    }
}
