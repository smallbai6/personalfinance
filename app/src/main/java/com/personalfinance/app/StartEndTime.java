package com.personalfinance.app;

import java.util.Calendar;

public class StartEndTime {
    private long start_time,end_time;
    private int timetype;//本日 本月 本季 本年 自定义
    public StartEndTime(long start_time,long end_time,int timetype){
        this.start_time=start_time;
        this.end_time=end_time;
        this.timetype=timetype;
    }
    public static long[] GetDay(){//得到本日的start_time end_time
        Calendar start_calendar = Calendar.getInstance();
        start_calendar.set(Calendar.HOUR_OF_DAY, 00);
        start_calendar.set(Calendar.MINUTE, 00);
        start_calendar.set(Calendar.SECOND, 00);
        start_calendar.set(Calendar.MILLISECOND, 00);
        long start_time = start_calendar.getTimeInMillis();

        Calendar end_calendar = Calendar.getInstance();
        end_calendar.set(Calendar.HOUR_OF_DAY, 23);
        end_calendar.set(Calendar.MINUTE, 59);
        end_calendar.set(Calendar.SECOND, 59);
        end_calendar.set(Calendar.MILLISECOND, 999);
        long end_time = end_calendar.getTimeInMillis();
        long[] time=new long[]{start_time,end_time};
        return time;
    }
    public static long[] GetMonth() {
        Calendar calendar = Calendar.getInstance();
        long start_time = start_date(calendar.get(Calendar.MONTH));
        long end_time = end_date(calendar.get(Calendar.MONTH));
        long[] time=new long[]{start_time,end_time};
        return time;
    }
    public static long[] GetSeason() {
        Calendar calendar = Calendar.getInstance();
        long start_time=0,end_time=0;
        switch (calendar.get(Calendar.MONTH) + 1) {
            case 1:
            case 2:
            case 3:
                start_time = start_date(0);
                end_time = end_date(2);
                // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            case 4:
            case 5:
            case 6:
                start_time = start_date(3);
                end_time = end_date(5);
                // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            case 7:
            case 8:
            case 9:
                start_time = start_date(6);
                end_time = end_date(8);
                // Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            case 10:
            case 11:
            case 12:
                start_time = start_date(9);
                end_time = end_date(11);
                //Log.d("liangjialing", LongToString(start_time) + "-----" + LongToString(end_time));
                break;
            default:
                break;
        }
        long[] time=new long[]{start_time,end_time};
        return time;
    }
    public static long[] GetYear() {
        long start_time = start_date(0);
        long end_time=end_date(11);
        long[] time=new long[]{start_time,end_time};
        return time;
    }

    private static long start_date(int startmonth) {
        Calendar start_calendar = Calendar.getInstance();
        start_calendar.set(Calendar.MONTH, startmonth);
        start_calendar.set(Calendar.DAY_OF_MONTH, 1);
        start_calendar.set(Calendar.HOUR_OF_DAY, 00);
        start_calendar.set(Calendar.MINUTE, 00);
        start_calendar.set(Calendar.SECOND, 00);
        start_calendar.set(Calendar.MILLISECOND, 00);
        return start_calendar.getTimeInMillis();
    }

    private static long end_date(int endmonth) {
        Calendar end_calendar = Calendar.getInstance();
        end_calendar.set(Calendar.MONTH, endmonth);
        end_calendar.set(Calendar.DAY_OF_MONTH, 1);//日期设置为1号
        end_calendar.add(Calendar.MONTH,1);
        end_calendar.add(Calendar.DAY_OF_MONTH, -1);//倒回到前一天
        end_calendar.set(Calendar.HOUR_OF_DAY, 23);
        end_calendar.set(Calendar.MINUTE, 59);
        end_calendar.set(Calendar.SECOND, 59);
        end_calendar.set(Calendar.MILLISECOND, 999);
        return end_calendar.getTimeInMillis();
    }
    public long[] SetLast(){
        Calendar start_calendar = Calendar.getInstance();
        Calendar end_calendar = Calendar.getInstance();
        start_calendar.setTimeInMillis(start_time);
        end_calendar.setTimeInMillis(end_time);
        if(timetype==0){//本日
            start_calendar.add(Calendar.DAY_OF_MONTH, -1);//倒回到前一天
            end_calendar.add(Calendar.DAY_OF_MONTH, -1);//倒回到前一天
        }else if(timetype==1) {//本月
            start_calendar.add(Calendar.MONTH,-1);
            end_calendar.set(Calendar.DAY_OF_MONTH,1);
            end_calendar.add(Calendar.DAY_OF_MONTH,-1);
        }else if(timetype==2){//本季
            start_calendar.add(Calendar.MONTH,-3);
            end_calendar.add(Calendar.DAY_OF_MONTH,1);
            end_calendar.add(Calendar.MONTH,-3);
            end_calendar.add(Calendar.DAY_OF_MONTH,-1);
        }else if(timetype==3){//本年
            start_calendar.add(Calendar.YEAR,-1);
            end_calendar.add(Calendar.YEAR,-1);
        }
        end_calendar.set(Calendar.HOUR_OF_DAY, 23);
        end_calendar.set(Calendar.MINUTE, 59);
        end_calendar.set(Calendar.SECOND, 59);
        end_calendar.set(Calendar.MILLISECOND, 999);
        start_time = start_calendar.getTimeInMillis();
        end_time = end_calendar.getTimeInMillis();
        if(timetype==4){//自定义
            long init_start_time=start_time;
            long init_end_time=end_time;
            // end_time-start_time;
            end_time=init_start_time-1;
            start_time=end_time-(init_end_time-init_start_time);

        }
        long[] time=new long[]{start_time,end_time};
        return time;
    }
    public long[] SetNext(){
        Calendar start_calendar = Calendar.getInstance();
        Calendar end_calendar = Calendar.getInstance();
        start_calendar.setTimeInMillis(start_time);
        end_calendar.setTimeInMillis(end_time);
        if(timetype==0){//本日
            start_calendar.add(Calendar.DAY_OF_MONTH, 1);
            end_calendar.add(Calendar.DAY_OF_MONTH, 1);
        }else if(timetype==1) {//本月
            start_calendar.add(Calendar.MONTH,1);
            end_calendar.add(Calendar.DAY_OF_MONTH,1);
            end_calendar.add(Calendar.MONTH,1);
            end_calendar.add(Calendar.DAY_OF_MONTH,-1);
        }else if(timetype==2){//本季
            start_calendar.add(Calendar.MONTH,3);
            end_calendar.add(Calendar.DAY_OF_MONTH,1);
            end_calendar.add(Calendar.MONTH,3);
            end_calendar.add(Calendar.DAY_OF_MONTH,-1);
        }else if(timetype==3){//本年
            start_calendar.add(Calendar.YEAR,1);
            end_calendar.add(Calendar.YEAR,1);
        }
        end_calendar.set(Calendar.HOUR_OF_DAY, 23);
        end_calendar.set(Calendar.MINUTE, 59);
        end_calendar.set(Calendar.SECOND, 59);
        end_calendar.set(Calendar.MILLISECOND, 999);
        start_time = start_calendar.getTimeInMillis();
        end_time = end_calendar.getTimeInMillis();
        if(timetype==4){//自定义
            long init_start_time=start_time;
            long init_end_time=end_time;
            start_time=init_end_time+1;
            end_time=start_time+(init_end_time-init_start_time);
        }
        long[] time=new long[]{start_time,end_time};
        return time;
    }

}
