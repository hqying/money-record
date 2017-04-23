package com.wordpress.jlvivit.moneyrecord;

import java.util.Arrays;
import java.util.Calendar;

public class Utility {

    private static final Calendar calendar = Calendar.getInstance();

    /*
     * Can't find a good Calendar method to do this for now. Working on it.
     */
    public static int getMaxDay(int year, int month) {
        Integer[] days31 = new Integer[] {1, 3, 5, 7, 8, 10, 12};
        Integer[] days30 = new Integer[] {4, 6, 9, 11};
        int maxDay;
        if (year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH)) {
            maxDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            if (Arrays.asList(days31).contains(month + 1)) {
                maxDay = 31;
            } else if (Arrays.asList(days30).contains(month + 1)) {
                maxDay = 30;
            } else if (month + 1 == 2) {
                maxDay = year % 4 == 0 ? 29 : 28;
            } else {
                throw new UnsupportedOperationException("Wrong month selection??!!??");
            }
        }
        return maxDay;
    }
}
