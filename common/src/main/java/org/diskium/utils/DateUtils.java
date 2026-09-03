package org.diskium.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateUtils {
    public static boolean isValidDate(String start, String end) {
        try {
            return !LocalDate.parse(start).isAfter(LocalDate.parse(end));
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
