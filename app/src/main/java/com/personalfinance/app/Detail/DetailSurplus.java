package com.personalfinance.app.Detail;

public class DetailSurplus {
    private String day;
    private String date;
    private double jieyu;
    private double shouru;
    private double zhichu;
    private long time;

    public DetailSurplus(String day, String date, double jieyu, double shouru, double zhichu,long time) {
        this.day=day;
        this.date=date;
        this.jieyu=jieyu;
        this.shouru=shouru;
        this.zhichu=zhichu;
        this.time=time;
    }
    public String getDay(){
        return day;
    }
    public String getDate(){
        return date;
    }
    public double getJieyu(){
        return jieyu;
    }
    public double getShouru(){
        return shouru;
    }
    public double getZhichu(){
        return zhichu;
    }
    public long getTime(){
        return time;
    }
}
