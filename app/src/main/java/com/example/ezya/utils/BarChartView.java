package com.example.ezya.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.Nullable;

import com.example.ezya.data.model.CategorySummary;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {

    public interface OnBarClickListener {
        void onBarClick(String emoji, String name, double amount);
    }

    private static final int[] COLORS = {
            0xFFFFDD2D, 0xFF4CAF50, 0xFF2196F3, 0xFFFF5252,
            0xFFFF9800, 0xFF9C27B0, 0xFF00BCD4, 0xFFE91E63,
            0xFF8BC34A, 0xFF795548
    };

    private static class Bar {
        String emoji;
        String name;
        double amount;
        int color;
        RectF rect = new RectF();
        float animatedHeight = 0f;
        float targetHeight = 0f;
    }

    private final List<Bar> bars = new ArrayList<>();
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selectedIndex = -1;
    private OnBarClickListener listener;
    private float animProgress = 0f;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(dp(11));
        textPaint.setTextAlign(Paint.Align.CENTER);

        labelPaint.setColor(0xFF8A8A8A);
        labelPaint.setTextSize(dp(10));
        labelPaint.setTextAlign(Paint.Align.CENTER);

        emptyPaint.setColor(0xFF2A2A2A);
        emptyPaint.setStyle(Paint.Style.FILL);

        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeWidth(dp(2));
        selectedPaint.setColor(0xFFFFFFFF);
    }

    public void setSummaries(List<CategorySummary> summaries) {
        bars.clear();
        selectedIndex = -1;

        if (summaries == null || summaries.isEmpty()) {
            invalidate();
            return;
        }

        double maxAmount = 0;
        for (CategorySummary s : summaries) {
            if (s.getAmount() > maxAmount) maxAmount = s.getAmount();
        }

        for (int i = 0; i < summaries.size(); i++) {
            CategorySummary s = summaries.get(i);
            Bar bar = new Bar();
            bar.emoji = s.getCategoryEmoji();
            bar.name = s.getCategoryName();
            bar.amount = s.getAmount();
            bar.color = COLORS[i % COLORS.length];
            bar.targetHeight = maxAmount > 0 ? (float) (s.getAmount() / maxAmount) : 0f;
            bar.animatedHeight = 0f;
            bars.add(bar);
        }

        startAnimation();
    }

    private void startAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animProgress = (float) a.getAnimatedValue();
            for (Bar bar : bars) {
                bar.animatedHeight = bar.targetHeight * animProgress;
            }
            invalidate();
        });
        animator.start();
    }

    public void setOnBarClickListener(OnBarClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        if (bars.isEmpty()) {
            float cx = w / 2f;
            float cy = h / 2f - dp(10);
            float barW = dp(32);
            float spacing = dp(12);
            int count = 4;
            float totalW = count * barW + (count - 1) * spacing;
            float startX = cx - totalW / 2f;
            for (int i = 0; i < count; i++) {
                float left = startX + i * (barW + spacing);
                float barH = dp(40 + i * 15);
                canvas.drawRoundRect(left, cy - barH, left + barW, cy,
                        dp(6), dp(6), emptyPaint);
            }
            return;
        }

        float paddingBottom = dp(28);
        float paddingTop = dp(12);
        float paddingHoriz = dp(8);
        float availableH = h - paddingBottom - paddingTop;
        float availableW = w - paddingHoriz * 2;

        int count = bars.size();
        float barW = Math.min(dp(36), (availableW / count) - dp(8));
        float spacing = (availableW - barW * count) / (count + 1);
        float baseY = h - paddingBottom;

        for (int i = 0; i < count; i++) {
            Bar bar = bars.get(i);
            float left = paddingHoriz + spacing + i * (barW + spacing);
            float right = left + barW;
            float barH = availableH * bar.animatedHeight;
            float top = baseY - barH;

            bar.rect.set(left, top, right, baseY);

            barPaint.setColor(bar.color);
            barPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(bar.rect, dp(6), dp(6), barPaint);

            if (i == selectedIndex) {
                selectedPaint.setColor(0xFFFFFFFF);
                canvas.drawRoundRect(bar.rect, dp(6), dp(6), selectedPaint);
            }

            // emoji над столбцом
            if (bar.animatedHeight > 0.1f && bar.emoji != null) {
                textPaint.setTextSize(dp(12));
                canvas.drawText(bar.emoji, left + barW / 2f, top - dp(2), textPaint);
            }

            // название под осью
            labelPaint.setColor(i == selectedIndex ? 0xFFFFDD2D : 0xFF8A8A8A);
            String label = bar.name.length() > 5
                    ? bar.name.substring(0, 4) + "…" : bar.name;
            canvas.drawText(label, left + barW / 2f, baseY + dp(14), labelPaint);
        }

        // линия оси X
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisPaint.setColor(0xFF2A2A2A);
        axisPaint.setStrokeWidth(dp(1));
        canvas.drawLine(paddingHoriz, baseY, w - paddingHoriz, baseY, axisPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float x = event.getX();
        float y = event.getY();

        for (int i = 0; i < bars.size(); i++) {
            Bar bar = bars.get(i);
            // расширяем зону касания по горизонтали
            RectF touch = new RectF(bar.rect.left - dp(8), 0,
                    bar.rect.right + dp(8), getHeight());
            if (touch.contains(x, y)) {
                selectedIndex = (selectedIndex == i) ? -1 : i;
                invalidate();
                if (listener != null && selectedIndex == i) {
                    listener.onBarClick(bar.emoji, bar.name, bar.amount);
                }
                return true;
            }
        }
        selectedIndex = -1;
        invalidate();
        return true;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}