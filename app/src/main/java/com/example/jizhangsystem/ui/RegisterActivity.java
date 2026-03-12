package com.example.jizhangsystem.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jizhangsystem.R;
import com.example.jizhangsystem.bean.User;
import com.example.jizhangsystem.dao.UserDao;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUser, etPwd, etNick;
    private RadioGroup rgRole;
    private Button btnReg;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userDao = new UserDao();
        // 1. 找控件
        etUser = findViewById(R.id.et_reg_username);
        etPwd = findViewById(R.id.et_reg_password);
        etNick = findViewById(R.id.et_reg_nickname);
        rgRole = findViewById(R.id.rg_role);
        btnReg = findViewById(R.id.btn_register);
        // 2. 点击注册
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = etUser.getText().toString().trim();
                String p = etPwd.getText().toString().trim();
                String n = etNick.getText().toString().trim();
                // 校验
                if (TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
                    Toast.makeText(RegisterActivity.this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 判断角色 (父母=1, 小孩=2)
                // 默认选中父母，如果选了小孩则变为2
                int role = 1;
                if (rgRole.getCheckedRadioButtonId() == R.id.rb_child) {
                    role = 2;
                }
                // 封装对象
                User newUser = new User();
                newUser.setUsername(u);
                newUser.setPassword(p);
                newUser.setNickname(n);
                newUser.setRole(role);
                // 执行注册
                performRegister(newUser);
            }
        });
    }

    private void performRegister(User user) {
        new Thread(() -> {
            // 调用 DAO
            boolean success = userDao.register(user);
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(RegisterActivity.this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
                    finish(); // 关闭注册页，自动回到登录页
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败 (可能是账号已存在)", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}