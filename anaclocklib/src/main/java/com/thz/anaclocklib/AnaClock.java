package com.thz.anaclocklib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;


public class AnaClock extends View {

    private static final String TAG = "ANACLOCK";
    private final static int SMALL_HAND = 1;
    private final static int BIG_HAND = 2;
    private int hour, minutes;
    private double smallHandStartX, smallHandEndX, bigHandStartX, bigHandEndX, smallHandStartY, smallHandEndY, bigHandStartY, bigHandEndY;
    private float smallHandLength, bigHandLength, dialRadius;
    private float smallHandWidth, bigHandWidth, dialWidth;
    private float validPadding;
    private double smallHandSlope, bigHandSlope;
    private int smallHandColor, bigHandColor;
    private boolean isDrawingAllowed = true;
    private boolean isActive;
    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isActive)
                updateTime();
        }
    };
    private int dialColor;

    public AnaClock(Context context) {
        super(context);
        init(context, null);
    }

    public AnaClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnaClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnaClock(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {

            TypedArray attributeArray = context.obtainStyledAttributes(attributeSet, R.styleable.AnaClock);


            hour = attributeArray.getInteger(R.styleable.AnaClock_start_hour, Calendar.getInstance().get(Calendar.HOUR));
            minutes = attributeArray.getInteger(R.styleable.AnaClock_start_minutes, Calendar.getInstance().get(Calendar.MINUTE));
            dialColor = attributeArray.getColor(R.styleable.AnaClock_dial_color, Color.BLUE);
            smallHandColor = attributeArray.getColor(R.styleable.AnaClock_small_hand_color, Color.RED);
            bigHandColor = attributeArray.getColor(R.styleable.AnaClock_big_hand_color, Color.BLUE);
            isActive = attributeArray.getBoolean(R.styleable.AnaClock_is_active, true);
            attributeArray.recycle();

        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        isDrawingAllowed = true;

        validPadding = getValidPadding();


        // ensure that width and height is same
        int measuredHeight = 0;
        int measuredWidth = 0;
        try {
            measuredWidth = measureWidth(widthMeasureSpec);
            measuredHeight = measureHeight(heightMeasureSpec);

            if (measuredHeight != measuredWidth) {
                isDrawingAllowed = false;
                throw new Exception(AnaClock.class.getCanonicalName() + ": Height and Width of this view must be same.");
            }

            if (normalizeValue(validPadding) > measuredWidth / 2) {
                isDrawingAllowed = false;
                throw new Exception(AnaClock.class.getCanonicalName() + ": Invalid padding values");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();


        if (isAttachedToWindow()) {


            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(tickReceiver, filter);

        } else {
            getContext().unregisterReceiver(tickReceiver);
        }
    }

    private int measureHeight(int measureSpec) throws Exception {
        // ensure that width and height is same
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;

        if (specMode == MeasureSpec.EXACTLY) { // only allowed fixed size or match parent
            result = specSize;
        } else {
            isDrawingAllowed = false;
            throw new Exception(AnaClock.class.getCanonicalName() + ": Height of this view cannot be wrap_content");
        }

        return result;
    }

    private int measureWidth(int measureSpec) throws Exception {

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        int result;

        if (specMode == MeasureSpec.EXACTLY) { // only allowed fixed size or match parent
            result = specSize;
        } else {
            isDrawingAllowed = false;
            throw new Exception(AnaClock.class.getCanonicalName() + ": Height of this view cannot be wrap_content");
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float viewMidX = getWidth() / 2;
        float viewMidY = getHeight() / 2;

        if (isDrawingAllowed) {

            dialRadius = getWidth() / 2.25f;
            smallHandStartX = viewMidX;
            smallHandStartY = viewMidY;

            bigHandStartX = smallHandStartX;
            bigHandStartY = smallHandStartY;


            bigHandLength = getHeight() - (getHeight() / 1.45f);
            smallHandLength = bigHandLength * 0.6f;


            smallHandWidth = getWidth() * 0.02f;
            bigHandWidth = getWidth() * 0.01f;
            dialWidth = smallHandWidth;

            drawDial(canvas, viewMidX, viewMidY);

            drawBigHand(canvas, viewMidX, viewMidY);

            drawSmallHand(canvas, viewMidX, viewMidY);


        } else {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            paint.setTextSize(getHeight() * 0.1f);
            canvas.drawText("Error, See logs.", 4, viewMidY, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if(checkTouchedElement(event.getX(), event.getY())==SMALL_HAND)
                    Log.d(TAG, "onTouchEvent: small hand touched");
                else if(checkTouchedElement(event.getX(), event.getY())==BIG_HAND)
                    Log.d(TAG, "onTouchEvent: big hand touched");
                else
                    Log.d(TAG, "onTouchEvent: No hand touch");



                break;
            }

        }
        return true;
    }

    private int checkTouchedElement(float x, float y) {

        Point clickedPoint = new Point((int) x, (int) y);
        Point startPoint = new Point((int) smallHandStartX, (int) smallHandStartY);

        // small hand
        Point smallHandPoint = new Point((int) smallHandEndX, (int) smallHandEndY);

        if (inLine(startPoint, smallHandPoint, clickedPoint))
            return SMALL_HAND;

        // big hand
        Point bigHandPoint = new Point((int) bigHandEndX, (int) bigHandEndY);

        if (inLine(startPoint, bigHandPoint, clickedPoint))
            return BIG_HAND;

        return -1;

    }

    private boolean inLine(Point A, Point B, Point C) {
        // if AC is horizontal
        if (A.x == C.x) return B.x == C.x;
        // if AC is vertical.
        if (A.y == C.y) return B.y == C.y;
        // match the gradients
        return (A.x - C.x) * (A.y - C.y) == (C.x - B.x) * (C.y - B.y);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
        invalidate();
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
        invalidate();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    private void drawBigHand(Canvas canvas, float viewMidX, float viewMidY) {
        Paint paint = new Paint();
        paint.setStrokeWidth(bigHandWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(bigHandColor);


        bigHandSlope = getMinutesAngleFromTime();
        bigHandEndX = (bigHandLength * Math.cos(bigHandSlope)) + getWidth() / 2;
        bigHandEndY = (bigHandLength * Math.sin(bigHandSlope)) + getHeight() / 2;

        canvas.drawLine(viewMidX, viewMidY, (float) bigHandEndX, (float) bigHandEndY, paint);
    }

    private void drawSmallHand(Canvas canvas, float viewMidX, float viewMidY) {
        Paint paint = new Paint();
        paint.setStrokeWidth(smallHandWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(smallHandColor);

        smallHandSlope = getHoursAngleFromTime();
        smallHandEndX = (smallHandLength * Math.cos(smallHandSlope)) + viewMidX;
        smallHandEndY = (smallHandLength * Math.sin(smallHandSlope)) + viewMidY;

        canvas.drawLine(viewMidX, viewMidY, (float) smallHandEndX, (float) smallHandEndY, paint);

    }

    private void drawDial(Canvas canvas, float viewMidX, float viewMidY) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(dialColor);
        paint.setStrokeWidth(dialWidth);
        canvas.drawCircle(viewMidX, viewMidY, dialRadius, paint);
    }

    private double getMinutesAngleFromTime() {

        Log.d(TAG, "getMinutesAngleFromTime: hour: " + hour);
        Log.d(TAG, "getMinutesAngleFromTime: mins: " + minutes);

        if (hour > 12)
            hour = hour - 12;

        double totalInput = (hour * 60) + minutes;


        double degreeInAngles = totalInput * 6;

        return Math.toRadians(degreeInAngles - 90);


    }

    private double getHoursAngleFromTime() {

        Log.d("ANACLOCK", "getMinutesAngleFromTime: hour: " + hour);

        if (hour > 12)
            hour = hour - 12;


        float hourDecimal = hour + (minutes / 60f);

        double degreeInAngles = hourDecimal * 30;


        return Math.toRadians(degreeInAngles - 90);


    }

    private float getValidPadding() {
        float[] paddings = {getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom(), getPaddingStart(), getPaddingEnd()};

        float validPadding = paddings[0];

        for (int i = 1; i < paddings.length; i++) {
            if (validPadding < paddings[i])
                validPadding = paddings[i];
        }

        return validPadding;
    }

    private float normalizeValue(float val) {
        return val < 0 ? val * (-1) : val;
    }

    private void updateTime() {

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR);
        minutes = calendar.get(Calendar.MINUTE);

        invalidate();

    }
}
