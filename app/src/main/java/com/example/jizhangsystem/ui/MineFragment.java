package com.example.jizhangsystem.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.jizhangsystem.R;
import com.example.jizhangsystem.dao.UserDao;

public class MineFragment extends Fragment {

    private TextView tvNick, tvRole, tvBudgetHint;
    private UserDao userDao;
    private int userId;
    private String nickname;
    private int role;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mine, container, false);

        userDao = new UserDao();

        // 1. 获取本地存储的用户基本信息
        SharedPreferences sp = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userId = sp.getInt("id", 0);
        nickname = sp.getString("name", "未登录");
        role = sp.getInt("role", 1);
        // 2. 初始化控件
        tvNick = v.findViewById(R.id.tv_mine_nick);
        tvRole = v.findViewById(R.id.tv_mine_role);
        tvBudgetHint = v.findViewById(R.id.tv_current_budget);
        // 3. 填充基础数据
        tvNick.setText(nickname);
        String roleName = (role == 0) ? "管理员" : (role == 1 ? "家长" : "子女");
        tvRole.setText("角色：" + roleName);
        // 4. 设置监听器
        v.findViewById(R.id.btn_set_budget).setOnClickListener(view -> showBudgetDialog());
        v.findViewById(R.id.btn_logout).setOnClickListener(view -> showLogoutConfirm());
        // 5. 初始加载预算金额
        refreshBudgetText();
        return v;
    }
    /*实时从数据库拉取预算并显示在按钮右侧*/
    private void refreshBudgetText() {
        new Thread(() -> {
            double budget = userDao.getBudget(userId);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    tvBudgetHint.setText("￥" + String.format("%.2f", budget) + " >");
                });
            }
        }).start();
    }

    /*弹出预算设置对话框*/
    private void showBudgetDialog() {
        final EditText et = new EditText(getContext());
        et.setHint("输入新预算金额");
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        et.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(requireContext())
                .setTitle("每月预算设置")
                .setView(et)
                .setPositiveButton("确定保存", (dialog, which) -> {
                    String input = et.getText().toString().trim();
                    if (!input.isEmpty()) {
                        updateBudgetInDB(Double.parseDouble(input));
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateBudgetInDB(double amount) {
        new Thread(() -> {
            if (userDao.updateBudget(userId, amount)) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "预算更新成功！", Toast.LENGTH_SHORT).show();
                        refreshBudgetText();
                    });
                }
            }
        }).start();
    }

    /**
     * 退出登录逻辑
     */
    private void showLogoutConfirm() {
        new AlertDialog.Builder(requireContext())
                .setTitle("提示")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("退出", (dialog, which) -> {
                    // 清除登录状态
                    SharedPreferences sp = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                    sp.edit().clear().apply();
                    // 跳转回登录页
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}