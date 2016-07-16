package psobolik.dockclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * DockClockView
 * Created by psobolik on 2015-01-18.
 */
class DockClockView extends View
        implements View.OnSystemUiVisibilityChangeListener, View.OnClickListener {
    private OnSetUiVisibilityListener mOnSetUiVisibilityListener = null;
    private boolean mIsPowerConnected = false;

    private final TextPaint mPaint = new TextPaint();
    private final Rect mScratchRect = new Rect();

    private int mTimeTextSize = 0;
    private int mDateTextSize = 0;

    private final java.text.DateFormat mTimeDateFormat = new SimpleDateFormat("h:mm", Locale.getDefault());
    private final java.text.DateFormat mAmPmDateFormat = new SimpleDateFormat("a", Locale.getDefault());
    private final java.text.DateFormat mDayDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    private final java.text.DateFormat mDateDateFormat = new SimpleDateFormat("MMMM d yyyy", Locale.getDefault());

    private final Rect mTimeRect = new Rect();
    private final Rect mAmPmRect = new Rect();
    private final Rect mSecondsRect = new Rect();
    private final Rect mDayRect = new Rect();
    private final Rect mDateRect = new Rect();

    private final Runnable mNavHider = new Runnable() {
        @Override public void run() {
            setNavVisibility(false);
        }
    };

    public DockClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DockClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void setIsPowerConnected(boolean isPowerConnected) {
        this.mIsPowerConnected = isPowerConnected;
    }

    private void init() {
        Context context = this.getContext();
        if (OnSetUiVisibilityListener.class.isAssignableFrom(context.getClass())) {
            this.mOnSetUiVisibilityListener = (OnSetUiVisibilityListener)context;
        }

        setOnSystemUiVisibilityChangeListener(this);

        // Set up the paint
        this.mPaint.setTextAlign(Paint.Align.RIGHT);
        this.mPaint.setStrokeWidth(4);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.setSystemUiVisibility(visibility);

        this.setNavVisibility(true);
    }

    @Override
    public void onClick(View v) {
        this.setNavVisibility(true);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
            setNavVisibility(true);
        }
    }

    void setNavVisibility(boolean visible) {
        int visibility;

        if (visible) {
            visibility = View.SYSTEM_UI_FLAG_VISIBLE;
            Handler handler = this.getHandler();
            if (handler != null) {
                handler.removeCallbacks(mNavHider);
                handler.postDelayed(mNavHider, 3000);
            }
        } else {
            visibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        if (this.mOnSetUiVisibilityListener != null && this.mOnSetUiVisibilityListener.canSetUiVisibility()) {
            this.setSystemUiVisibility(visibility);
            // Hide the Action Bar, too
            this.mOnSetUiVisibilityListener.onSetUiVisibility(visible);
        }
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

        TextPaint paint = this.mPaint;
        paint.setTextSize(width);
        while(true) {
            paint.setTextSize(result);
            paint.getTextBounds(text, 0, text.length(), this.mScratchRect);
            //Log.d("calculateTextSize", String.format("target: %d; calc: %d", width, this.mScratchRect.width()));
            int b = compare(this.mScratchRect.width(), width);
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
            //Log.d("calculateTextSize", String.format("low: %d; high: %d; result: %d", low, high, result));
        }
        return result;
    }

    private String getAmPmString(Date date) {
        return this.mAmPmDateFormat.format(date);
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
        final String DATE_TEXT = "September MM, MMMM";
        final String THREE_MS = "MMM";
        final String ONE_M  = "M";

        super.onSizeChanged(xNew, yNew, xOld, yOld);

        this.mDateTextSize = this.calculateTextSize(xNew, DATE_TEXT);
        this.mTimeTextSize = this.mDateTextSize * 2;
        int secondsHeight = this.mDateTextSize / 8;
        int secondsMargin = secondsHeight * 2;

        int left = this.getPaddingLeft();
        int right = this.getWidth() - this.getPaddingRight();
        int top = this.getPaddingTop();

        this.mPaint.setTextSize(this.mTimeTextSize);
        int height = (int) this.mPaint.getFontSpacing();

        this.mPaint.setTextSize(this.mDateTextSize);
        int width = (int) (this.mPaint.measureText(THREE_MS) - this.mPaint.measureText(ONE_M));

        this.mAmPmRect.set(right - width, top, right, top + height);
        this.mTimeRect.set(left, top, right - width, top + height);

        top += height + secondsMargin;
        height = (int) this.mPaint.getFontSpacing();
        this.mDayRect.set(left, top, right, top + height);

        top += height;
        this.mDateRect.set(left, top, right, top + height);

        top = this.mTimeRect.bottom + secondsMargin;
        this.mSecondsRect.set(left, top, right, top + secondsHeight);

//        Log.d("mAmPmRect", this.mAmPmRect.toString());
//        Log.d("mTimeRect", this.mTimeRect.toString());
//        Log.d("mDayRect", this.mDayRect.toString());
//        Log.d("mDateRect", this.mDateRect.toString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int TICKS_PER_SECOND = 1;
        final int TICKS = 1000 / TICKS_PER_SECOND;

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        String text;
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(this.getTextColor());

        // Draw Time
        this.mPaint.setTextSize(this.mTimeTextSize);
        text = this.getTimeString(now);
        canvas.drawText(text, this.mTimeRect.right, this.mTimeRect.bottom, this.mPaint);

        this.mPaint.setTextSize(this.mDateTextSize);
        text = this.getAmPmString(now);
        canvas.drawText(text, this.mAmPmRect.right, this.mAmPmRect.bottom, this.mPaint);

        // Draw Date
        text = this.getDayString(now);
        canvas.drawText(text, this.mDayRect.right, this.mDayRect.bottom, this.mPaint);

        text = this.getDateString(now);
        canvas.drawText(text, this.mDateRect.right, this.mDateRect.bottom, this.mPaint);

        // Draw seconds
        float mid = (this.mSecondsRect.width() * ((calendar.get(Calendar.SECOND) * 1000) + calendar.get(Calendar.MILLISECOND) + 1) / 60000);
        Paint.Style styleL, styleR;
        if (calendar.get(Calendar.MINUTE) % 2 == 0) {
            styleL = Paint.Style.STROKE;
            styleR = Paint.Style.FILL_AND_STROKE;
        } else {
            styleL = Paint.Style.FILL_AND_STROKE;
            styleR = Paint.Style.STROKE;
        }
        this.mPaint.setStyle(styleL);
        canvas.drawRect(this.mSecondsRect.left, this.mSecondsRect.top, this.mSecondsRect.left + mid, this.mSecondsRect.bottom, this.mPaint);
        this.mPaint.setStyle(styleR);
        canvas.drawRect(this.mSecondsRect.left + mid, this.mSecondsRect.top, this.mSecondsRect.right, this.mSecondsRect.bottom, this.mPaint);

        //this.postInvalidateDelayed(1000 - new GregorianCalendar().get(Calendar.MILLISECOND));
        this.postInvalidateDelayed(TICKS - (calendar.get(Calendar.MILLISECOND) % TICKS));
    }

    private int getTextColor() {
        final int COLOR_POWER_CONNECTED = 0xff00b32a;
        final int COLOR_POWER_DISCONNECTED = 0xffffffff;

        return this.mIsPowerConnected ? COLOR_POWER_CONNECTED : COLOR_POWER_DISCONNECTED;
    }
}
