package com.rrafols.packt.epg.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Program {
    private String name;
    private String description;
    private long startTime;
    private long endTime;
    private String startTimeText;
    private String endTimeText;
    private String timeText;

    public Program(String name, String description, long startTime, long endTime) {
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getStartTimeText() {
        if (startTimeText == null) {
            startTimeText = getTextTime(startTime);
        }

        return startTimeText;
    }

    public String getEndTimeText() {
        if (endTimeText == null) {
            endTimeText = getTextTime(endTime);
        }

        return endTimeText;
    }

    public String getTimeText() {
        if (timeText == null) {
            timeText = getStartTimeText() + " - " + getEndTimeText();
        }
        return timeText;
    }

    private static String getTextTime(long time) {
        Date date = new Date(time);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        return dateFormatter.format(date);
    }
}
