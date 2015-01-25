package psobolik.desktopclock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * DeskClockView
 * Created by psobolik on 2015-01-18.
 */
public class DeskClockView extends View {
    private final TextPaint mPaint = new TextPaint();
    private final Rect mScratchRect = new Rect();
    private int mTimeTextSize = 0;
    private int mDateTextSize = 0;
    private java.text.DateFormat mTimeDateFormat = null;
    private java.text.DateFormat mDayDateFormat = null;
    private java.text.DateFormat mDateDateFormat = null;

    public DeskClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DeskClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.mTimeDateFormat = DateFormat.getTimeFormat(this.getContext());
        this.mDayDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        this.mDateDateFormat = new SimpleDateFormat("MMMM d yyyy", Locale.getDefault());
    }

    private int compare(int v1, int v2) {
        final int lowTolerance = 0;
        final int highTolerance = 20;
        int diff = v2 - v1;
        return diff < lowTolerance ? 1 : diff > highTolerance ? -1 : 0;
    }

    private int calculateTextSize(int width, String text) {
        int result = width / 4;
        int high = width;
        int low = 0;

        Rect textBounds = this.mScratchRect;

        TextPaint paint = this.mPaint;
        paint.setTextSize(width);
        while(true) {
            paint.setTextSize(result);
            paint.getTextBounds(text, 0, text.length(), textBounds);
            Log.d("calculateTextSize", String.format("target: %d; calc: %d", width, textBounds.width()));
            int b = compare(textBounds.width(), width);
            if (b == 0) break;
            else switch (b) {
                case -1: // Too low
                    low = result;
                    break;
                case 1: // Too high
                    high = result;
                    break;
            }
            result = (high + low) / 2;
            Log.d("calculateTextSize", String.format("low: %d; high: %d; result: %d", low, high, result));
        }
        return result;
    }

    private String getTimeString(Date date) {
        return this.mTimeDateFormat.format(date);
    }

    private String getDateString(Date date) {
        return this.mDateDateFormat.format(date);
    }

    private String getDayString(Date date) {
        return this.mDayDateFormat.format(date);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        final String TIME_TEXT = "XX:XX XX";
        final String DATE_TEXT = "September XX, XXXX";

        super.onSizeChanged(xNew, yNew, xOld, yOld);

        this.mTimeTextSize = this.calculateTextSize(xNew, TIME_TEXT);
        this.mDateTextSize = this.calculateTextSize(xNew, DATE_TEXT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Fill the background
        canvas.drawColor(0xff000000);

        // Set up the paint
        TextPaint paint = this.mPaint;
        paint.setColor(0xffffffff);
        paint.setTextAlign(Paint.Align.RIGHT);

        int x = this.getWidth() - this.getPaddingRight();
        int y = this.getPaddingTop();

        @SuppressLint("DrawAllocation") GregorianCalendar calendar = new GregorianCalendar();
        Date now = calendar.getTime();

        paint.setTextSize(this.mTimeTextSize);
        y += paint.getFontSpacing();
        String text = this.getTimeString(now);
        canvas.drawText(text, x, y, paint);

        paint.setTextSize(this.mDateTextSize);
        y += paint.getFontSpacing();
        text = this.getDayString(now);
        canvas.drawText(text, x, y, paint);

        y += paint.getFontSpacing();
        text = this.getDateString(now);
        canvas.drawText(text, x, y, paint);

        this.postInvalidateDelayed(500);
    }
}
