package com.example.jizhangsystem.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jizhangsystem.MainActivity;
import com.example.jizhangsystem.R;
import com.example.jizhangsystem.bean.User;
import com.example.jizhangsystem.dao.UserDao;
import com.example.jizhangsystem.utils.JDBCUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister, tvRefreshConfig;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);
        tvRefreshConfig = findViewById(R.id.tv_refresh_config);

        userDao = new UserDao();

        JDBCUtils.init(getApplicationContext());
        if (JDBCUtils.getConfigUrl() == null || JDBCUtils.getConfigUrl().isEmpty()) {
            JDBCUtils.setConfigUrl(JDBCUtils.DEFAULT_CONFIG_URL);
        }

        btnLogin.setEnabled(false);
        Toast.makeText(this, "正在从 Gist 获取配置…", Toast.LENGTH_SHORT).show();
        fetchConfigAndEnableLogin();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!JDBCUtils.hasConfig()) {
                    Toast.makeText(LoginActivity.this, "配置未就绪，正在重试…", Toast.LENGTH_SHORT).show();
                    fetchConfigAndEnableLogin();
                    return;
                }
                String name = etUsername.getText().toString().trim();
                String pwd = etPassword.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pwd)) {
                    Toast.makeText(LoginActivity.this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                login(name, pwd);
            }
        });

        if (tvGoRegister != null) {
            tvGoRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                }
            });
        }
        if (tvRefreshConfig != null) {
            tvRefreshConfig.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JDBCUtils.clearSavedConfig();
                    // 强制用「不带 commit hash」的地址拉取，确保拿到 Gist 最新内容，避免旧链接拿到旧配置
                    JDBCUtils.setConfigUrl(JDBCUtils.DEFAULT_CONFIG_URL);
                    btnLogin.setEnabled(false);
                    Toast.makeText(LoginActivity.this, "已清除旧配置，正在重新获取…", Toast.LENGTH_SHORT).show();
                    fetchConfigAndEnableLogin();
                }
            });
        }
    }

    private void fetchConfigAndEnableLogin() {
        String url = JDBCUtils.getConfigUrl();
        if (url == null || url.isEmpty()) url = JDBCUtils.DEFAULT_CONFIG_URL;
        JDBCUtils.fetchConfigFromUrlAsync(url,
                () -> {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        String addr = JDBCUtils.getCurrentHostPort();
                        Toast.makeText(LoginActivity.this, "配置已就绪：" + (addr.isEmpty() ? "—" : addr), Toast.LENGTH_LONG).show();
                    });
                },
                () -> runOnUiThread(() -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "获取配置失败，请检查网络与 Gist 后重试", Toast.LENGTH_LONG).show();
                }));
    }

    private void login(String name, String pwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                User user = userDao.login(name, pwd);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null) {
                            android.content.SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("id", user.getId());
                            editor.putString("name", user.getNickname());
                            // 主页判断身份
                            editor.putInt("role", user.getRole());
                            editor.apply();

                            Toast.makeText(LoginActivity.this, "登录成功！欢迎 " + user.getNickname(), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (!JDBCUtils.hasConfig()) {
                                Toast.makeText(LoginActivity.this, "连接失败，请检查网络与 Gist 配置", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "登录失败。若为连接/隧道问题，请点「配置不对？点此重新获取」", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        }).start();
    }
}