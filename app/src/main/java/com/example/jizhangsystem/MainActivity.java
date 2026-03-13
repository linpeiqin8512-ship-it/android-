package com.example.jizhangsystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.jizhangsystem.bean.User;
import com.example.jizhangsystem.dao.UserDao;
import com.example.jizhangsystem.ui.DetailFragment;
import com.example.jizhangsystem.ui.LoginActivity;
import com.example.jizhangsystem.ui.ReportFragment;
import com.example.jizhangsystem.ui.MineFragment;
import com.example.jizhangsystem.ui.UserMenuBottomSheetDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private int currentUserId;
    private int currentUserRole; // 0=管理员, 1=父母, 2=小孩
    private String currentNickname;
    private UserDao userDao;

    private ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> { if (uri != null) handleSelectedImage(uri); }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userDao = new UserDao();

        SharedPreferences sp = getSharedPreferences("userInfo", MODE_PRIVATE);
        currentUserId = sp.getInt("id", 0);
        currentUserRole = sp.getInt("role", 1);
        currentNickname = sp.getString("name", "用户");

        if (currentUserId == 0) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ivAvatar = findViewById(R.id.iv_avatar);
        findViewById(R.id.cv_avatar_container).setOnClickListener(v -> showUserMenu());
        loadSavedAvatar();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();
            if (id == R.id.nav_detail) selected = new DetailFragment();
            else if (id == R.id.nav_report) selected = new ReportFragment();
            else if (id == R.id.nav_mine) selected = new MineFragment();

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_slide_in_right, R.anim.fragment_fade_out)
                        .replace(R.id.fragment_container, selected)
                        .commit();
            }
            animateBottomNavItem(bottomNav, id, id == R.id.nav_report);
            return true;
        });

        bottomNav.setOnItemReselectedListener(item -> {
            int id = item.getItemId();
            animateBottomNavItem(bottomNav, id, id == R.id.nav_report);
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DetailFragment())
                    .commit();
        }
    }

    private void animateBottomNavItem(BottomNavigationView bottomNav, int itemId, boolean bounce) {
        View itemView = bottomNav.findViewById(itemId);
        if (itemView == null) return;

        itemView.animate().cancel();
        itemView.setScaleX(1f);
        itemView.setScaleY(1f);
        itemView.setTranslationY(0f);

        if (bounce) {
            ObjectAnimator up = ObjectAnimator.ofFloat(itemView, View.TRANSLATION_Y, 0f, -6f, 0f);
            up.setDuration(220);

            ObjectAnimator sx = ObjectAnimator.ofFloat(itemView, View.SCALE_X, 1f, 1.08f, 1f);
            sx.setDuration(220);
            ObjectAnimator sy = ObjectAnimator.ofFloat(itemView, View.SCALE_Y, 1f, 1.08f, 1f);
            sy.setDuration(220);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(up, sx, sy);
            set.start();
        } else {
            itemView.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(120)
                    .withEndAction(() -> itemView.animate().scaleX(1f).scaleY(1f).setDuration(120).start())
                    .start();
        }
    }

    /* 根据角色动态生成的快速操作菜单*/
    private void showUserMenu() {
        currentNickname = getSharedPreferences("userInfo", MODE_PRIVATE).getString("name", "用户");

        UserMenuBottomSheetDialogFragment.show(
                getSupportFragmentManager(),
                currentNickname,
                currentUserRole,
                actionId -> {
                    if (UserMenuBottomSheetDialogFragment.ACTION_CHANGE_AVATAR.equals(actionId)) openGallery();
                    else if (UserMenuBottomSheetDialogFragment.ACTION_CHANGE_NICKNAME.equals(actionId)) showChangeNicknameDialog();
                    else if (UserMenuBottomSheetDialogFragment.ACTION_ADMIN_RESET_USERS.equals(actionId)) showAdminUserList();
                    else if (UserMenuBottomSheetDialogFragment.ACTION_PARENT_ADD_CHILD.equals(actionId)) showAddChildDialog();
                    else if (UserMenuBottomSheetDialogFragment.ACTION_CHILD_BIND_PARENT.equals(actionId)) showBindParentDialog();
                    else if (UserMenuBottomSheetDialogFragment.ACTION_LOGOUT.equals(actionId)) showLogoutConfirmDialog();
                }
        );
    }

    // 1. 管理员逻辑：获取全员列表并重置
    private void showAdminUserList() {
        new Thread(() -> {
            List<User> allUsers = userDao.getAllUsers();
            runOnUiThread(() -> {
                String[] names = new String[allUsers.size()];
                for (int i = 0; i < allUsers.size(); i++) {
                    names[i] = allUsers.get(i).getNickname() + " (ID:" + allUsers.get(i).getId() + ")";
                }
                new AlertDialog.Builder(this)
                        .setTitle("请选择要重置的成员")
                        .setItems(names, (d, which) -> showResetUserDialog(allUsers.get(which)))
                        .show();
            });
        }).start();
    }

    private void showResetUserDialog(User target) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        final EditText etNick = new EditText(this); etNick.setHint("新昵称"); etNick.setText(target.getNickname());
        final EditText etPass = new EditText(this); etPass.setHint("新密码"); etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNick); layout.addView(etPass);

        new AlertDialog.Builder(this).setTitle("正在重置：" + target.getNickname())
                .setView(layout)
                .setPositiveButton("确定修改", (d, w) -> {
                    String p = etPass.getText().toString().trim();
                    String n = etNick.getText().toString().trim();
                    if (p.isEmpty() || n.isEmpty()) return;
                    new Thread(() -> {
                        if (userDao.adminUpdateUser(target.getId(), p, n)) {
                            runOnUiThread(() -> Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                }).show();
    }

    // 2. 家长逻辑：直接创建孩子账号
    private void showAddChildDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        final EditText etU = new EditText(this); etU.setHint("子女登录账号");
        final EditText etP = new EditText(this); etP.setHint("子女登录密码");
        final EditText etN = new EditText(this); etN.setHint("子女昵称");
        layout.addView(etU); layout.addView(etP); layout.addView(etN);

        new AlertDialog.Builder(this).setTitle("创建子女账户")
                .setView(layout)
                .setPositiveButton("创建", (d, w) -> {
                    new Thread(() -> {
                        User child = new User();
                        child.setUsername(etU.getText().toString());
                        child.setPassword(etP.getText().toString());
                        child.setNickname(etN.getText().toString());
                        if (userDao.addChild(child, currentUserId)) {
                            runOnUiThread(() -> Toast.makeText(this, "添加成功，请在主页下拉查看", Toast.LENGTH_LONG).show());
                        }
                    }).start();
                }).show();
    }

    // 3. 子女逻辑：主动绑定父母
    private void showBindParentDialog() {
        final EditText et = new EditText(this); et.setHint("请输入父母的账号");
        new AlertDialog.Builder(this).setTitle("关联父母").setView(et)
                .setPositiveButton("申请绑定", (d, w) -> {
                    new Thread(() -> {
                        int pId = userDao.getUserIdByUsername(et.getText().toString().trim());
                        if (pId > 0 && userDao.bindParent(currentUserId, pId)) {
                            runOnUiThread(() -> Toast.makeText(this, "绑定成功！", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, "账号不存在", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                }).show();
    }
    //  4. 基础修改昵称逻辑 (本人使用)
    private void showChangeNicknameDialog() {
        final EditText et = new EditText(this);
        et.setHint("请输入新昵称");
        et.setText(currentNickname);
        et.setPadding(50, 40, 50, 40);
        new AlertDialog.Builder(this).setTitle("修改昵称").setView(et)
                .setPositiveButton("保存", (d, w) -> {
                    String newName = et.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        new Thread(() -> {
                            if (userDao.updateNickname(currentUserId, newName)) {
                                getSharedPreferences("userInfo", MODE_PRIVATE).edit().putString("name", newName).apply();
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "修改成功！", Toast.LENGTH_SHORT).show();
                                    currentNickname = newName;
                                });
                            }
                        }).start();
                    }
                }).show();
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this).setMessage("确定退出吗？")
                .setPositiveButton("确定", (d, w) -> {
                    getSharedPreferences("userInfo", MODE_PRIVATE).edit().clear().apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }).show();
    }

    private void openGallery() { galleryLauncher.launch("image/*"); }
    private void handleSelectedImage(Uri uri) {
        Glide.with(this).load(uri).into(ivAvatar);
        getSharedPreferences("app_settings", MODE_PRIVATE).edit().putString("avatar_" + currentUserId, uri.toString()).apply();
    }
    private void loadSavedAvatar() {
        String uri = getSharedPreferences("app_settings", MODE_PRIVATE).getString("avatar_" + currentUserId, null);
        if (uri != null) Glide.with(this).load(Uri.parse(uri)).into(ivAvatar);
    }
}