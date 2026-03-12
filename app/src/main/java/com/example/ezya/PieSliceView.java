package com.example.ezya;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

public class PieSliceView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float startAngle;
    private float sweepAngle;
    private int color;
    private String emoji;
    private String categoryName;
    private double categoryAmount;
    private final RectF rectF = new RectF();

    public PieSliceView(Context context) {
        super(context);
    }

    public void setSliceData(float startAngle, float sweepAngle, int color,
                             String emoji, String categoryName, double categoryAmount) {
        this.startAngle = startAngle;
        this.sweepAngle = sweepAngle;
        this.color = color;
        this.emoji = emoji;
        this.categoryName = categoryName;
        this.categoryAmount = categoryAmount;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(cx, cy) - 8f;
        rectF.set(cx - radius, cy - radius, cx + radius, cy + radius);
        paint.setColor(color);
        canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
    }

    public String getEmoji() { return emoji; }
    public String getCategoryName() { return categoryName; }
    public double getCategoryAmount() { return categoryAmount; }
}