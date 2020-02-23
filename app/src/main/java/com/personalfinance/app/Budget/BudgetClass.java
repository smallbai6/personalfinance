package com.personalfinance.app.Budget;

public class BudgetClass {
    private String type;//消费类型
    private String budgetmoney;//预算金额
    private String zyc;//支出余额超支
    private String resultmoney;//预算-总金额

    public BudgetClass(String type, String budgetmoney, String zyc, String resultmoney) {
        this.type = type;
        this.budgetmoney = budgetmoney;
        this.zyc = zyc;
        this.resultmoney = resultmoney;
    }

    public String getType() {
        return type;
    }

    public String getBudgetmoney() {
        return budgetmoney;
    }

    public String getZyc() {
        return zyc;
    }

    public String getResultmoney() {
        return resultmoney;
    }
}
