package com.educenter.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility for formatting and parsing dates and times in EduCenter.
 * Supports standard Vietnamese display formats and ISO database storage formats.
 */
public final class DateUtils {

    public static final String FORMAT_DATE_DB = "yyyy-MM-dd";
    public static final String FORMAT_DATETIME_DB = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_DISPLAY = "dd/MM/yyyy";
    public static final String FORMAT_TIME_DISPLAY = "HH:mm";
    public static final String FORMAT_MONTH_YEAR = "MM/yyyy";

    private DateUtils() {
        // Utility class
    }

    public static String getTodayDb() {
        return new SimpleDateFormat(FORMAT_DATE_DB, Locale.getDefault()).format(new Date());
    }

    public static String getCurrentDateTimeDb() {
        return new SimpleDateFormat(FORMAT_DATETIME_DB, Locale.getDefault()).format(new Date());
    }

    public static String getCurrentMonthYear() {
        return new SimpleDateFormat(FORMAT_MONTH_YEAR, Locale.getDefault()).format(new Date());
    }

    public static String toDisplayDate(String dbDate) {
        if (dbDate == null || dbDate.trim().isEmpty()) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat(FORMAT_DATE_DB, Locale.getDefault()).parse(dbDate);
            if (date != null) {
                return new SimpleDateFormat(FORMAT_DATE_DISPLAY, Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            // If already in display format or parsing fails, return as is
            return dbDate;
        }
        return dbDate;
    }

    public static String toDbDate(String displayDate) {
        if (displayDate == null || displayDate.trim().isEmpty()) {
            return "";
        }
        try {
            Date date = new SimpleDateFormat(FORMAT_DATE_DISPLAY, Locale.getDefault()).parse(displayDate);
            if (date != null) {
                return new SimpleDateFormat(FORMAT_DATE_DB, Locale.getDefault()).format(date);
            }
        } catch (ParseException e) {
            return displayDate;
        }
        return displayDate;
    }

    public static String getDayOfWeekName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Thứ 2";
            case Calendar.TUESDAY:
                return "Thứ 3";
            case Calendar.WEDNESDAY:
                return "Thứ 4";
            case Calendar.THURSDAY:
                return "Thứ 5";
            case Calendar.FRIDAY:
                return "Thứ 6";
            case Calendar.SATURDAY:
                return "Thứ 7";
            case Calendar.SUNDAY:
                return "Chủ nhật";
            default:
                return "Thứ " + dayOfWeek;
        }
    }

    public static boolean isEndDateAfterStartDate(String startDateDb, String endDateDb) {
        if (startDateDb == null || endDateDb == null) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATE_DB, Locale.getDefault());
            Date start = sdf.parse(startDateDb);
            Date end = sdf.parse(endDateDb);
            if (start != null && end != null) {
                return !end.before(start);
            }
        } catch (ParseException e) {
            return false;
        }
        return false;
    }

    public static boolean isEndTimeAfterStartTime(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_TIME_DISPLAY, Locale.getDefault());
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            if (start != null && end != null) {
                return end.after(start);
            }
        } catch (ParseException e) {
            return false;
        }
        return false;
    }
}
