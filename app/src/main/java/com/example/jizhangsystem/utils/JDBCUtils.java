package com.example.jizhangsystem.utils;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtils
{
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";
    private static final String IP = "15.tcp.cpolar.top";
    private static final String PORT = "11573";
    private static final String DB_NAME = "AccountBookDB";
    public static Connection getConn() {
        Connection conn = null;
        try {
            // 加载 jtds 驱动
            Class.forName("net.sourceforge.jtds.jdbc.Driver");

            // 拼接连接字符串
            String url = "jdbc:jtds:sqlserver://" + IP + ":" + PORT + "/" + DB_NAME;

            // 获取连接
            conn = DriverManager.getConnection(url, USER, PASSWORD);
            Log.d("JDBC_TEST", "恭喜！数据库连接成功！");

        } catch (ClassNotFoundException e) {
            Log.e("JDBC_TEST", "失败：找不到驱动类，检查 build.gradle 是否加了 jtds 依赖", e);
        } catch (SQLException e) {
            Log.e("JDBC_TEST", "失败：连接数据库报错，请检查 IP、防火墙或密码", e);
        }
        return conn;
    }

    // 关闭连接的方法
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
