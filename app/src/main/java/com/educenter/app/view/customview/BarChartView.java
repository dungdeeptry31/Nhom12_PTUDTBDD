package com.educenter.app.view.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.educenter.app.repository.StatisticsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Custom 2D Canvas Bar Chart View.
 * Renders revenue bars across recent months with zero external dependencies.
 */
public class BarChartView extends View {

    private final List<StatisticsRepository.MonthlyRevenue> dataList = new ArrayList<>();
    private Paint barPaint;
    private Paint barBgPaint;
    private Paint textPaint;
    private Paint valuePaint;
    private Paint gridPaint;

    private int primaryColor = Color.parseColor("#2563EB");
    private int barBgColor = Color.parseColor("#F1F5F9");
    private int textColor = Color.parseColor("#64748B");
    private int valueColor = Color.parseColor("#0F172A");
    private int gridColor = Color.parseColor("#E2E8F0");

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(primaryColor);
        barPaint.setStyle(Paint.Style.FILL);

        barBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barBgPaint.setColor(barBgColor);
        barBgPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(spToPx(11));
        textPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(valueColor);
        valuePaint.setTextSize(spToPx(10));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(gridColor);
        gridPaint.setStrokeWidth(dpToPx(1));
    }

    public void setData(List<StatisticsRepository.MonthlyRevenue> data) {
        dataList.clear();
        if (data != null) {
            dataList.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int count = dataList.size();
        if (count == 0) {
            drawEmptyState(canvas);
            return;
        }

        int width = getWidth();
        int height = getHeight();

        float paddingLeft = dpToPx(12);
        float paddingRight = dpToPx(12);
        float paddingTop = dpToPx(28);
        float paddingBottom = dpToPx(32);

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;
        float baselineY = height - paddingBottom;

        // Baseline line
        canvas.drawLine(paddingLeft, baselineY, width - paddingRight, baselineY, gridPaint);

        // Find max value
        double maxVal = 1000000.0; // Default minimum scale: 1M
        for (StatisticsRepository.MonthlyRevenue item : dataList) {
            if (item.revenue > maxVal) {
                maxVal = item.revenue;
            }
        }

        float slotWidth = chartWidth / count;
        float barWidth = Math.min(slotWidth * 0.55f, dpToPx(34));
        float cornerRadius = dpToPx(6);

        for (int i = 0; i < count; i++) {
            StatisticsRepository.MonthlyRevenue item = dataList.get(i);
            float centerX = paddingLeft + (i + 0.5f) * slotWidth;
            float left = centerX - barWidth / 2f;
            float right = centerX + barWidth / 2f;

            // Background placeholder bar
            canvas.drawRoundRect(left, paddingTop, right, baselineY, cornerRadius, cornerRadius, barBgPaint);

            // Active bar
            float barHeight = (float) ((item.revenue / maxVal) * chartHeight);
            if (barHeight > 0) {
                float barTop = baselineY - barHeight;
                canvas.drawRoundRect(left, barTop, right, baselineY, cornerRadius, cornerRadius, barPaint);

                // Value text above bar
                String valText = formatShortMoney(item.revenue);
                canvas.drawText(valText, centerX, barTop - dpToPx(5), valuePaint);
            } else {
                canvas.drawText("0", centerX, baselineY - dpToPx(5), textPaint);
            }

            // Month label below baseline
            canvas.drawText(item.monthLabel, centerX, baselineY + dpToPx(18), textPaint);
        }
    }

    private void drawEmptyState(Canvas canvas) {
        String msg = "Chưa có dữ liệu doanh thu";
        float x = getWidth() / 2f;
        float y = getHeight() / 2f;
        canvas.drawText(msg, x, y, textPaint);
    }

    private String formatShortMoney(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format(Locale.US, "%.1f tỷ", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            double mil = amount / 1_000_000.0;
            if (mil == (long) mil) {
                return String.format(Locale.US, "%d tr", (long) mil);
            } else {
                return String.format(Locale.US, "%.1f tr", mil);
            }
        } else if (amount >= 1_000) {
            return String.format(Locale.US, "%d k", (long) (amount / 1_000));
        } else {
            return String.valueOf((long) amount);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }
}
