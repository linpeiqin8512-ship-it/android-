package com.example.jizhangsystem.bean;

public class CategorySumBean {
    private String category;   // 分类名称（比如：餐饮）
    private float totalAmount; // 总金额（比如：500.0）

    public CategorySumBean(String category, float totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public String getCategory() { return category; }
    public float getTotalAmount() { return totalAmount; }
}