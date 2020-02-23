package com.personalfinance.app.Detail.DetailBulk;
/**
 * 每个节点的具体数据
 * Created by xiaoyehai on 2018/7/11 0011.
 */
public class DENodeData {

    private String type;
    private String showtime;
    private long time;
    private String money;

    /*public NodeData() {
    }*/

    public DENodeData(String type,String showtime,String money,long time) {
        this.type=type;
        this.showtime=showtime;
        this.money=money;this.time=time;
    }

    public String getType(){
        return type;
    }
    public void setType(){
        this.type=type;
    }
    public String getShowtime(){
        return showtime;
    }
    public void setShowtime(){
        this.showtime=showtime;
    }
    public String getMoney(){
        return money;
    }
    public void setMoney(){
        this.money=money;
    }
    public long getTime(){
        return time;
    }
    public void setTime(){
        this.time=time;
    }
}