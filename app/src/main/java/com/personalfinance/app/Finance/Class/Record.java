package com.personalfinance.app.Finance.Class;

public class Record {
    private String Buy_Sale;
    private String Product_Name;
    private String Time;
    private String M_Q_S;

   // private String User_Number;
  //  private String Product_Number;
    private String Order_Number;
    private String Sure_Status;

    public Record(String Buy_Sale, String Product_Name, String Time, String M_Q_S, String Order_Number,String Sure_Status) {
        this.Buy_Sale = Buy_Sale;
        this.Product_Name = Product_Name;
        this.Time = Time;
        this.M_Q_S = M_Q_S;
       // this.User_Number = User_Number;
       // this.Product_Number = Product_Number;
        this.Order_Number=Order_Number;
        this.Sure_Status=Sure_Status;
    }

    public String getBuy_Sale() {
        return Buy_Sale;
    }

    public String getProduct_Name() {
        return Product_Name;
    }

    public String getTime() {
        return Time;
    }

    public String getM_Q_S() {
        return M_Q_S;

    }

    public String getOrder_Number(){
        return Order_Number;
    }
   /* public String getUser_Number() {
        return User_Number;
    }

    public String getProduct_Number() {
        return Product_Number;
    }*/

   public String getSure_Status(){
       return Sure_Status;
   }

    public void setBuy_Sale() {
        this.Buy_Sale = Buy_Sale;
    }

    public void setProduct_Name() {
        this.Product_Name = Product_Name;
    }

    public void setTime() {
        this.Time = Time;
    }

    public void setM_Q_S() {
        this.M_Q_S = M_Q_S;
    }


  /*  public void setUser_Number() {
        this.User_Number = User_Number;
    }

    public void setProduct_Number() {
        this.Product_Number = Product_Number;
    }*/
  public void setOrder_Number(){
      this.Order_Number=Order_Number;
  }
  public void setSure_Status(){
      this.Sure_Status=Sure_Status;
  }
}
