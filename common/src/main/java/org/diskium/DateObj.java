package org.diskium;

public class DateObj {
    int date;
    int month;
    int year;

    public DateObj(int date, int month, int year) {
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public int getDate() {
        return date;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public boolean isAfter(DateObj input) {
        if (year > input.getYear()) return false;
        if (month > input.getMonth()) return false;
        if (date >= input.getDate()) return false;
        return true;
    }

    public boolean isBefore(DateObj input) {
        return !isAfter(input);
    }
}
