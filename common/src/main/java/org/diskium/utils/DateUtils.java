package org.diskium.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateUtils {
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
}
