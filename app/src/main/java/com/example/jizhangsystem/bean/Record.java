package com.example.jizhangsystem.bean;

public class Record {
    private int id;             // 对应 record_id
    private int userId;         // 对应 user_id (谁记的账)
    private double amount;      // 对应 amount (金额)
    private String typeName;    // 对应 type_name (支出/收入)
    private String category;    // 对应 category (餐饮/交通/工资等)
    private String remark;      // 对应 remark (备注)
    private String date;        // 对应 record_date (时间，为了显示方便，我们用 String 存)

    // 无参构造
    public Record() {
    }

    // 全参构造
    public Record(int id, int userId, double amount, String typeName, String category, String remark, String date) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.typeName = typeName;
        this.category = category;
        this.remark = remark;
        this.date = date;
    }

    // Getter & Setter (用于读取和设置数据)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}