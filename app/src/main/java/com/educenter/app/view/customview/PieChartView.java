package com.educenter.app.view.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Custom 2D Canvas Donut Chart View.
 * Renders tuition payment status distribution with zero external dependencies.
 */
public class PieChartView extends View {

    private int paidCount = 0;
    private int partialCount = 0;
    private int unpaidCount = 0;

    private Paint paidPaint;
    private Paint partialPaint;
    private Paint unpaidPaint;
    private Paint bgRingPaint;
    private Paint centerTextMainPaint;
    private Paint centerTextSubPaint;

    private int colorPaid = Color.parseColor("#15803D");
    private int colorPartial = Color.parseColor("#B45309");
    private int colorUnpaid = Color.parseColor("#B91C1C");
    private int colorBgRing = Color.parseColor("#F1F5F9");
    private int colorTextMain = Color.parseColor("#0F172A");
    private int colorTextSub = Color.parseColor("#64748B");

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float strokeWidth = dpToPx(24);

        paidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paidPaint.setColor(colorPaid);
        paidPaint.setStyle(Paint.Style.STROKE);
        paidPaint.setStrokeWidth(strokeWidth);

        partialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        partialPaint.setColor(colorPartial);
        partialPaint.setStyle(Paint.Style.STROKE);
        partialPaint.setStrokeWidth(strokeWidth);

        unpaidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unpaidPaint.setColor(colorUnpaid);
        unpaidPaint.setStyle(Paint.Style.STROKE);
        unpaidPaint.setStrokeWidth(strokeWidth);

        bgRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgRingPaint.setColor(colorBgRing);
        bgRingPaint.setStyle(Paint.Style.STROKE);
        bgRingPaint.setStrokeWidth(strokeWidth);

        centerTextMainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextMainPaint.setColor(colorTextMain);
        centerTextMainPaint.setTextSize(spToPx(20));
        centerTextMainPaint.setTextAlign(Paint.Align.CENTER);
        centerTextMainPaint.setFakeBoldText(true);

        centerTextSubPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerTextSubPaint.setColor(colorTextSub);
        centerTextSubPaint.setTextSize(spToPx(11));
        centerTextSubPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(int paid, int partial, int unpaid) {
        this.paidCount = Math.max(0, paid);
        this.partialCount = Math.max(0, partial);
        this.unpaidCount = Math.max(0, unpaid);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int total = paidCount + partialCount + unpaidCount;
        int width = getWidth();
        int height = getHeight();

        float size = Math.min(width, height);
        float strokeWidth = dpToPx(24);
        float radius = (size - strokeWidth * 2) / 2f;
        float cx = width / 2f;
        float cy = height / 2f;

        float left = cx - radius;
        float top = cy - radius;
        float right = cx + radius;
        float bottom = cy + radius;

        if (total == 0) {
            // Draw empty ring
            canvas.drawArc(left, top, right, bottom, 0, 360, false, bgRingPaint);
            canvas.drawText("0", cx, cy + dpToPx(6), centerTextMainPaint);
            canvas.drawText("Hóa đơn", cx, cy + dpToPx(22), centerTextSubPaint);
            return;
        }

        float paidSweep = (paidCount * 360f) / total;
        float partialSweep = (partialCount * 360f) / total;
        float unpaidSweep = (unpaidCount * 360f) / total;

        float currentAngle = -90f; // Start from top

        // Draw segments with small gaps if more than 1 segment
        float gap = (paidCount > 0 && (partialCount > 0 || unpaidCount > 0)) ? 2f : 0f;

        if (paidCount > 0) {
            canvas.drawArc(left, top, right, bottom, currentAngle + gap / 2f, Math.max(0, paidSweep - gap), false, paidPaint);
            currentAngle += paidSweep;
        }

        if (partialCount > 0) {
            canvas.drawArc(left, top, right, bottom, currentAngle + gap / 2f, Math.max(0, partialSweep - gap), false, partialPaint);
            currentAngle += partialSweep;
        }

        if (unpaidCount > 0) {
            canvas.drawArc(left, top, right, bottom, currentAngle + gap / 2f, Math.max(0, unpaidSweep - gap), false, unpaidPaint);
        }

        // Center Text: Total invoices & label
        canvas.drawText(String.valueOf(total), cx, cy + dpToPx(4), centerTextMainPaint);
        canvas.drawText("Hóa đơn", cx, cy + dpToPx(20), centerTextSubPaint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
