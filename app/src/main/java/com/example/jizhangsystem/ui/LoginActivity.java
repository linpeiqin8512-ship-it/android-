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

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvGoRegister;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userDao = new UserDao();

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            Toast.makeText(LoginActivity.this, "登录失败，账号或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}