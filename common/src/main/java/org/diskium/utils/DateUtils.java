package org.diskium.utils;

import org.diskium.MultiplatformLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateUtils {

    private static String dateFormat;

/**
     * Sets date format from config.
     *
     * @param s Date format to be set.
     */
    public static void setDateFormat(String s) {
        dateFormat = s;
    }

/**
     * Checks that non-null bounds are valid dates and that the start does
     * not follow the end. Does not use the configured date format.
     *
     * @param start the lower date bound, or {@code null} for no lower limit
     * @param end the upper date bound, or {@code null} for no upper limit
     * @return {@code true} if both supplied dates are valid and ordered
     */
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

/**
     * Checks character counts in a date pattern, without validating its structure.
     *
     * @param date the pattern to inspect
     * @return {@code true} if it contains exactly two {@code d}, two {@code m},
     *         and four {@code y} characters
     */
    public static boolean isValidString(String date) {
        return date.chars().filter(c -> c == 'd').count() == 2 && date.chars().filter(c -> c == 'm').count() == 2 && date.chars().filter(c -> c == 'y').count() == 4;
    }

/**
     * Parses the date by using date format.
     *
     * @param s String value of the date to be parsed.
     * @return Parsed date or null, if it couldn't be parsed (that also logs as error).
     */
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
