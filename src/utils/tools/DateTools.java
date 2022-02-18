package com.farplace.farpush.itchat.utils.tools;

public class DateTools {

    public static String getDuration(long time) {
        long second = 1000;
        long minute = 60 * second;
        long hour = 60 * minute;
        long day = 24 * hour;
        long duration = System.currentTimeMillis()- time;
        System.out.println(duration);
        if (duration > day) {
            return duration / day + "天";
        } else if (duration > hour) {
            return duration / hour + "小时";
        } else if (duration > minute) {
            return duration / minute + "分钟";
        } else if (duration > second) {
            return duration / second + "秒";
        }
        return "刚才";
    }
}
