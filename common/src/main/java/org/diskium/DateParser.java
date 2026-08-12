package org.diskium;

public class DateParser {

    public static DateObj parse(String input, String format) {
        String date = refactor(input, true);
        String refFormat = refactor(format, false);
        StringBuilder day = new StringBuilder();
        StringBuilder month = new StringBuilder();
        StringBuilder year = new StringBuilder();
        if (date.length() != refFormat.length()) return null;
        for (int i = 0; i < date.length(); i++) {
            if (format.charAt(i) == 'd') {
                day.append(date.charAt(i));
            } else if (format.charAt(i) == 'm') {
                month.append(date.charAt(i));
            } else {
                year.append(date.charAt(i));
            }
        }
        try {
            return new DateObj(Integer.parseInt(day.toString()), Integer.parseInt(month.toString()), Integer.parseInt(year.toString()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String refactor(String input, boolean digit) {
        String allowedChars;
        if (digit) {
            allowedChars = "1234567890";
        } else {
            allowedChars = "dmy";
        }

        StringBuilder builder = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (allowedChars.indexOf(c) != -1) {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
