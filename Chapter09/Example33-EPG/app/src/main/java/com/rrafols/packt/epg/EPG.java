package com.rrafols.packt.epg;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.rrafols.packt.epg.data.Channel;
import com.rrafols.packt.epg.data.Program;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class EPG extends View {
    private static final String TAG = EPG.class.getName();

    private static final int BACKGROUND_COLOR = 0xFF333333;
    private static final int PROGRAM_COLOR = 0xFF666666;
    private static final int HIGHLIGHTED_PROGRAM_COLOR = 0xFFBCBCBC;
    private static final int CURRENT_TIME_COLOR = 0xB4DD1030;

    private static final int CHANNEL_HEIGHT = 80;
    private static final float DEFAULT_TIME_SCALE = 0.0001f;
    private static final float PROGRAM_MARGIN = 4;
    private static final int TIME_THRESHOLD = 16;
    private static final float ANIM_THRESHOLD = 0.01f;
    private static final float TIMEBAR_HEIGHT = 18;

    private final float channelHeight;
    private final float timebarHeight;
    private final float programMargin;
    private final int highlightedProgramColor;
    private final int highlightedProgramTextColor;
    private final int programColor;
    private final int programTextColor;


    private final Paint paintTimeBar;
    private final Paint paintChannelText;
    private final Paint paintProgramText;
    private final Paint paintProgram;
    private final Paint paintCurrentTime;

    private Channel[] channelList;
    private ChannelIconTarget[] channelTargets;
    private int  backgroundColor;

    private float dragX;
    private float dragY;
    private boolean zooming;
    private boolean dragged;
    private float scrollX;
    private float scrollY;
    private float scrollXTarget;
    private float scrollYTarget;
    private float chNameWidthTarget;
    private float chNameWidth;
    private boolean shortChannelMode;
    private boolean switchNameWidth;
    private float timeScale;

    private float frScrollX;
    private float frScrollY;
    private float frChNameWidth;

    private long timeStart;
    private long accTime;
    private Context context;
    private Rect textBoundaries;
    private Rect timeBarTextBoundaries;

    private ScaleGestureDetector scaleDetector;

    private final long initialTimeValue;
    private final Calendar calendar;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm", Locale.US);
    private final HashMap<Long, String> dateFormatted;
    private EPGCallback callback;

    public EPG(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        paintChannelText = new Paint();
        paintChannelText.setAntiAlias(true);
        paintChannelText.setTextSize(40.f);

        paintProgramText = new Paint();
        paintProgramText.setAntiAlias(true);
        paintProgramText.setTextSize(55.f);

        paintProgram = new Paint();
        paintProgram.setAntiAlias(true);
        paintProgram.setStyle(Paint.Style.FILL);

        paintTimeBar = new Paint();
        paintTimeBar.setTextSize(30.f);
        paintTimeBar.setAntiAlias(true);
        timeBarTextBoundaries = new Rect();
        paintTimeBar.getTextBounds("88:88", 0, "88:88".length(), timeBarTextBoundaries);

        paintCurrentTime = new Paint();
        paintCurrentTime.setStyle(Paint.Style.FILL);

        final float screenDensity = getResources().getDisplayMetrics().density;

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EPG, 0, 0);
        try {
            backgroundColor = ta.getColor(R.styleable.EPG_backgroundColor, BACKGROUND_COLOR);
            paintChannelText.setColor(ta.getColor(R.styleable.EPG_channelTextColor, Color.WHITE));
            paintCurrentTime.setColor(ta.getColor(R.styleable.EPG_currentTimeColor, CURRENT_TIME_COLOR));
            paintTimeBar.setColor(ta.getColor(R.styleable.EPG_timeBarColor, Color.WHITE));

            highlightedProgramColor = ta.getColor(R.styleable.EPG_highlightedProgramColor, HIGHLIGHTED_PROGRAM_COLOR);
            programColor = ta.getColor(R.styleable.EPG_programColor, PROGRAM_COLOR);

            channelHeight = ta.getDimension(R.styleable.EPG_channelHeight,
                    CHANNEL_HEIGHT * screenDensity);

            programMargin = ta.getDimension(R.styleable.EPG_programMargin,
                    PROGRAM_MARGIN * screenDensity);

            timebarHeight = ta.getDimension(R.styleable.EPG_timebarHeight,
                    TIMEBAR_HEIGHT * screenDensity);
            
            programTextColor = ta.getColor(R.styleable.EPG_programTextColor, Color.WHITE);
            highlightedProgramTextColor = ta.getColor(R.styleable.EPG_highlightedProgramTextColor, Color.BLACK);

        } finally {
            ta.recycle();
        }

        timeScale = DEFAULT_TIME_SCALE * screenDensity;

        scrollX = 0.f;
        scrollY = 0.f;
        scrollXTarget = 0.f;
        scrollYTarget = 0.f;
        zooming = false;

        // more information:
        // https://developer.android.com/training/gestures/scale.html
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private long focusTime;
            private float scrollCorrection = 0.f;
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                zooming = true;
                focusTime = getHorizontalPositionTime(scrollXTarget + detector.getFocusX() - frChNameWidth);
                scrollCorrection = getTimeHorizontalPosition((focusTime)) - scrollXTarget;
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                timeScale *= detector.getScaleFactor();
                timeScale = Math.max(DEFAULT_TIME_SCALE * screenDensity / 2,
                        Math.min(timeScale, DEFAULT_TIME_SCALE * screenDensity * 4));

                // correct scroll position otherwise will move too much when zooming
                float current = getTimeHorizontalPosition((focusTime)) - scrollXTarget;
                float scrollDifference = current - scrollCorrection;
                scrollXTarget += scrollDifference;
                zooming = true;

                invalidate();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                zooming = true;
            }
        });

        chNameWidthTarget = channelHeight;
        chNameWidth = chNameWidthTarget;
        shortChannelMode = true;
        switchNameWidth = false;

        textBoundaries = new Rect();
        timeStart = SystemClock.elapsedRealtime();
        initialTimeValue = System.currentTimeMillis() - 30 * 60 * 1000;
        calendar = Calendar.getInstance();

        dateFormatted = new HashMap<>();
    }

    public void setCallback(EPGCallback callback) {
        this.callback = callback;
    }

    public void setChannelList(Channel[] channelList) {
        this.channelList = channelList;
        this.channelTargets = new ChannelIconTarget[channelList.length];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        animateLogic();

        if (switchNameWidth) {
            if (shortChannelMode) {
                chNameWidthTarget = channelHeight * 2;
                shortChannelMode = false;
            } else {
                chNameWidthTarget = channelHeight;
                shortChannelMode = true;
            }
            switchNameWidth = false;
        }

        long currentTime = System.currentTimeMillis();

        drawBackground(canvas);
        drawEPGBody(canvas, currentTime, frScrollY);
        drawTimeBar(canvas, currentTime);
        drawCurrentTime(canvas, currentTime);

        if (missingAnimations()) invalidate();
    }

    /**
     * Draw the horizontal top bar with half-hour indications.
     */
    private void drawTimeBar(Canvas canvas, long currentTime) {
        calendar.setTimeInMillis(initialTimeValue - 120 * 60 * 1000);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long time = calendar.getTimeInMillis();
        float x = getTimeHorizontalPosition(time) - frScrollX + getWidth() / 4.f;

        while (x < getWidth()) {
            if (x > 0) {
                canvas.drawLine(x, 0, x, timebarHeight, paintTimeBar);
            }

            if (x + timeBarTextBoundaries.width() > 0) {
                String date = null;
                if (dateFormatted.containsKey(time)) {
                    date = dateFormatted.get(time);
                } else {
                    date = dateFormatter.format(new Date(time));
                    dateFormatted.put(time, date);
                }

                canvas.drawText(date,
                        x + programMargin,
                        (timebarHeight - timeBarTextBoundaries.height()) / 2.f
                                + timeBarTextBoundaries.height(),
                        paintTimeBar);
            }

            time += 30 * 60 * 1000;
            x = getTimeHorizontalPosition(time) - frScrollX + getWidth() / 4.f;
        }

        canvas.drawLine(0,
                timebarHeight,
                getWidth(),
                timebarHeight,
                paintTimeBar);
    }

    /**
     * Draw the EPG background.
     */
    private void drawBackground(Canvas canvas) {
        canvas.drawARGB(backgroundColor >> 24, (backgroundColor >> 16) & 0xff,
                (backgroundColor >> 8) & 0xff, backgroundColor & 0xff);
    }

    /**
     * Draw a vertical bar on the current time
     */
    private void drawCurrentTime(Canvas canvas, long currentTime) {
        float currentTimePos = frChNameWidth + getTimeHorizontalPosition(currentTime) - frScrollX;
        canvas.drawRect(currentTimePos - programMargin/2,
                0,
                currentTimePos + programMargin/2,
                timebarHeight,
                paintCurrentTime);

        canvas.clipRect(frChNameWidth, 0, getWidth(), getHeight());
        canvas.drawRect(currentTimePos - programMargin/2,
                timebarHeight,
                currentTimePos + programMargin/2,
                getHeight(),
                paintCurrentTime);
    }

    private void drawEPGBody(Canvas canvas, long currentTime, float verticalOffset) {
        // compute initial and end channel to draw based on the scroll position and screen size
        int startChannel = (int) (frScrollY / channelHeight);
        verticalOffset -= startChannel * channelHeight;
        int endChannel = startChannel + (int) ((getHeight() -  timebarHeight) / channelHeight) + 1;
        if (endChannel >= channelList.length) endChannel = channelList.length - 1;

        canvas.save();
        canvas.clipRect(0, timebarHeight, getWidth(), getHeight());
        for (int i = startChannel; i <= endChannel; i++) {
            float channelTop = (i - startChannel) * channelHeight - verticalOffset + timebarHeight;
            float channelBottom = channelTop + channelHeight;

            // draw channel text only when channel is expanded
            if (!shortChannelMode) {
                paintChannelText.getTextBounds(channelList[i].getName(),
                        0,
                        channelList[i].getName().length(),
                        textBoundaries);

                canvas.drawText(channelList[i].getName(),
                        channelHeight - programMargin * 2,
                        (channelHeight - textBoundaries.height()) / 2 + textBoundaries.height() + channelTop,
                        paintChannelText);
            }
            canvas.drawLine(0, channelBottom, getWidth(), channelBottom, paintChannelText);

            if (channelList[i].getIcon() != null) {
                float iconMargin = (channelHeight - channelList[i].getIcon().getHeight()) / 2;
                canvas.drawBitmap(channelList[i].getIcon(), iconMargin, channelTop + iconMargin, null);
            } else {
                if (channelTargets[i] == null) {
                    channelTargets[i] = new ChannelIconTarget(channelList[i]);
                }

                Picasso.with(context)
                        .load(channelList[i]
                        .getIconUrl())
                        .into(channelTargets[i]);
            }

            canvas.save();
            canvas.clipRect(frChNameWidth, 0, getWidth(), getHeight());
            float horizontalOffset = frChNameWidth - frScrollX;
            ArrayList<Program> programs = channelList[i].getPrograms();
            for (int j = 0; j < programs.size(); j++) {
                Program program = programs.get(j);

                long st = program.getStartTime();
                long et = program.getEndTime();

                float programStartX = getTimeHorizontalPosition(st);
                float programEndX = getTimeHorizontalPosition(et);

                // if program start position is bigger than the screen width we can discard
                // this item and the following programs we will be outside of the screen.
                if (programStartX - frScrollX > getWidth()) break;

                // if program end is before the start of the drawing area, we can skip it.
                if (programEndX - frScrollX >= 0) {
                    // highlight program if it is currently playing
                    if (st <= currentTime && et > currentTime) {
                        paintProgram.setColor(highlightedProgramColor);
                        paintProgramText.setColor(highlightedProgramTextColor);
                    } else {
                        paintProgram.setColor(programColor);
                        paintProgramText.setColor(programTextColor);
                    }

                    canvas.drawRoundRect(horizontalOffset + programMargin + programStartX,
                            channelTop + programMargin,
                            horizontalOffset - programMargin + programEndX,
                            channelBottom - programMargin,
                            programMargin,
                            programMargin,
                            paintProgram);

                    canvas.save();
                    canvas.clipRect(horizontalOffset + programMargin * 2 + programStartX,
                            channelTop + programMargin,
                            horizontalOffset - programMargin * 2 + programEndX,
                            channelBottom - programMargin);

                    paintProgramText.getTextBounds(program.getName(), 0, program.getName().length(), textBoundaries);
                    float textPosition = channelTop + textBoundaries.height() + ((channelHeight - programMargin * 2) - textBoundaries.height()) / 2;
                    canvas.drawText(program.getName(),
                                horizontalOffset + programMargin * 2 + programStartX,
                                textPosition,
                                paintProgramText);
                    canvas.restore();
                }
            }

            canvas.restore();
        }
        canvas.drawLine(frChNameWidth, timebarHeight, frChNameWidth, getHeight(), paintChannelText);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        if (zooming) {
            zooming = false;
            return true;
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragX = event.getX();
                dragY = event.getY();

                getParent().requestDisallowInterceptTouchEvent(true);
                dragged = false;
                return true;

            case MotionEvent.ACTION_UP:
                if (!dragged) {
                    // touching inside the channel area, will toggle large/short channels
                    if (event.getX() < frChNameWidth) {
                        switchNameWidth = true;
                        invalidate();
                    } else {
                        clickProgram(event.getX(), event.getY());
                    }
                }

                getParent().requestDisallowInterceptTouchEvent(false);
                return true;

            case MotionEvent.ACTION_MOVE:
                float newX = event.getX();
                float newY = event.getY();

                scrollScreen(dragX - newX, dragY - newY);

                dragX = newX;
                dragY = newY;
                dragged = true;
                return true;
            default:
                return false;
        }
    }

    private void clickProgram(float x, float y) {
        long ts = getHorizontalPositionTime(scrollXTarget + x - frChNameWidth);
        int channel = (int) ((y + frScrollY - timebarHeight) / channelHeight);

        ArrayList<Program> programs = channelList[channel].getPrograms();
        for (int i = 0; i < programs.size(); i++) {
            Program pr = programs.get(i);
            if (ts >= pr.getStartTime() && ts < pr.getEndTime()) {
                if (callback != null) {
                    callback.programClicked(channelList[channel], pr);
                }
                break;
            }
        }
    }

    /**
     * Convert a timestamp into a horizontal position.
     */
    private float getTimeHorizontalPosition(long ts) {
        long timeDifference = (ts - initialTimeValue);
        return timeDifference * timeScale;
    }

    /**
     * Convert a horizontal position into a timestamp
     */
    private long getHorizontalPositionTime(float x) {
        return (long) ((x / timeScale) + initialTimeValue);
    }

    /**
     * Check if there is any animation that has not finished.
     */
    private boolean missingAnimations() {
        if (Math.abs(scrollXTarget - scrollX) > ANIM_THRESHOLD) return true;
        if (Math.abs(scrollYTarget - scrollY) > ANIM_THRESHOLD) return true;
        if (Math.abs(chNameWidthTarget - chNameWidth) > ANIM_THRESHOLD) return true;

        return false;
    }

    /**
     * Execute logic iterations and interpolate between current and next logic iteration
     */
    private void animateLogic() {
        long currentTime = SystemClock.elapsedRealtime();
        accTime += currentTime - timeStart;
        timeStart = currentTime;

        while (accTime > TIME_THRESHOLD) {
            scrollX += (scrollXTarget - scrollX) / 4.f;
            scrollY += (scrollYTarget - scrollY) / 4.f;
            chNameWidth += (chNameWidthTarget - chNameWidth) / 4.f;
            accTime -= TIME_THRESHOLD;
        }

        float factor = ((float) accTime) / TIME_THRESHOLD;
        float nextScrollX = scrollX + (scrollXTarget - scrollX) / 4.f;
        float nextScrollY = scrollY + (scrollYTarget - scrollY) / 4.f;
        float nextChNameWidth = chNameWidth + (chNameWidthTarget - chNameWidth) / 4.f;

        frScrollX = scrollX * (1.f - factor) + nextScrollX * factor;
        frScrollY = scrollY * (1.f - factor) + nextScrollY * factor;
        frChNameWidth = chNameWidth * (1.f - factor) + nextChNameWidth * factor;
    }

    /**
     * scroll screen by dx, dy and trigger a redraw cycle.
     */
    private void scrollScreen(float dx, float dy) {
        scrollXTarget += dx;
        scrollYTarget += dy;

        if (scrollXTarget < -chNameWidth) scrollXTarget = -chNameWidth;
        if (scrollYTarget < 0) scrollYTarget = 0;

        float maxHeight = channelList.length * channelHeight - getHeight() + 1 + timebarHeight;
        if (scrollYTarget > maxHeight) scrollYTarget = maxHeight;

        invalidate();
    }


    /**
     * Picaso callback for image loading
     */
    class ChannelIconTarget implements Target {
        private Channel ch;

        ChannelIconTarget(Channel ch) {
            this.ch = ch;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            ch.setIcon(bitmap);
            invalidate();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}
    }

    interface EPGCallback {
        void programClicked(Channel channel, Program program);
    }
}
