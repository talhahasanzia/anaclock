package com.thz.anaclocklib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;


public class AnaClock extends View {


    ///--------- dial values ---------///
    protected float dialRadius;

    protected float dialWidth;

    protected int dialColor;

    @DrawableRes
    protected int dialResource;

    ///--------- hours hand values ---------///
    protected final static int HOURS_HAND = 1;

    protected int hour;

    protected double hoursHandEndX, hoursHandEndY;

    protected float hoursHandLength;

    protected double hoursHandSlope;

    protected float hoursHandWidth;

    protected int hoursHandColor;

    @DrawableRes
    protected int hoursHandResource;


    ///--------- minutes hand values ---------///
    protected final static int MINUTES_HAND = 2;

    protected int minutes;

    protected double minutesHandEndX, minutesHandEndY;

    protected float minutesHandLength;

    protected float minutesHandWidth;

    protected double minutesHandSlope;

    protected int minutesHandColour;

    @DrawableRes
    protected int minutesHandResource;

    ///--------- seconds hand values ---------///
    protected final static int SECONDS_HAND = 3;


    protected int seconds;

    protected double secondsHandEndX, secondsHandEndY;

    protected float secondsHandLength;

    protected float secondsHandWidth;

    protected double secondsHandSlope;

    protected int secondsHandColour;

    protected final long DELAY_SECOND = 1000;

    @DrawableRes
    protected int secondsHandResource;


    ///--------- global variables ---------///
    protected float validPadding;

    protected boolean isDrawingAllowed = true;

    protected boolean isActive;

    protected Runnable runnable;

    protected Handler timeHandler;

    Paint paint;

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

    protected void init(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {

            TypedArray attributeArray = context.obtainStyledAttributes(attributeSet, R.styleable.AnaClock);


            hour = attributeArray.getInteger(R.styleable.AnaClock_start_hour, Calendar.getInstance().get(Calendar.HOUR));
            minutes = attributeArray.getInteger(R.styleable.AnaClock_start_minutes, Calendar.getInstance().get(Calendar.MINUTE));
            seconds = attributeArray.getInteger(R.styleable.AnaClock_start_seconds, Calendar.getInstance().get(Calendar.SECOND));
            dialColor = attributeArray.getColor(R.styleable.AnaClock_dial_color, Color.DKGRAY);
            hoursHandColor = attributeArray.getColor(R.styleable.AnaClock_hours_hand_color, Color.DKGRAY);
            minutesHandColour = attributeArray.getColor(R.styleable.AnaClock_minutes_hand_color, Color.GRAY);
            secondsHandColour = attributeArray.getColor(R.styleable.AnaClock_seconds_hand_color, Color.RED);
            isActive = attributeArray.getBoolean(R.styleable.AnaClock_is_active, true);
            attributeArray.recycle();

        }
        // init here to avoid init in onDraw, call paint.reset() to get a fresh paint object
        paint = new Paint();
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

    protected int measureHeight(int measureSpec) throws Exception {
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

    protected int measureWidth(int measureSpec) throws Exception {

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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();


        if (isAttachedToWindow()) {

            bindHandler();

        } else {
            unbindHandler();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float viewMidX = getWidth() / 2;
        float viewMidY = getHeight() / 2;

        if (isDrawingAllowed) {

            calculateDialRadius();

            calculateSecondsHandLength();

            calculateMinutesHandLength();

            calculateHoursHandLength();

            calculateHoursHandWidth();

            calculateMinutesHandWidth();

            calculateSecondsHandWidth();

            calculateDialWidth();

            drawDial(canvas, viewMidX, viewMidY);

            drawSeconds(canvas, viewMidX, viewMidY);

            drawMinutes(canvas, viewMidX, viewMidY);

            drawHours(canvas, viewMidX, viewMidY);


        } else {

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.RED);
            paint.setTextSize(getHeight() * 0.1f);
            canvas.drawText("Error, See logs.", 4, viewMidY, paint);
        }
    }

    protected void calculateDialWidth() {
        dialWidth = getWidth() * 0.02f;
    }

    protected void calculateSecondsHandWidth() {
        secondsHandWidth = getWidth() * 0.005f;
    }

    protected void calculateMinutesHandWidth() {
        minutesHandWidth = getWidth() * 0.01f;
    }

    protected void calculateHoursHandWidth() {
        hoursHandWidth = getWidth() * 0.02f;
    }

    protected void calculateHoursHandLength() {
        hoursHandLength = (getHeight() - (getHeight() / 1.45f)) * 0.6f;
    }

    protected void calculateSecondsHandLength() {
        secondsHandLength = getHeight() * 0.42f;
    }

    protected void calculateMinutesHandLength() {
        minutesHandLength = getHeight() - (getHeight() / 1.45f);
    }

    protected void calculateDialRadius() {
        dialRadius = getWidth() / 2.25f;
    }

    protected void drawSeconds(Canvas canvas, float viewMidX, float viewMidY) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(secondsHandWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(secondsHandColour);


        secondsHandSlope = getSecondsAngleFromTime();
        secondsHandEndX = (secondsHandLength * Math.cos(secondsHandSlope)) + getWidth() / 2;
        secondsHandEndY = (secondsHandLength * Math.sin(secondsHandSlope)) + getHeight() / 2;

        canvas.drawLine(viewMidX, viewMidY, (float) secondsHandEndX, (float) secondsHandEndY, paint);
    }


    protected void drawMinutes(Canvas canvas, float viewMidX, float viewMidY) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(minutesHandWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(minutesHandColour);


        minutesHandSlope = getMinutesAngleFromTime();
        minutesHandEndX = (minutesHandLength * Math.cos(minutesHandSlope)) + getWidth() / 2;
        minutesHandEndY = (minutesHandLength * Math.sin(minutesHandSlope)) + getHeight() / 2;

        canvas.drawLine(viewMidX, viewMidY, (float) minutesHandEndX, (float) minutesHandEndY, paint);
    }

    protected void drawHours(Canvas canvas, float viewMidX, float viewMidY) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(hoursHandWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(hoursHandColor);

        hoursHandSlope = getHoursAngleFromTime();
        hoursHandEndX = (hoursHandLength * Math.cos(hoursHandSlope)) + viewMidX;
        hoursHandEndY = (hoursHandLength * Math.sin(hoursHandSlope)) + viewMidY;

        canvas.drawLine(viewMidX, viewMidY, (float) hoursHandEndX, (float) hoursHandEndY, paint);

    }

    protected void drawDial(Canvas canvas, float viewMidX, float viewMidY) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(dialColor);
        paint.setStrokeWidth(dialWidth);
        canvas.drawCircle(viewMidX, viewMidY, dialRadius, paint);
    }


    protected double getSecondsAngleFromTime() {


        if (seconds > 59)
            seconds = seconds - 60;

        double totalInput = (hour * 60) + minutes + seconds;


        double degreeInAngles = totalInput * 6;

        return Math.toRadians(degreeInAngles - 90);


    }

    protected double getMinutesAngleFromTime() {


        if (hour > 12)
            hour = hour - 12;

        double totalInput = (hour * 60) + minutes;


        double degreeInAngles = totalInput * 6;

        return Math.toRadians(degreeInAngles - 90);


    }

    protected double getHoursAngleFromTime() {


        if (hour > 12)
            hour = hour - 12;


        float hourDecimal = hour + (minutes / 60f);

        double degreeInAngles = hourDecimal * 30;


        return Math.toRadians(degreeInAngles - 90);


    }

    protected float getValidPadding() {
        float[] paddings = {getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom(), getPaddingStart(), getPaddingEnd()};

        float validPadding = paddings[0];

        for (int i = 1; i < paddings.length; i++) {
            if (validPadding < paddings[i])
                validPadding = paddings[i];
        }

        return validPadding;
    }

    protected float normalizeValue(float val) {
        return val < 0 ? val * (-1) : val;
    }

    protected void updateTime() {

        Calendar calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR);
        minutes = calendar.get(Calendar.MINUTE);
        seconds = calendar.get(Calendar.SECOND);

        invalidate();

    }

    protected void bindHandler() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isActive) {
                    updateTime();
                }
                timeHandler.postDelayed(this, DELAY_SECOND);
            }
        };

        timeHandler = new Handler();
        timeHandler.postDelayed(runnable, DELAY_SECOND);

    }

    protected void unbindHandler() {
        timeHandler.removeCallbacks(runnable);
        runnable = null;
        timeHandler = null;
    }

    ///----------- getters and setters  -----------///

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

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
