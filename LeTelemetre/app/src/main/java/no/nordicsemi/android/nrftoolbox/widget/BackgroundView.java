package no.nordicsemi.android.nrftoolbox.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class BackgroundView extends ImageView {
    public BackgroundView(Context context) {
        super(context);
        setScaleType(ScaleType.MATRIX);
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
    }

    protected boolean setFrame(int l, int t, int r, int b) {
        Matrix matrix = new Matrix();
        float scaleFactor = ((float) r) / ((float) getDrawable().getIntrinsicWidth());
        matrix.setScale(scaleFactor, scaleFactor, 0.0f, 0.0f);
        matrix.postTranslate(0.0f, ((float) b) - (((float) getDrawable().getIntrinsicHeight()) * scaleFactor));
        setImageMatrix(matrix);
        return super.setFrame(l, t, r, b);
    }
}
