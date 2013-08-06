package com.lato.roskaroope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jaakko on 8/6/13.
 */
public class CompassView extends View {

    private Drawable mLogo = null;
    private double mAngle = 0;
    private boolean mEnabled = false;

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLogo = context.getResources().getDrawable(R.drawable.arrow);
    }

    public void setAngle(double angle) {
        if(mEnabled) {
            mAngle = angle;
            invalidate();
        }
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    protected void onDraw(Canvas canvas) {
        if (mEnabled) {
            Rect imageBounds = canvas.getClipBounds();
            mLogo.setBounds(canvas.getClipBounds());
            canvas.save();
            canvas.rotate((float) mAngle, imageBounds.centerX(), imageBounds.centerY());
            mLogo.draw(canvas);
            canvas.restore();
        }

    }
}
