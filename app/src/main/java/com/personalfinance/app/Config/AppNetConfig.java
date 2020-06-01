package com.personalfinance.app.Config;

public class AppNetConfig {
    public static String localhost="192.168.0.104:8080/PersonalFinance";
    //http://localhost:8080/PersonalFinance/RegisterServlet?User_Name=k&User_Password=p
    public static String Register="http://"+localhost+"/RegisterServlet";
    public static String Login="http://"+localhost+"/LoginServlet";
    public static String CloseAccount="http://"+localhost+"/CloseAccountServlet";
    public static String Data_syncCS="http://"+localhost+"/Client_ServerServlet";


    public static String FinanceProduct="http://"+localhost+"/FinanceProduct";
    public static String FinanceHold="http://"+localhost+"/FinanceHold";
    public static String FinanceRecord="http://"+localhost+"/FinanceRecord";
    public static String FinanceBuy="http://"+localhost+"/FinanceBuy";
    public static String FinanceSale="http://"+localhost+"/FinanceSale";

}
