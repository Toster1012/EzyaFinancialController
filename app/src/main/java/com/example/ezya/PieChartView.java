package com.example.ezya;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import java.util.ArrayList;
import java.util.List;

public class PieChartView extends View {

    public interface OnSliceClickListener {
        void onSliceClicked(String emoji, String name, double amount);
    }

    private static class SliceData {
        float startAngle;
        float sweepAngle;
        int color;
        String emoji;
        String name;
        double amount;
        float currentOffset = 0f;

        SliceData(float startAngle, float sweepAngle, int color,
                  String emoji, String name, double amount) {
            this.startAngle = startAngle;
            this.sweepAngle = sweepAngle;
            this.color = color;
            this.emoji = emoji;
            this.name = name;
            this.amount = amount;
        }
    }

    private final List<SliceData> slices = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rectF = new RectF();
    private int selectedIndex = -1;
    private OnSliceClickListener sliceClickListener;
    private boolean isEmpty = true;

    private static final float EXPAND_OFFSET = 18f;
    private static final int[] CHART_COLORS = {
            0xFFFFDD2D, 0xFFFF6B6B, 0xFF4ECDC4, 0xFF45B7D1,
            0xFF96CEB4, 0xFFFF8B94, 0xFFA8E6CF, 0xFFFFD3B6
    };

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        emptyPaint.setColor(0xFF2A2A2A);
        emptyPaint.setStyle(Paint.Style.STROKE);
        emptyPaint.setStrokeWidth(20f);
        emptyPaint.setAntiAlias(true);
    }

    public void setSliceClickListener(OnSliceClickListener listener) {
        this.sliceClickListener = listener;
    }

    public void setCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            buildSlices(new ArrayList<>());
            return;
        }
        List<SliceInput> inputs = new ArrayList<>();
        for (Category c : categories) {
            inputs.add(new SliceInput(c.getEmoji(), c.getName(), c.getAmount()));
        }
        buildSlices(inputs);
    }

    public void setSummaries(List<CategorySummary> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            buildSlices(new ArrayList<>());
            return;
        }
        List<SliceInput> inputs = new ArrayList<>();
        for (CategorySummary s : summaries) {
            inputs.add(new SliceInput(s.getEmoji(), s.getName(), s.getAmount()));
        }
        buildSlices(inputs);
    }

    private static class SliceInput {
        String emoji;
        String name;
        double amount;

        SliceInput(String emoji, String name, double amount) {
            this.emoji = emoji;
            this.name = name;
            this.amount = amount;
        }
    }

    private void buildSlices(List<SliceInput> inputs) {
        slices.clear();
        selectedIndex = -1;

        if (inputs.isEmpty()) {
            isEmpty = true;
            invalidate();
            return;
        }

        isEmpty = false;
        double total = 0;
        for (SliceInput s : inputs) total += s.amount;

        float startAngle = -90f;
        for (int i = 0; i < inputs.size(); i++) {
            SliceInput s = inputs.get(i);
            float sweepAngle = (float) (s.amount / total * 360f);
            slices.add(new SliceData(startAngle, sweepAngle,
                    CHART_COLORS[i % CHART_COLORS.length],
                    s.emoji, s.name, s.amount));
            startAngle += sweepAngle;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - EXPAND_OFFSET - 8f;

        if (isEmpty) {
            rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);
            canvas.drawArc(rectF, 0, 360, false, emptyPaint);
            return;
        }

        for (SliceData slice : slices) {
            float offset = slice.currentOffset;
            double midAngle = Math.toRadians(slice.startAngle + slice.sweepAngle / 2f);
            float offsetX = (float) Math.cos(midAngle) * offset;
            float offsetY = (float) Math.sin(midAngle) * offset;

            RectF rf = new RectF(
                    cx - radius + offsetX,
                    cy - radius + offsetY,
                    cx + radius + offsetX,
                    cy + radius + offsetY
            );

            paint.setColor(slice.color);
            canvas.drawArc(rf, slice.startAngle, slice.sweepAngle, true, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        if (isEmpty) return true;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float dx = event.getX() - cx;
        float dy = event.getY() - cy;

        double touchRadius = Math.sqrt(dx * dx + dy * dy);
        float radius = Math.min(cx, cy) - EXPAND_OFFSET - 8f;
        if (touchRadius > radius + EXPAND_OFFSET) return true;

        double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;

        for (int i = 0; i < slices.size(); i++) {
            SliceData slice = slices.get(i);
            float normalizedStart = slice.startAngle + 90;
            if (normalizedStart < 0) normalizedStart += 360;
            float normalizedEnd = normalizedStart + slice.sweepAngle;

            if (angle >= normalizedStart && angle <= normalizedEnd) {
                toggleSlice(i);
                return true;
            }
        }
        return true;
    }

    private void toggleSlice(int index) {
        if (selectedIndex == index) {
            animateSlice(index, EXPAND_OFFSET, 0f);
            selectedIndex = -1;
        } else {
            if (selectedIndex >= 0 && selectedIndex < slices.size()) {
                animateSlice(selectedIndex, slices.get(selectedIndex).currentOffset, 0f);
            }
            animateSlice(index, 0f, EXPAND_OFFSET);
            selectedIndex = index;

            SliceData slice = slices.get(index);
            if (sliceClickListener != null) {
                sliceClickListener.onSliceClicked(slice.emoji, slice.name, slice.amount);
            }
        }
    }

    private void animateSlice(int index, float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(250);
        animator.setInterpolator(new OvershootInterpolator(1.5f));
        animator.addUpdateListener(anim -> {
            if (index < slices.size()) {
                slices.get(index).currentOffset = (float) anim.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }
}