package org.achartengine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import org.achartengine.util.IndexXYMap;
import org.achartengine.util.MathHelper;
import org.achartengine.util.XYEntry;

public class XYSeries implements Serializable {
    private static final double PADDING = 1.0E-12d;
    private List<String> mAnnotations;
    private double mMaxX;
    private double mMaxY;
    private double mMinX;
    private double mMinY;
    private final int mScaleNumber;
    private final IndexXYMap<Double, Double> mStringXY;
    private String mTitle;
    private final IndexXYMap<Double, Double> mXY;

    public XYSeries(String title) {
        this(title, 0);
    }

    public XYSeries(String title, int scaleNumber) {
        this.mXY = new IndexXYMap();
        this.mMinX = MathHelper.NULL_VALUE;
        this.mMaxX = -1.7976931348623157E308d;
        this.mMinY = MathHelper.NULL_VALUE;
        this.mMaxY = -1.7976931348623157E308d;
        this.mAnnotations = new ArrayList();
        this.mStringXY = new IndexXYMap();
        this.mTitle = title;
        this.mScaleNumber = scaleNumber;
        initRange();
    }

    public int getScaleNumber() {
        return this.mScaleNumber;
    }

    private void initRange() {
        this.mMinX = MathHelper.NULL_VALUE;
        this.mMaxX = -1.7976931348623157E308d;
        this.mMinY = MathHelper.NULL_VALUE;
        this.mMaxY = -1.7976931348623157E308d;
        int length = getItemCount();
        for (int k = 0; k < length; k++) {
            updateRange(getX(k), getY(k));
        }
    }

    private void updateRange(double x, double y) {
        this.mMinX = Math.min(this.mMinX, x);
        this.mMaxX = Math.max(this.mMaxX, x);
        this.mMinY = Math.min(this.mMinY, y);
        this.mMaxY = Math.max(this.mMaxY, y);
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void add(double r4, double r6) {
        /*
        r3 = this;
        monitor-enter(r3);
    L_0x0001:
        r0 = r3.mXY;	 Catch:{ all -> 0x0025 }
        r1 = java.lang.Double.valueOf(r4);	 Catch:{ all -> 0x0025 }
        r0 = r0.get(r1);	 Catch:{ all -> 0x0025 }
        if (r0 == 0) goto L_0x0013;
    L_0x000d:
        r0 = r3.getPadding();	 Catch:{ all -> 0x0025 }
        r4 = r4 + r0;
        goto L_0x0001;
    L_0x0013:
        r0 = r3.mXY;	 Catch:{ all -> 0x0025 }
        r1 = java.lang.Double.valueOf(r4);	 Catch:{ all -> 0x0025 }
        r2 = java.lang.Double.valueOf(r6);	 Catch:{ all -> 0x0025 }
        r0.put(r1, r2);	 Catch:{ all -> 0x0025 }
        r3.updateRange(r4, r6);	 Catch:{ all -> 0x0025 }
        monitor-exit(r3);
        return;
    L_0x0025:
        r0 = move-exception;
        monitor-exit(r3);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.achartengine.model.XYSeries.add(double, double):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void add(int r4, double r5, double r7) {
        /*
        r3 = this;
        monitor-enter(r3);
    L_0x0001:
        r0 = r3.mXY;	 Catch:{ all -> 0x0025 }
        r1 = java.lang.Double.valueOf(r5);	 Catch:{ all -> 0x0025 }
        r0 = r0.get(r1);	 Catch:{ all -> 0x0025 }
        if (r0 == 0) goto L_0x0013;
    L_0x000d:
        r0 = r3.getPadding();	 Catch:{ all -> 0x0025 }
        r5 = r5 + r0;
        goto L_0x0001;
    L_0x0013:
        r0 = r3.mXY;	 Catch:{ all -> 0x0025 }
        r1 = java.lang.Double.valueOf(r5);	 Catch:{ all -> 0x0025 }
        r2 = java.lang.Double.valueOf(r7);	 Catch:{ all -> 0x0025 }
        r0.put(r4, r1, r2);	 Catch:{ all -> 0x0025 }
        r3.updateRange(r5, r7);	 Catch:{ all -> 0x0025 }
        monitor-exit(r3);
        return;
    L_0x0025:
        r0 = move-exception;
        monitor-exit(r3);
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.achartengine.model.XYSeries.add(int, double, double):void");
    }

    protected double getPadding() {
        return PADDING;
    }

    public synchronized void remove(int index) {
        XYEntry<Double, Double> removedEntry = this.mXY.removeByIndex(index);
        double removedX = ((Double) removedEntry.getKey()).doubleValue();
        double removedY = ((Double) removedEntry.getValue()).doubleValue();
        if (removedX == this.mMinX || removedX == this.mMaxX || removedY == this.mMinY || removedY == this.mMaxY) {
            initRange();
        }
    }

    public synchronized void clear() {
        this.mXY.clear();
        this.mStringXY.clear();
        initRange();
    }

    public synchronized double getX(int index) {
        return ((Double) this.mXY.getXByIndex(index)).doubleValue();
    }

    public synchronized double getY(int index) {
        return ((Double) this.mXY.getYByIndex(index)).doubleValue();
    }

    public void addAnnotation(String annotation, double x, double y) {
        this.mAnnotations.add(annotation);
        this.mStringXY.put(Double.valueOf(x), Double.valueOf(y));
    }

    public void removeAnnotation(int index) {
        this.mAnnotations.remove(index);
        this.mStringXY.removeByIndex(index);
    }

    public double getAnnotationX(int index) {
        return ((Double) this.mStringXY.getXByIndex(index)).doubleValue();
    }

    public double getAnnotationY(int index) {
        return ((Double) this.mStringXY.getYByIndex(index)).doubleValue();
    }

    public int getAnnotationCount() {
        return this.mAnnotations.size();
    }

    public String getAnnotationAt(int index) {
        return (String) this.mAnnotations.get(index);
    }

    public synchronized SortedMap<Double, Double> getRange(double start, double stop, boolean beforeAfterPoints) {
        if (beforeAfterPoints) {
            SortedMap<Double, Double> headMap = this.mXY.headMap(Double.valueOf(start));
            if (!headMap.isEmpty()) {
                start = ((Double) headMap.lastKey()).doubleValue();
            }
            SortedMap<Double, Double> tailMap = this.mXY.tailMap(Double.valueOf(stop));
            if (!tailMap.isEmpty()) {
                Iterator<Double> tailIterator = tailMap.keySet().iterator();
                Double next = (Double) tailIterator.next();
                if (tailIterator.hasNext()) {
                    stop = ((Double) tailIterator.next()).doubleValue();
                } else {
                    stop += next.doubleValue();
                }
            }
        }
        return this.mXY.subMap(Double.valueOf(start), Double.valueOf(stop));
    }

    public int getIndexForKey(double key) {
        return this.mXY.getIndexForKey(Double.valueOf(key));
    }

    public synchronized int getItemCount() {
        return this.mXY.size();
    }

    public double getMinX() {
        return this.mMinX;
    }

    public double getMinY() {
        return this.mMinY;
    }

    public double getMaxX() {
        return this.mMaxX;
    }

    public double getMaxY() {
        return this.mMaxY;
    }
}
