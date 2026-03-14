package com.example.jizhangsystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据库连接工具。配置只来自 Gist 拉取，不在代码里写死端口。
 * 你只需在 Gist 里改 db_host / db_port，App 打开时自动拉取，无需改 JDBCUtils。
 */
public class JDBCUtils {

    private static final String PREFS_NAME = "db_config";
    private static final String KEY_HOST = "db_host";
    private static final String KEY_PORT = "db_port";
    private static final String KEY_DB_NAME = "db_name";
    private static final String KEY_USER = "db_user";
    private static final String KEY_PASSWORD = "db_password";
    private static final String KEY_CONFIG_URL = "db_config_url";

    private static final String DEFAULT_DB_NAME = "AccountBookDB";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "123456";

    /** 固定 Gist Raw 地址（取最新内容，无需带 commit hash）。只改 Gist 里的 db_host/db_port 即可 */
    public static final String DEFAULT_CONFIG_URL = "https://gist.githubusercontent.com/linpeiqin8512-ship-it/ce6d236c694b44d79e108ef85fa54a16/raw/db_config.json";

    private static Context appContext;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    /** 在 Application 或 MainActivity.onCreate 里调用一次，传入 getApplicationContext() */
    public static void init(Context context) {
        if (context != null) appContext = context.getApplicationContext();
    }

    /** 设置「配置 URL」：App 启动时会后台拉取该 URL 的 JSON 并覆盖本地配置 */
    public static void setConfigUrl(String url) {
        if (appContext == null) return;
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_CONFIG_URL, url).apply();
    }

    public static String getConfigUrl() {
        if (appContext == null) return null;
        return appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_CONFIG_URL, null);
    }

    /** 后台拉取配置 URL 的 JSON 并保存；拉取成功后 getConn() 会使用新配置 */
    public static void fetchConfigFromUrlAsync(String configUrl, Runnable onSuccess, Runnable onFailure) {
        executor.execute(() -> {
            try {
                URL url = new URL(configUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                int code = conn.getResponseCode();
                if (code != 200) {
                    if (onFailure != null) runOnMain(onFailure);
                    return;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                String json = sb.toString();
                JSONObject o = new JSONObject(json);
                String host = o.optString("db_host", o.optString("host", "")).trim();
                int port = o.optInt("db_port", o.optInt("port", 0));
                String dbName = o.optString("dbName", "").trim();
                String user = o.optString("user", "").trim();
                String password = o.optString("password", "").trim();
                if (host.isEmpty() || port <= 0) {
                    if (onFailure != null) runOnMain(onFailure);
                    return;
                }
                if (dbName.isEmpty()) dbName = DEFAULT_DB_NAME;
                saveConfig(host, String.valueOf(port), dbName, user.isEmpty() ? DEFAULT_USER : user, password.isEmpty() ? DEFAULT_PASSWORD : password);
                Log.i("JDBCUtils", "配置已从 Gist 保存: " + host + ":" + port);
                if (onSuccess != null) runOnMain(onSuccess);
            } catch (Exception e) {
                Log.e("JDBCUtils", "fetchConfigFromUrl failed", e);
                if (onFailure != null) runOnMain(onFailure);
            }
        });
    }

    private static void runOnMain(Runnable r) {
        if (appContext == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(r);
    }

    /** 当前保存的 host:port，用于界面提示（如「配置已就绪：15.tcp.cpolar.top:13039」） */
    public static String getCurrentHostPort() {
        String h = getHost();
        String p = getPort();
        return (h != null && p != null) ? (h + ":" + p) : "";
    }

    /** 是否已有从 Gist 拉取并保存的配置（有才能连库，否则 getConn 返回 null） */
    public static boolean hasConfig() {
        if (appContext == null) return false;
        String h = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_HOST, null);
        return h != null && !h.isEmpty();
    }

    /** 清除已保存的 host/port 等；用于「强制重新从 URL 拉取」 */
    public static void clearSavedConfig() {
        if (appContext == null) return;
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .remove(KEY_HOST).remove(KEY_PORT).remove(KEY_DB_NAME).remove(KEY_USER).remove(KEY_PASSWORD)
                .apply();
    }

    /** 直接保存配置（不通过 URL） */
    public static void saveConfig(String host, String port, String dbName, String user, String password) {
        if (appContext == null) return;
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_HOST, host)
                .putString(KEY_PORT, port)
                .putString(KEY_DB_NAME, dbName)
                .putString(KEY_USER, user)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    private static String getHost() {
        if (appContext == null) return null;
        String s = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_HOST, null);
        return (s != null && !s.isEmpty()) ? s : null;
    }

    private static String getPort() {
        if (appContext == null) return null;
        String s = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_PORT, null);
        return (s != null && !s.isEmpty()) ? s : null;
    }

    private static String getDbName() {
        if (appContext == null) return DEFAULT_DB_NAME;
        String s = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_DB_NAME, null);
        return (s != null && !s.isEmpty()) ? s : DEFAULT_DB_NAME;
    }

    private static String getUser() {
        if (appContext == null) return DEFAULT_USER;
        String s = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_USER, null);
        return (s != null && !s.isEmpty()) ? s : DEFAULT_USER;
    }

    private static String getPassword() {
        if (appContext == null) return DEFAULT_PASSWORD;
        String s = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_PASSWORD, null);
        return (s != null && !s.isEmpty()) ? s : DEFAULT_PASSWORD;
    }

    public static Connection getConn() {
        String host = getHost();
        String port = getPort();
        if (host == null || port == null) {
            Log.w("JDBCUtils", "尚无配置，请先从 Gist 拉取（登录页会自动拉取）");
            return null;
        }
        Connection conn = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            String url = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + getDbName();
            conn = DriverManager.getConnection(url, getUser(), getPassword());
            Log.d("JDBC_TEST", "数据库连接成功: " + host + ":" + port);
        } catch (ClassNotFoundException e) {
            Log.e("JDBC_TEST", "找不到 jtds 驱动", e);
        } catch (SQLException e) {
            Log.e("JDBC_TEST", "连接失败: " + host + ":" + port, e);
        }
        return conn;
    }

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
