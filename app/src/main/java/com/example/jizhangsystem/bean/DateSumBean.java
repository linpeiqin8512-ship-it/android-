package com.example.jizhangsystem.bean;

public class DateSumBean {
    private String date;   // 日期 (例如 "2025-11-28")
    private float amount;  // 当天总支出

    public DateSumBean(String date, float amount) {
        this.date = date;
        this.amount = amount;
    }
    public String getDate() { return date; }
    public float getAmount() { return amount; }
}