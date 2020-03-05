package com.personalfinance.app.Sqlite;

public class Info implements Comparable<Info>{
    private double money;
    private String type;
    private long time;
    private String text;

    public Info(double money, String type, long time, String text) {
        this.money = money;
        this.type = type;
        this.time = time;
        this.text = text;
    }
    public int compareTo(Info o) {
        //降序
        //return o.time - this.time;
        //升序
        // return this.time - o.time;
        if(this.time<o.time){
            return 1;
        }else if(this.time>o.time){
            return -1;
        }
        return 0;
    }
    public void setMoney(double money) {
        this.money = money;
    }

    public double getMoney() {
        return money;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
