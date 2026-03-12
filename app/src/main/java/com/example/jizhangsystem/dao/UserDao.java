package com.example.jizhangsystem.dao;

import android.util.Log;
import com.example.jizhangsystem.bean.User;
import com.example.jizhangsystem.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // 登录方法 - 修改：增加了读取 monthly_budget
    public User login(String username, String password) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return null;

            String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setNickname(rs.getString("nickname"));
                user.setRole(rs.getInt("role"));
                user.setParentId(rs.getInt("parent_id"));
                user.setMonthlyBudget(rs.getDouble("monthly_budget")); // ★ 读取预算
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UserDao", "登录查询异常: " + e.getMessage());
        } finally {
            JDBCUtils.close(conn);
        }
        return user;
    }

    /**
     * ★ 新增：更新用户预算
     */
    public boolean updateBudget(int userId, double budget) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "UPDATE Users SET monthly_budget = ? WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, budget);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            JDBCUtils.close(conn);
        }
    }

    /**
     * ★ 新增：获取用户当前预算
     */
    public double getBudget(int userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        double budget = 0;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return 0;
            String sql = "SELECT monthly_budget FROM Users WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                budget = rs.getDouble("monthly_budget");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn);
        }
        return budget;
    }

    // 注册方法 (保持不变)
    public boolean register(User user) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "INSERT INTO Users (username, password, nickname, role, parent_id) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNickname());
            ps.setInt(4, user.getRole());
            ps.setInt(5, 0);
            int rows = ps.executeUpdate();
            if (rows > 0) isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UserDao", "注册异常: " + e.getMessage());
        } finally {
            JDBCUtils.close(conn);
        }
        return isSuccess;
    }

    // 查找父母ID (保持不变)
    public int getUserIdByUsername(String username) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = 0;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return 0;
            String sql = "SELECT user_id FROM Users WHERE username = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) { id = rs.getInt("user_id"); }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn);
        }
        return id;
    }

    // 绑定父母 (保持不变)
    public boolean bindParent(int childId, int parentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "UPDATE Users SET parent_id = ? WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, parentId);
            ps.setInt(2, childId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            JDBCUtils.close(conn);
        }
    }

    public boolean addChild(User child, int parentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isSuccess = false;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "INSERT INTO Users (username, password, nickname, role, parent_id) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, child.getUsername());
            ps.setString(2, child.getPassword());
            ps.setString(3, child.getNickname());
            ps.setInt(4, 2);
            ps.setInt(5, parentId);
            int rows = ps.executeUpdate();
            if (rows > 0) isSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UserDao", "添加子账户失败: " + e.getMessage());
        } finally {
            JDBCUtils.close(conn);
        }
        return isSuccess;
    }

    public List<User> getChildren(int parentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return list;
            String sql = "SELECT user_id, nickname FROM Users WHERE parent_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, parentId);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("user_id"));
                u.setNickname(rs.getString("nickname"));
                list.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn);
        }
        return list;
    }
    public boolean updateNickname(int userId, String newNickname) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        try {
            conn = com.example.jizhangsystem.utils.JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "UPDATE Users SET nickname = ? WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newNickname);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            com.example.jizhangsystem.utils.JDBCUtils.close(conn);
        }
    }
    /**
     * 管理员专用：获取系统所有用户
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        java.sql.ResultSet rs = null;
        try {
            conn = com.example.jizhangsystem.utils.JDBCUtils.getConn();
            if (conn == null) return list;
            String sql = "SELECT user_id, nickname, role FROM Users";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("user_id"));
                String roleName = rs.getInt("role") == 1 ? " (家长)" : (rs.getInt("role") == 2 ? " (子女)" : " (管理员)");
                u.setNickname(rs.getString("nickname") + roleName);
                list.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); } finally { com.example.jizhangsystem.utils.JDBCUtils.close(conn); }
        return list;
    }
    public boolean adminUpdateUser(int targetUserId, String newPass, String newNick) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement ps = null;
        try {
            conn = com.example.jizhangsystem.utils.JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "UPDATE Users SET password = ?, nickname = ? WHERE user_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPass);
            ps.setString(2, newNick);
            ps.setInt(3, targetUserId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; } finally { com.example.jizhangsystem.utils.JDBCUtils.close(conn); }
    }
}