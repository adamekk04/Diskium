package org.diskium.utils;

import org.diskium.MultiplatformLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateUtils {

    private static String dateFormat;

    public static void setDateFormat(String s) {
        dateFormat = s;
    }

    public static boolean isValidDate(String start, String end) {
        try {
            LocalDate parsedStart;
            LocalDate parsedEnd;

            if (start != null) {
                parsedStart = LocalDate.parse(start);
            } else {
                parsedStart = null;
            }

            if (end != null) {
                parsedEnd = LocalDate.parse(end);
            } else {
                parsedEnd = null;
            }

            if (parsedStart == null || parsedEnd == null) {
                return true;
            }

            return !LocalDate.parse(start).isAfter(LocalDate.parse(end));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidString(String date) {
        return date.chars().filter(c -> c == 'd').count() == 2 && date.chars().filter(c -> c == 'm').count() == 2 && date.chars().filter(c -> c == 'y').count() == 4;
    }

    public static LocalDate parse(String s) {
        String pattern = dateFormat.replace("yyyy", "uuuu").replace("mm", "MM");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);

        try {
            return LocalDate.parse(s, formatter);
        } catch (DateTimeParseException e) {
            MultiplatformLogger.error("Can't parse date: " + s);
            return null;
        }
    }
}
