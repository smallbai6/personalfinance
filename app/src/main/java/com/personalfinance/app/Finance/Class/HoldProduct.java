package com.personalfinance.app.Finance.Class;

import android.graphics.drawable.Drawable;

public class HoldProduct {
    private Drawable Picture;
    private String Company, Product_Name, Money, Yesterday_income, Sum_income;
    private String User_Number, Product_Number;

    public HoldProduct(Drawable Picture, String Company, String Product_Name, String Money, String Yesterday_income
            , String Sum_income, String User_Number, String Product_Number) {
        this.Picture = Picture;
        this.Company = Company;
        this.Product_Name = Product_Name;
        this.Money = Money;
        this.Yesterday_income = Yesterday_income;
        this.Sum_income = Sum_income;
        this.User_Number = User_Number;
        this.Product_Number = Product_Number;
    }

    public Drawable getPicture() {
        return Picture;
    }

    public String getCompany() {
        return Company;
    }

    public String getProduct_Name() {
        return Product_Name;
    }

    public String getMoney() {
        return Money;
    }

    public String getYesterday_income() {
        return Yesterday_income;
    }

    public String getSum_income() {
        return Sum_income;
    }

    public String getUser_Number() {
        return User_Number;
    }

    public String getProduct_Number() {
        return Product_Number;
    }

    public void setPicture() {
        this.Picture = Picture;
    }

    public void setCompany() {
        this.Company = Company;
    }

    public void setProduct_Name() {
        this.Product_Name = Product_Name;
    }

    public void setMoney() {
        this.Money = Money;
    }

    public void setYesterday_income() {
        this.Yesterday_income = Yesterday_income;
    }

    public void setSum_income() {
        this.Sum_income = Sum_income;
    }

    public void setUser_Number() {
        this.User_Number = User_Number;
    }

    public void setProduct_Number() {
        this.Product_Number = Product_Number;
    }
}
