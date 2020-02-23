package com.personalfinance.app.Detail;

public class DetailNodeData {
    String a, b, c, d, e, f;
    long time;

    public DetailNodeData(String a, String b, String c, String d, String e, String f, long time) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.time = time;
    }

    public void setA() {
        this.a = a;
    }
    public String getA(){
        return a;
    }
    public void setB() {
        this.b = b;
    }
    public String getB(){
        return b;
    }
    public void setC() {
        this.c = c;
    }
    public String getC(){
        return c;
    }
    public void setD() {
        this.d = d;
    }
    public String getD(){
        return d;
    }
    public void setE() {
        this.e = e;
    }public String getE(){
        return e;
    }

    public void setF() {
        this.f = f;
    }
    public String getF(){
        return f;
    }
    public void setTime() {
        this.time = time;
    }
    public long getTime(){
        return time;
    }
}