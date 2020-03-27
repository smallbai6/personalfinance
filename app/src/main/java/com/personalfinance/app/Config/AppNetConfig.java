package com.personalfinance.app.Config;

public class AppNetConfig {
    public static String localhost="192.168.2.101:8080/PersonalFinance";
    //http://localhost:8080/PersonalFinance/RegisterServlet?User_Name=k&User_Password=p
    public static String Register="http://"+localhost+"/RegisterServlet";
    public static String Login="http://"+localhost+"/LoginServlet";
    public static String CloseAccount="http://"+localhost+"/CloseAccountServlet";
    public static String Data_syncCS="http://"+localhost+"/Client_ServerServlet";
}
