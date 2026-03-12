package com.example.jizhangsystem.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.example.jizhangsystem.R;
import com.example.jizhangsystem.adapter.RecordAdapter;
import com.example.jizhangsystem.bean.Record;
import com.example.jizhangsystem.dao.RecordDao;
import com.example.jizhangsystem.dao.UserDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private TextView tvBalance, tvIncome, tvExpense, tvMonthFilter, tvBudget, tvSurplus;
    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;
    private Spinner spinnerFilter;

    private RecordDao recordDao;
    private UserDao userDao;
    private RecordAdapter adapter;
    private List<com.example.jizhangsystem.bean.User> familyList;

    private int currentUserId, currentShowUserId, currentUserRole;
    private String currentMonth = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        recordDao = new RecordDao();
        userDao = new UserDao();

        // 刷新登录状态
        SharedPreferences sp = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        currentUserId = sp.getInt("id", 0);
        currentUserRole = sp.getInt("role", 1);
        currentShowUserId = currentUserId;

        currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new java.util.Date());

        tvBalance = view.findViewById(R.id.tv_balance);
        tvIncome = view.findViewById(R.id.tv_total_income);
        tvExpense = view.findViewById(R.id.tv_total_outcome);
        tvMonthFilter = view.findViewById(R.id.tv_month_filter);
        tvBudget = view.findViewById(R.id.tv_budget);
        tvSurplus = view.findViewById(R.id.tv_surplus);
        recyclerView = view.findViewById(R.id.recycler_view);
        btnAdd = view.findViewById(R.id.btn_add);
        spinnerFilter = view.findViewById(R.id.spinner_family_filter);

        tvMonthFilter.setText(currentMonth.replace("-", "年") + "月 ▽");
        tvMonthFilter.setOnClickListener(v -> showMonthPickerDialog());
        btnAdd.setOnClickListener(v -> startActivity(new Intent(getContext(), AddRecordActivity.class)));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 强行同步一次 ID，防止切号后变量还是旧的
        SharedPreferences sp = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        currentUserId = sp.getInt("id", 0);

        if (currentUserRole == 1 || currentUserRole == 0) {
            initFamilySpinner();
        } else {
            currentShowUserId = currentUserId;
            loadData();
        }
    }

    private void showMonthPickerDialog() {
        new TimePickerBuilder(getContext(), (date, v) -> {
            currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date);
            tvMonthFilter.setText(currentMonth.replace("-", "年") + "月 ▽");
            loadData();
        }).setType(new boolean[]{true, true, false, false, false, false})
                .setSubmitText("确定").setCancelText("取消").setLabel("年","月","","","","").build().show();
    }

    private void loadData() {
        if (currentShowUserId <= 0) return;
        new Thread(() -> {
            List<Record> list = recordDao.queryRecordsByMonth(currentShowUserId, currentMonth);
            double budget = userDao.getBudget(currentShowUserId);

            double outcome = 0, income = 0;
            for (Record r : list) {
                if ("支出".equals(r.getTypeName())) outcome += Math.abs(r.getAmount());
                else income += Math.abs(r.getAmount());
            }

            double finalOut = outcome;
            double finalInc = income;
            double finalSurplus = budget - outcome;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    adapter = new RecordAdapter(getContext(), list);
                    adapter.setOnItemLongClickListener(this::showDeleteDialog);
                    recyclerView.setAdapter(adapter);

                    tvBalance.setText("¥ " + String.format("%.2f", finalInc - finalOut));
                    tvExpense.setText(String.format("%.2f", finalOut));
                    tvIncome.setText(String.format("%.2f", finalInc));
                    tvBudget.setText(String.format("%.2f", budget));
                    tvSurplus.setText(String.format("%.2f", finalSurplus));

                    if (finalSurplus < 0) tvSurplus.setTextColor(Color.RED);
                    else tvSurplus.setTextColor(Color.WHITE);
                });
            }
        }).start();
    }

    private void initFamilySpinner() {
        new Thread(() -> {
            List<com.example.jizhangsystem.bean.User> list = new ArrayList<>();
            if (currentUserRole == 0) {
                list.addAll(userDao.getAllUsers());
            } else {
                com.example.jizhangsystem.bean.User me = new com.example.jizhangsystem.bean.User();
                me.setId(currentUserId); me.setNickname("我的账单");
                list.add(me);
                list.addAll(userDao.getChildren(currentUserId));
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    familyList = list;
                    if (familyList.size() <= 1 && currentUserRole != 0) {
                        spinnerFilter.setVisibility(View.GONE);
                        loadData();
                        return;
                    }

                    spinnerFilter.setVisibility(View.VISIBLE);
                    List<String> names = new ArrayList<>();
                    int savedPosition = 0;
                    for (int i = 0; i < familyList.size(); i++) {
                        names.add(familyList.get(i).getNickname());
                        if (familyList.get(i).getId() == currentShowUserId) savedPosition = i;
                    }

                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                            android.R.layout.simple_spinner_dropdown_item, names);
                    spinnerFilter.setAdapter(spinnerAdapter);

                    // 强制定位到刚才选中的位置
                    spinnerFilter.setSelection(savedPosition, false);

                    spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                            int selectedId = familyList.get(pos).getId();
                            //只有当 ID 真的变了，才重新加载。
                            // 配合上面的 setSelection，可以防止重复加载或初始化加载失败。
                            if (selectedId != currentShowUserId) {
                                currentShowUserId = selectedId;
                                loadData();
                            }
                        }
                        @Override public void onNothingSelected(AdapterView<?> p) {}
                    });

                    // 补丁：列表生成后，确保数据刷新
                    loadData();
                });
            }
        }).start();
    }

    private void showDeleteDialog(Record record) {
        if (currentUserRole != 0 && currentShowUserId != currentUserId) {
            Toast.makeText(getContext(), "无权修改他人账单", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("删除提示")
                .setMessage("确定删除吗？")
                .setPositiveButton("删除", (d, w) -> {
                    new Thread(() -> {
                        if (recordDao.deleteRecord(record.getId())) loadData();
                    }).start();
                })
                .setNegativeButton("取消", null).show();
    }
}