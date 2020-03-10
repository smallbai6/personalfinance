package com.personalfinance.app.Main;

public class MainListClass {
    private String name,time,expend,income;
    public MainListClass(String name,String time,String expend,String income){
        this.name=name;
        this.time=time;
        this.expend=expend;
        this.income=income;
    }
    public void setName(){
        this.name=name;
    }
    public String getName(){
        return name;
    }
    public void setTime(){
        this.time=time;
    }
    public String getTime(){
        return time;
    }
    public void setExpend(){
        this.expend=expend;
    }
    public String getExpend(){
        return expend;
    }
    public void setIncome(){
        this.income=income;
    }
    public String getIncome(){
        return income;
    }
}
