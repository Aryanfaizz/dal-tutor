package com.example.daltutor.units;

import android.annotation.SuppressLint;

public class DateTime {
    private String year;
    private String month;
    private String day;
    private String hour;
    private String minute;

    /**
     *
     * @param date_units: an array of String[5]: year, month, day, hour, minute
     * @return String in the format YYYY-MM-DD HH:MM
     */
   public static String format_date(String[] date_units) {
       return (String.format("%s-%s-%s %s:%s", date_units[0], date_units[1], date_units[2], date_units[3], date_units[4]));
   }

    /**
     *
     * @param date_units: an array of Int[5]: year, month, day, hour, minute
     * @return String in the format YYYY-MM-DD HH:MM
     */
   @SuppressLint("DefaultLocale")
   public static String format_date(int[] date_units) {
       return (String.format("%d-%02d-%02d %02d:%02d",
               date_units[0],
               date_units[1],
               date_units[2],
               date_units[3],
               date_units[4]));
   }
}
