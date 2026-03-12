package com.example.jizhangsystem.dao;

import android.util.Log;
import com.example.jizhangsystem.bean.CategorySumBean;
import com.example.jizhangsystem.bean.DateSumBean;
import com.example.jizhangsystem.bean.Record;
import com.example.jizhangsystem.utils.JDBCUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDao {

    public boolean addRecord(Record record) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "INSERT INTO Records (user_id, amount, type_name, category, remark, record_date) VALUES (?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, record.getUserId());
            ps.setDouble(2, record.getAmount());
            ps.setString(3, record.getTypeName());
            ps.setString(4, record.getCategory());
            ps.setString(5, record.getRemark());
            ps.setString(6, record.getDate());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; } finally { JDBCUtils.close(conn); }
    }

    public boolean updateRecord(Record record) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            String sql = "UPDATE Records SET amount=?, type_name=?, category=?, remark=?, record_date=? WHERE record_id=?";
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, record.getAmount());
            ps.setString(2, record.getTypeName());
            ps.setString(3, record.getCategory());
            ps.setString(4, record.getRemark());
            ps.setString(5, record.getDate());
            ps.setInt(6, record.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; } finally { JDBCUtils.close(conn); }
    }

    public List<Record> queryRecordsByMonth(int userId, String yearMonth) {
        List<Record> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return list;
            String[] parts = yearMonth.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            String sql = "SELECT * FROM Records WHERE user_id = ? AND YEAR(record_date) = ? AND MONTH(record_date) = ? ORDER BY record_date DESC";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            rs = ps.executeQuery();
            while (rs.next()) {
                Record r = new Record();
                r.setId(rs.getInt("record_id"));
                r.setUserId(rs.getInt("user_id"));
                r.setAmount(rs.getDouble("amount"));
                r.setTypeName(rs.getString("type_name"));
                r.setCategory(rs.getString("category"));
                r.setRemark(rs.getString("remark"));

                // ★ 修正1：主页列表日期格式化
                String rawDate = rs.getString("record_date");
                if (rawDate != null && rawDate.length() >= 10) {
                    r.setDate(rawDate.substring(0, 10));
                } else {
                    r.setDate(rawDate);
                }

                list.add(r);
            }
        } catch (Exception e) { e.printStackTrace(); } finally { JDBCUtils.close(conn); }
        return list;
    }

    public boolean deleteRecord(int id) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return false;
            ps = conn.prepareStatement("DELETE FROM Records WHERE record_id = ?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; } finally { JDBCUtils.close(conn); }
    }

    /*** ★ 修正：报表分类统计逻辑 ★ ***/
    public List<CategorySumBean> getCategorySumsFiltered(int userId, String mode, String selectedTime) {
        List<CategorySumBean> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return list;

            String sql = "";
            if ("WEEK".equals(mode)) {
                sql = "SELECT category, SUM(amount) as total FROM Records WHERE user_id = ? AND type_name = '支出' AND record_date BETWEEN DATEADD(day, -6, ?) AND ? GROUP BY category";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, selectedTime);
                ps.setString(3, selectedTime);
            } else if ("MONTH".equals(mode)) {
                String[] p = selectedTime.split("-");
                sql = "SELECT category, SUM(amount) as total FROM Records WHERE user_id = ? AND type_name = '支出' AND YEAR(record_date) = ? AND MONTH(record_date) = ? GROUP BY category";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, Integer.parseInt(p[0]));
                ps.setInt(3, Integer.parseInt(p[1]));
            } else { // YEAR
                sql = "SELECT category, SUM(amount) as total FROM Records WHERE user_id = ? AND type_name = '支出' AND YEAR(record_date) = ? GROUP BY category";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, Integer.parseInt(selectedTime));
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new CategorySumBean(rs.getString("category"), Math.abs(rs.getFloat("total"))));
            }
        } catch (Exception e) { e.printStackTrace(); } finally { JDBCUtils.close(conn); }
        return list;
    }

    /*** ★ 修正：报表趋势统计逻辑 ★ ***/
    public List<DateSumBean> getDateSumsFiltered(int userId, String mode, String selectedTime) {
        List<DateSumBean> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return list;

            String sql = "";
            if ("WEEK".equals(mode)) {
                sql = "SELECT FORMAT(record_date, 'MM-dd') as d, SUM(amount) as t FROM Records WHERE user_id = ? AND type_name = '支出' AND record_date BETWEEN DATEADD(day, -6, ?) AND ? GROUP BY FORMAT(record_date, 'MM-dd') ORDER BY d ASC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, selectedTime);
                ps.setString(3, selectedTime);
            } else if ("MONTH".equals(mode)) {
                String[] p = selectedTime.split("-");
                sql = "SELECT DAY(record_date) as d, SUM(amount) as t FROM Records WHERE user_id = ? AND type_name = '支出' AND YEAR(record_date) = ? AND MONTH(record_date) = ? GROUP BY DAY(record_date) ORDER BY d ASC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, Integer.parseInt(p[0]));
                ps.setInt(3, Integer.parseInt(p[1]));
            } else { // YEAR
                sql = "SELECT MONTH(record_date) as d, SUM(amount) as t FROM Records WHERE user_id = ? AND type_name = '支出' AND YEAR(record_date) = ? GROUP BY MONTH(record_date) ORDER BY d ASC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, Integer.parseInt(selectedTime));
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new DateSumBean(rs.getString("d"), Math.abs(rs.getFloat("t"))));
            }
        } catch (Exception e) { e.printStackTrace(); } finally { JDBCUtils.close(conn); }
        return list;
    }

    public List<Record> queryRecordsForExport(int userId, String mode, String selectedTime) {
        List<Record> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = JDBCUtils.getConn();
            if (conn == null) return list;

            String sql = "";
            if ("WEEK".equals(mode)) {
                sql = "SELECT * FROM Records WHERE user_id = ? AND record_date BETWEEN DATEADD(day, -6, ?) AND ? ORDER BY record_date ASC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setString(2, selectedTime);
                ps.setString(3, selectedTime);
            } else if ("MONTH".equals(mode)) {
                String[] p = selectedTime.split("-");
                sql = "SELECT * FROM Records WHERE user_id = ? AND YEAR(record_date) = ? AND MONTH(record_date) = ? ORDER BY record_date ASC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, Integer.parseInt(p[0]));
                ps.setInt(3, Integer.parseInt(p[1]));
            } else { // YEAR
                sql = "SELECT * FROM Records WHERE user_id = ? AND YEAR(record_date) = ? ORDER BY record_date ASC";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, userId);
                ps.setInt(2, Integer.parseInt(selectedTime));
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Record r = new Record();

                // ★ 修正2：导出表格日期格式化
                String rawDate = rs.getString("record_date");
                if (rawDate != null && rawDate.length() >= 10) {
                    r.setDate(rawDate.substring(0, 10)); // 只取 yyyy-MM-dd
                } else {
                    r.setDate(rawDate);
                }

                r.setTypeName(rs.getString("type_name"));
                r.setCategory(rs.getString("category"));
                r.setAmount(rs.getDouble("amount"));
                r.setRemark(rs.getString("remark"));
                list.add(r);
            }
        } catch (Exception e) { e.printStackTrace(); } finally { JDBCUtils.close(conn); }
        return list;
    }
}