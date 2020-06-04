package com.personalfinance.app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.personalfinance.app.Config.DatabaseConfig;
import com.personalfinance.app.Sqlite.DetailSurplus;
import com.personalfinance.app.Sqlite.Info;
import com.personalfinance.app.Sqlite.Node;
import com.personalfinance.app.Sqlite.NodeData;
import com.personalfinance.app.Util.DataFormatUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DetailList {
    private SQLiteDatabase db;
    private Cursor cursor;
    private String Username;
    private long start_time;
    private long end_time;
    private List<Node> list = new ArrayList<>();
    private List<Info> InfoList = new ArrayList<>();
    private List<DetailSurplus> day_dayList = new ArrayList<>();
    private List<DetailSurplus> day_monthList = new ArrayList<>();
    private List<DetailSurplus> month_monthList = new ArrayList<>();
    private List<DetailSurplus> season_seasonList = new ArrayList<>();
    private List<DetailSurplus> year_yearList = new ArrayList<>();

    public DetailList(String Username, long start_time, long end_time) {
        this.Username = Username;
        this.start_time = start_time;
        this.end_time = end_time;
       }

    /*
   日详情列表
    */
    public List<Info> DetailInfo() {//获取该时间段内所有的流水
        InfoList.clear();
        //支出收入信息
        try {
            db = SQLiteDatabase.openDatabase(DatabaseConfig.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        for (int i = 0; i < 2; i++) {

            if (i == 0) {
                cursor = db.query("expendinfo", null, "User_Name=?",
                        new String[]{Username}, null, null, null);
            } else if (i == 1) {
                cursor = db.query("incomeinfo", null, "User_Name=?",
                        new String[]{Username}, null, null, null);
            }
            if (cursor.moveToFirst()) {
                do {
                    long time = cursor.getLong(cursor.getColumnIndex("Time"));
                    if ((start_time <= time) && (end_time >= time)) {//在规定的时间段内进行查找
                        String money = cursor.getString(cursor.getColumnIndex("Money"));
                        String type = i + cursor.getString(cursor.getColumnIndex("Type"));
                        String text = cursor.getString(cursor.getColumnIndex("Message"));
                        Info info = new Info(Double.valueOf(money), type, time, text);
                        InfoList.add(info);
                    }
                } while (cursor.moveToNext());
            }
        }
        } catch (Exception e) {

        } finally {
            cursor.close();
            db.close();
        }
        Collections.sort(InfoList);
        return InfoList;
    }

    public String[] Get_IandE() {
        DetailInfo();//得到数据
        double totalincomemoney = 0;
        double totalexpendmoney = 0;
        for (Info info : InfoList) {
            if (info.getType().substring(0, 1).equals("0")) {//支出
                totalexpendmoney += info.getMoney();
            } else if (info.getType().substring(0, 1).equals("1")) {//收入
                totalincomemoney += info.getMoney();
            }
        }
        String[] money = new String[]{DataFormatUtil.formatPrice(totalexpendmoney), DataFormatUtil.formatPrice(totalincomemoney)};
        return money;
    }

    /*
    天中的天结余
     */
    public List<DetailSurplus> Detailday_day() {
        day_dayList.clear();
        //获得天的结余
        double expendmoney = 0;
        double incomemoney = 0;
        String time;
        boolean createnew = true;
        for (int i = 0; i < InfoList.size(); i++) {
            if (i == 0) {
                time = LongToString(InfoList.get(0).getTime()).substring(0, 11);
            } else if (i != 0 && createnew) {
                time = LongToString(InfoList.get(i).getTime()).substring(0, 11);
            } else {
                time = LongToString(InfoList.get(i - 1).getTime()).substring(0, 11);
            }
            if (LongToString(InfoList.get(i).getTime()).substring(0, 11).equals(time)) {//如果是同一天
                if (InfoList.get(i).getType().substring(0, 1).equals("0")) {//判断是为支出
                    expendmoney = expendmoney + InfoList.get(i).getMoney();
                } else if (InfoList.get(i).getType().substring(0, 1).equals("1")) {//判断是为收入
                    incomemoney = incomemoney + InfoList.get(i).getMoney();
                }
                createnew = false;
                if (i == (InfoList.size() - 1)) {
                    String day = LongToString(InfoList.get(i).getTime()).substring(8, 11);
                    String date = LongToString(InfoList.get(i).getTime()).substring(0, 4) + "." +
                            LongToString(InfoList.get(i).getTime()).substring(5, 7);
                    double jieyu = incomemoney - expendmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = InfoList.get(i).getTime();
                    DetailSurplus detailday_day = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    day_dayList.add(detailday_day);
                }
            } else {//总结出一个
                String day = LongToString(InfoList.get(i - 1).getTime()).substring(8, 11);
                String date = LongToString(InfoList.get(i - 1).getTime()).substring(0, 4) + "." +
                        LongToString(InfoList.get(i - 1).getTime()).substring(5, 7);
                double jieyu = incomemoney - expendmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = InfoList.get(i - 1).getTime();
                DetailSurplus detailday_day = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                day_dayList.add(detailday_day);
                incomemoney = 0;
                expendmoney = 0;
                i = i - 1;
                createnew = true;
            }
        }
        return day_dayList;
    }

    /*
    天中的月结余
     */
    public List<DetailSurplus> Detailday_month() {
        day_monthList.clear();
        //获得月的结余
        double expendmoney = 0;
        double incomemoney = 0;
        double totalmoney = 0;
        String time;
        boolean createnew = true;
        for (int i = 0; i < day_dayList.size(); i++) {
            if (i == 0) {
                time = day_dayList.get(0).getDate().substring(5, 7);
            } else if (i != 0 && createnew) {
                time = day_dayList.get(i).getDate().substring(5, 7);
            } else {
                time = day_dayList.get(i - 1).getDate().substring(5, 7);
            }
            if (day_dayList.get(i).getDate().substring(5, 7).equals(time)) {//如果是同一月份
                expendmoney = expendmoney + day_dayList.get(i).getZhichu();
                incomemoney = incomemoney + day_dayList.get(i).getShouru();
                totalmoney = totalmoney + day_dayList.get(i).getJieyu();
                createnew = false;
                if (i == (day_dayList.size() - 1)) {
                    String day = day_dayList.get(i).getDate().substring(5, 7) + "月";
                    String date = day_dayList.get(i).getDate().substring(0, 4);
                    double jieyu = totalmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = day_dayList.get(i).getTime();
                    DetailSurplus detailday_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    day_monthList.add(detailday_month);
                }
            } else {//总结出一个
                String day = day_dayList.get(i - 1).getDate().substring(5, 7) + "月";//LongToString(day_dayList.get(i - 1).getTime()).substring(8, 11);
                String date = day_dayList.get(i - 1).getDate().substring(0, 4);
                double jieyu = totalmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = day_dayList.get(i - 1).getTime();
                DetailSurplus detailday_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                day_monthList.add(detailday_month);
                incomemoney = 0;
                expendmoney = 0;
                totalmoney = 0;
                i = i - 1;
                createnew = true;
            }
        }
        return day_monthList;
    }

    /*
    月中月结余同时也是季中的月结余
     */
    public List<DetailSurplus> Detailmonth_month() {
        month_monthList.clear();
        //获得月的结余
        double expendmoney = 0;
        double incomemoney = 0;
        String time;
        boolean createnew = true;
        for (int i = 0; i < InfoList.size(); i++) {
            if (i == 0) {
                time = LongToString(InfoList.get(0).getTime()).substring(5, 7);
            } else if (i != 0 && createnew) {
                time = LongToString(InfoList.get(i).getTime()).substring(5, 7);
            } else {
                time = LongToString(InfoList.get(i - 1).getTime()).substring(5, 7);
            }
            if (LongToString(InfoList.get(i).getTime()).substring(5, 7).equals(time)) {//如果是同一月
                if (InfoList.get(i).getType().substring(0, 1).equals("0")) {//判断是为支出
                    expendmoney = expendmoney + InfoList.get(i).getMoney();
                } else if (InfoList.get(i).getType().substring(0, 1).equals("1")) {//判断是为收入
                    incomemoney = incomemoney + InfoList.get(i).getMoney();
                }
                createnew = false;
                if (i == (InfoList.size() - 1)) {
                    String day = LongToString(InfoList.get(i).getTime()).substring(5, 8);
                    String date = LongToString(InfoList.get(i).getTime()).substring(0, 4);
                    double jieyu = incomemoney - expendmoney;
                    double shouru = incomemoney;
                    double zhichu = expendmoney;
                    long lasttime = InfoList.get(i).getTime();
                    DetailSurplus detailmonth_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                    month_monthList.add(detailmonth_month);
                }
            } else {//总结出一个
                String day = LongToString(InfoList.get(i - 1).getTime()).substring(5, 8);
                String date = LongToString(InfoList.get(i - 1).getTime()).substring(0, 4);
                double jieyu = incomemoney - expendmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = InfoList.get(i - 1).getTime();
                DetailSurplus detailmonth_month = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                month_monthList.add(detailmonth_month);
                incomemoney = 0;
                expendmoney = 0;
                i = i - 1;
                createnew = true;
            }
        }
        return month_monthList;
    }

    /*
    季中的季结余
     */
    public List<DetailSurplus> Detailseason_season() {
        season_seasonList.clear();
        double expendmoney = 0;
        double incomemoney = 0;
        double totalmoney = 0;
        int i = 0;//month_monthList列表的指针位置
        boolean change = false;
        int biaoshifu = 0;
        String day = "";
        while (i < month_monthList.size()) {
            switch (Integer.valueOf(month_monthList.get(i).getDay().substring(0, 2))) {
                case 12:
                case 11:
                case 10:
                    day = "4季";
                    biaoshifu = 4;
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        change = true;
                    } else {
                        change = false;
                    }
                    i++;
                    break;
                case 9:
                case 8:
                case 7:
                    if (biaoshifu != 3) {
                        if (biaoshifu == 0) {
                            biaoshifu = 3;
                        } else {
                            biaoshifu = 3;
                            change = true;
                            break;
                        }
                    }
                    day = "3季";
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        change = true;
                    } else {
                        change = false;
                    }
                    i++;
                    break;
                case 6:
                case 5:
                case 4:
                    if (biaoshifu != 2) {
                        if (biaoshifu == 0) {
                            biaoshifu = 2;
                        } else {
                            biaoshifu = 2;
                            change = true;
                            break;
                        }
                    }
                    day = "2季";
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        change = true;
                    } else {
                        change = false;
                    }
                    i++;
                    break;
                case 3:
                case 2:
                case 1:
                    if (biaoshifu != 1) {
                        if (biaoshifu == 0) {
                            biaoshifu = 1;
                        } else {
                            biaoshifu = 1;
                            change = true;
                            break;
                        }
                    }
                    day = "1季";
                    expendmoney = expendmoney + month_monthList.get(i).getZhichu();
                    incomemoney = incomemoney + month_monthList.get(i).getShouru();
                    totalmoney = totalmoney + month_monthList.get(i).getJieyu();
                    if (i == (month_monthList.size() - 1)) {
                        change = true;
                    } else {
                        change = false;
                    }
                    i++;
                    break;
            }
            if (change == true) {
                String date = month_monthList.get(i - 1).getDate();
                double jieyu = totalmoney;
                double shouru = incomemoney;
                double zhichu = expendmoney;
                long lasttime = month_monthList.get(i - 1).getTime();
                DetailSurplus detailseason_season = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
                season_seasonList.add(detailseason_season);
                incomemoney = 0;
                expendmoney = 0;
                totalmoney = 0;
            }
        }
        return season_seasonList;
    }

    /*
    年中的年结余  就是按一年来算的
     */
    public List<DetailSurplus> Detailyear_year() {
        year_yearList.clear();
        //获得年的结余
        double expendmoney = 0;
        double incomemoney = 0;
        double totalmoney = 0;
        for (int i = 0; i < month_monthList.size(); i++) {
            expendmoney = expendmoney + month_monthList.get(i).getZhichu();
            incomemoney = incomemoney + month_monthList.get(i).getShouru();
            totalmoney = totalmoney + month_monthList.get(i).getJieyu();
        }
        String day = LongToString(start_time).substring(0, 4);
        String date = "";
        double jieyu = totalmoney;
        double shouru = incomemoney;
        double zhichu = expendmoney;
        long lasttime = start_time;
        DetailSurplus detailyear_year = new DetailSurplus(day, date, jieyu, shouru, zhichu, lasttime);
        year_yearList.add(detailyear_year);
        return year_yearList;
    }

    /*
   季列表
    */
    public List<Node> Detailseason_list() {
        list.clear();
        DetailInfo();
        Detailmonth_month();
        Detailseason_season();
        //开始综合成list，实现流水列表
        NodeData nodeData;
        int a, b = 0, c = 0;//a为季，b为月，c为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01, leveldivide12;//等级划分关联
        for (a = 0; a < season_seasonList.size(); a++) {

            nodeData = new NodeData(season_seasonList.get(a).getDay(), season_seasonList.get(a).getDate(),
                    DataFormatUtil.formatPrice(season_seasonList.get(a).getJieyu()), DataFormatUtil.formatPrice(season_seasonList.get(a).getShouru()),
                    DataFormatUtil.formatPrice(season_seasonList.get(a).getZhichu()), "",
                    season_seasonList.get(a).getTime());
            list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
            leveldivide01 = i;
            i++;
            int ji = Integer.valueOf(season_seasonList.get(a).getDay().substring(0, 1));
            while ((Integer.valueOf(month_monthList.get(b).getDay().substring(0, 2)) <= (ji * 3))
                    && (Integer.valueOf(month_monthList.get(b).getDay().substring(0, 2)) >= (ji * 3 - 2))) {

                nodeData = new NodeData(month_monthList.get(b).getDay(), month_monthList.get(b).getDate(),
                        DataFormatUtil.formatPrice(month_monthList.get(b).getJieyu()), DataFormatUtil.formatPrice(month_monthList.get(b).getShouru()),
                        DataFormatUtil.formatPrice(month_monthList.get(b).getZhichu()), "",
                        month_monthList.get(b).getTime());
                list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "0"));
                leveldivide12 = i;
                i++;
                while (month_monthList.get(b).getDay().equals(LongToString(InfoList.get(c).getTime()).substring(5, 8))) {

                    nodeData = new NodeData(LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(18), InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(12, 17), DataFormatUtil.formatPrice(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText(), InfoList.get(c).getTime());
                    list.add(new Node<NodeData>(i + "", leveldivide12 + "", nodeData, "1"));
                    i++;
                    c++;
                    if (c >= InfoList.size()) {
                        break;
                    }
                }
                b++;
                if (b >= month_monthList.size()) {
                    break;
                }
            }
        }
        return list;
    }

    /*
   年列表
    */
    public List<Node> Detailyear_list() {
        //开始综合list，实现流水列表
        NodeData nodeData;
        int a, b = 0, c = 0;//a为月，b为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01;//等级划分关联
        list.clear();
        DetailInfo();
        Detailmonth_month();
        Detailyear_year();
        if (!month_monthList.isEmpty()) {
            nodeData = new NodeData(year_yearList.get(0).getDay(), year_yearList.get(0).getDate(),
                    DataFormatUtil.formatPrice(year_yearList.get(0).getJieyu()), DataFormatUtil.formatPrice(year_yearList.get(0).getShouru()),
                    DataFormatUtil.formatPrice(year_yearList.get(0).getZhichu()), "",
                    year_yearList.get(0).getTime());
            list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
            i++;
        }

        for (a = 0; a < month_monthList.size(); a++) {

            nodeData = new NodeData(month_monthList.get(a).getDay(), month_monthList.get(a).getDate(),
                    DataFormatUtil.formatPrice(month_monthList.get(a).getJieyu()), DataFormatUtil.formatPrice(month_monthList.get(a).getShouru()),
                    DataFormatUtil.formatPrice(month_monthList.get(a).getZhichu()), "",
                    month_monthList.get(a).getTime());
            list.add(new Node<NodeData>(i + "", "0", nodeData, "1"));
            leveldivide01 = i;
            i++;
            while (month_monthList.get(a).getDay().equals(LongToString(InfoList.get(b).getTime()).substring(5, 8))) {

                nodeData = new NodeData(LongToString(InfoList.get(b).getTime()).substring(8, 10),
                        LongToString(InfoList.get(b).getTime()).substring(18), InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(12, 17), DataFormatUtil.formatPrice(InfoList.get(b).getMoney()),
                        InfoList.get(b).getText(), InfoList.get(b).getTime());
                list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "2"));
                i++;
                b++;
                if (b >= InfoList.size()) {
                    break;
                }
            }
        }
        return list;
    }

    /*
    月列表
     */
    public List<Node> Detailmonth_list() {
        list.clear();
        DetailInfo();
        Detailmonth_month();
        //开始综合list，实现流水列表
        NodeData nodeData;
        int a, b = 0, c = 0;//a为月，b为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01;//等级划分关联
        for (a = 0; a < month_monthList.size(); a++) {

            nodeData = new NodeData(month_monthList.get(a).getDay(), month_monthList.get(a).getDate(),
                    DataFormatUtil.formatPrice(month_monthList.get(a).getJieyu()), DataFormatUtil.formatPrice(month_monthList.get(a).getShouru()),
                    DataFormatUtil.formatPrice(month_monthList.get(a).getZhichu()), "",
                    month_monthList.get(a).getTime());
            list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
            leveldivide01 = i;
            i++;
            while (month_monthList.get(a).getDay().equals(LongToString(InfoList.get(b).getTime()).substring(5, 8))) {

                nodeData = new NodeData(LongToString(InfoList.get(b).getTime()).substring(8, 10),
                        LongToString(InfoList.get(b).getTime()).substring(18), InfoList.get(b).getType(),
                        LongToString(InfoList.get(b).getTime()).substring(12, 17), DataFormatUtil.formatPrice(InfoList.get(b).getMoney()),
                        InfoList.get(b).getText(), InfoList.get(b).getTime());
                list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "1"));
                i++;
                b++;
                if (b >= InfoList.size()) {
                    break;
                }
            }
        }
        return list;
    }

    /*
    天列表
     */
    public List<Node> Detailday_list() {
        Log.d("DetailActivity.liang", "进入detailday_list");
        list.clear();
        DetailInfo();
        Detailday_day();
        Detailday_month();
        //开始进行将三个day_monthList+day_dayList+InfoList进行综合，综合成list，实现流水列表
        NodeData nodeData;
        int a, b = 0, c = 0;//a为月，b为日，c为日详情
        int i = 0;//为指针在list中的位置
        int leveldivide01, leveldivide12;//等级划分关联listisExpand[i]
        for (a = 0; a < day_monthList.size(); a++) {

            nodeData = new NodeData(day_monthList.get(a).getDay(), day_monthList.get(a).getDate(),
                    DataFormatUtil.formatPrice(day_monthList.get(a).getJieyu()), DataFormatUtil.formatPrice(day_monthList.get(a).getShouru()),
                    DataFormatUtil.formatPrice(day_monthList.get(a).getZhichu()), "",
                    day_monthList.get(a).getTime());
            list.add(new Node<NodeData>(i + "", "-1", nodeData, "0"));
            leveldivide01 = i;
            i++;

            while (day_monthList.get(a).getDay().equals(day_dayList.get(b).getDate().substring(5, 7) + "月")) {

                nodeData = new NodeData(day_dayList.get(b).getDay(), day_dayList.get(b).getDate(),
                        DataFormatUtil.formatPrice(day_dayList.get(b).getJieyu()), DataFormatUtil.formatPrice(day_dayList.get(b).getShouru()),
                        DataFormatUtil.formatPrice(day_dayList.get(b).getZhichu()), "",
                        day_dayList.get(b).getTime());
                list.add(new Node<NodeData>(i + "", leveldivide01 + "", nodeData, "0"));
                leveldivide12 = i;
                i++;
                while (LongToString(day_dayList.get(b).getTime()).substring(0, 11).equals(
                        LongToString(InfoList.get(c).getTime()).substring(0, 11))) {

                    nodeData = new NodeData(LongToString(InfoList.get(c).getTime()).substring(8, 10),
                            LongToString(InfoList.get(c).getTime()).substring(18),
                            InfoList.get(c).getType(),
                            LongToString(InfoList.get(c).getTime()).substring(12, 17),
                            DataFormatUtil.formatPrice(InfoList.get(c).getMoney()),
                            InfoList.get(c).getText(),
                            InfoList.get(c).getTime());
                    list.add(new Node<NodeData>(i + "", leveldivide12 + "", nodeData, "1"));
                    i++;
                    c++;
                    if (c >= InfoList.size()) {
                        break;
                    }
                }
                b++;
                if (b >= day_dayList.size()) {
                    break;
                }
            }
        }
        return list;
    }


    public static String LongToString(long date) {
        Date dateOld = new Date(date); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = new SimpleDateFormat("yyyy年MM月dd日 HH:mm EE").format(dateOld);// 把date类型的时间转换为string
        return sDateTime;
    }


}
