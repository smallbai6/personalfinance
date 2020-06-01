package com.personalfinance.app.Finance.Class;

import android.graphics.drawable.Drawable;

/**
 * 产品显示的类
 */
public class Product {
    private Drawable Picture;
    private String Product_Name;
    private String Yield;
    private String Purchase_Amount;
    private String Product_Number;
    private String User_Number;

    public Product(Drawable Picture, String Product_Name, String Yield
            , String Purchase_Amount, String Product_Number, String User_Number) {
        this.Picture = Picture;
        this.Product_Name = Product_Name;
        this.Yield = Yield;
        this.Purchase_Amount = Purchase_Amount;
        this.Product_Number = Product_Number;
        this.User_Number = User_Number;
    }

    public Drawable getPicture() {
        return Picture;
    }

    public String getProduct_Name() {
        return Product_Name;
    }

    public String getYield() {
        return Yield;
    }

    public String getPurchase_Amount() {
        return Purchase_Amount;
    }

    public String getProduct_Number() {
        return Product_Number;
    }

    public String getUser_Number() {
        return User_Number;
    }

    public void setPicture() {
        this.Picture = Picture;
    }

    public void setProduct_Name() {
        this.Product_Name = Product_Name;
    }

    public void setYield() {
        this.Yield = Yield;
    }

    public void setPurchase_Amount() {
        this.Purchase_Amount = Purchase_Amount;
    }

    public void setProduct_Number() {
        this.Product_Number = Product_Number;
    }

    public void setUser_Number() {
        this.User_Number = User_Number;
    }
}
