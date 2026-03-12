package com.example.jizhangsystem.bean;

public class User {
    private int id;
    private String username;
    private String password;
    private String nickname;
    private int role; // 0=管理员, 1=父母, 2=小孩 三类人员
    private int parentId;
    private double monthlyBudget; // ★ 新增：每月预算

    public User() {}

    public User(int id, String username, String password, String nickname, int role, int parentId, double monthlyBudget) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.parentId = parentId;
        this.monthlyBudget = monthlyBudget;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    // ★ Getter & Setter
    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }
}