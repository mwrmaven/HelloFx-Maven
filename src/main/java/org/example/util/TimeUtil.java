package org.example.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * @author mavenr
 * @Classname TimeUtil
 * @Description TODO
 * @Date 2023/6/13 17:21
 */
public class TimeUtil {

    private final String SPACE = " ";

    public String cron(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String cron = second + SPACE + minute + SPACE + hour + SPACE
                + day + SPACE + month + SPACE + "?" + SPACE + "*";
        System.out.println("格式化后的cron为：" + cron);
        return cron;
    }

    public Date localDateTimeToDate(LocalDateTime ldt) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = ldt.atZone(zone).toInstant();
        return Date.from(instant);
    }

    public Calendar dateToCalendar(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return calendar;
    }

    public String cron(LocalDateTime ldt) {
        return this.cron(this.dateToCalendar(this.localDateTimeToDate(ldt)));
    }
}
