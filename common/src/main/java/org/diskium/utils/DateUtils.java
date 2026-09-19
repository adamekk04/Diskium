package org.diskium.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateUtils {
    public static boolean isValidDate(String start, String end) {
        try {
            if (start == null || end == null) return true;
            return !LocalDate.parse(start).isAfter(LocalDate.parse(end));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidString(String date) {
        return date.chars().filter(c -> c == 'd').count() == 2 && date.chars().filter(c -> c == 'm').count() == 2 && date.chars().filter(c -> c == 'y').count() == 4;
    }
}
