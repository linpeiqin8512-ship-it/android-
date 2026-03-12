package com.example.jizhangsystem.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.example.jizhangsystem.R;
import com.example.jizhangsystem.bean.CategorySumBean;
import com.example.jizhangsystem.bean.DateSumBean;
import com.example.jizhangsystem.bean.Record;
import com.example.jizhangsystem.dao.RecordDao;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private PieChart pieChart;
    private BarChart barChart;
    private LineChart lineChart;
    private RadioGroup rgTime;
    private TextView tvTitle, tvDateFilter;
    private View btnExport; // ★ 新增：导出按钮变量
    private RecordDao recordDao;
    private int currentUserId;

    private String currentMode = "WEEK";
    private String selectedTime = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);

        pieChart = v.findViewById(R.id.chart_pie);
        barChart = v.findViewById(R.id.chart_bar);
        lineChart = v.findViewById(R.id.chart_line);
        rgTime = v.findViewById(R.id.rg_time_picker);
        tvTitle = v.findViewById(R.id.tv_chart_title);
        tvDateFilter = v.findViewById(R.id.tv_report_date_filter);
        btnExport = v.findViewById(R.id.btn_export); // ★ 初始化导出按钮

        recordDao = new RecordDao();
        currentUserId = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("id", 0);

        initDefaultTime();

        rgTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_week) currentMode = "WEEK";
            else if (checkedId == R.id.rb_month) currentMode = "MONTH";
            else if (checkedId == R.id.rb_year) currentMode = "YEAR";
            initDefaultTime();
            loadCharts();
        });

        tvDateFilter.setOnClickListener(view -> showTimePickerByMode());

        // ★ 绑定导出逻辑
        if (btnExport != null) {
            btnExport.setOnClickListener(view -> performExport());
        }

        return v;
    }

    /**
     * ★ 执行导出逻辑：从数据库查明细 -> 生成CSV -> 调起分享
     */
    private void performExport() {
        new Thread(() -> {
            // 1. 获取账单明细数据 (需确保 RecordDao 已经添加了 queryRecordsForExport 方法)
            List<Record> list = recordDao.queryRecordsForExport(currentUserId, currentMode, selectedTime);

            if (list == null || list.isEmpty()) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "该时段暂无数据，导出取消", Toast.LENGTH_SHORT).show());
                }
                return;
            }

            // 2. 拼接 CSV 内容字符串
            StringBuilder csv = new StringBuilder();
            csv.append("日期,收支类型,分类,金额,备注\n"); // 表头
            for (Record r : list) {
                csv.append(r.getDate()).append(",")
                        .append(r.getTypeName()).append(",")
                        .append(r.getCategory()).append(",")
                        .append(r.getAmount()).append(",")
                        .append(r.getRemark() == null ? "" : r.getRemark().replace(",", "，"))
                        .append("\n");
            }

            try {
                // 3. 将内容写入临时文件
                String fileName = "记账明细_" + selectedTime + ".csv";
                File file = new File(requireContext().getExternalCacheDir(), fileName);
                FileOutputStream fos = new FileOutputStream(file);
                // 写入 UTF-8 BOM 头，防止 Excel 打开中文乱码
                fos.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                fos.write(csv.toString().getBytes());
                fos.close();

                // 4. 成功后调起系统分享
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> shareFile(file));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "生成表格失败", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    /**
     * ★ 调起系统分享
     */
    private void shareFile(File file) {
        Uri uri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "下载/分享账单表格"));
    }

    // --- 以下为你原来的逻辑，保持不变 ---

    private void initDefaultTime() {
        Date now = new Date();
        if ("WEEK".equals(currentMode)) {
            selectedTime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now);
            tvDateFilter.setText(selectedTime + " 往前7天 ▽");
        } else if ("MONTH".equals(currentMode)) {
            selectedTime = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(now);
            tvDateFilter.setText(selectedTime.replace("-", "年") + "月 ▽");
        } else {
            selectedTime = new SimpleDateFormat("yyyy", Locale.getDefault()).format(now);
            tvDateFilter.setText(selectedTime + "年度 ▽");
        }
    }

    private void showTimePickerByMode() {
        boolean[] type;
        if ("WEEK".equals(currentMode)) type = new boolean[]{true, true, true, false, false, false};
        else if ("MONTH".equals(currentMode)) type = new boolean[]{true, true, false, false, false, false};
        else type = new boolean[]{true, false, false, false, false, false};

        new TimePickerBuilder(getContext(), (date, v) -> {
            if ("WEEK".equals(currentMode)) {
                selectedTime = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                tvDateFilter.setText(selectedTime + " 往前7天 ▽");
            } else if ("MONTH".equals(currentMode)) {
                selectedTime = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date);
                tvDateFilter.setText(selectedTime.replace("-", "年") + "月 ▽");
            } else {
                selectedTime = new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
                tvDateFilter.setText(selectedTime + "年度 ▽");
            }
            loadCharts();
        }).setType(type).setSubmitText("确定").setCancelText("取消").setLabel("年","月","日","","","").build().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCharts();
    }

    private void loadCharts() {
        new Thread(() -> {
            List<CategorySumBean> cats = recordDao.getCategorySumsFiltered(currentUserId, currentMode, selectedTime);
            List<DateSumBean> dates = recordDao.getDateSumsFiltered(currentUserId, currentMode, selectedTime);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    String titleSuffix = "支出概览";
                    if ("WEEK".equals(currentMode)) titleSuffix = "近7日支出图表";
                    else if ("MONTH".equals(currentMode)) titleSuffix = selectedTime + " 月度报表";
                    else titleSuffix = selectedTime + " 年度趋势";

                    tvTitle.setText(titleSuffix);
                    renderPie(cats); renderBar(cats); renderLine(dates);
                });
            }
        }).start();
    }

    private void renderPie(List<CategorySumBean> list) {
        pieChart.clear();
        if (list.isEmpty()) { pieChart.setNoDataText("该时段暂无支出数据"); return; }
        float total = 0;
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySumBean b : list) {
            entries.add(new PieEntry(b.getTotalAmount(), b.getCategory()));
            total += b.getTotalAmount();
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setUsePercentValues(true);
        pieChart.setData(data);
        pieChart.setCenterText("合计\n¥ " + String.format("%.2f", total));
        pieChart.getDescription().setEnabled(false);
        pieChart.animateXY(800, 800);
        pieChart.invalidate();
    }

    private void renderBar(List<CategorySumBean> list) {
        barChart.clear();
        if (list.isEmpty()) return;
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            entries.add(new BarEntry(i, list.get(i).getTotalAmount()));
            labels.add(list.get(i).getCategory());
        }
        BarDataSet ds = new BarDataSet(entries, "支出项目");
        ds.setColors(ColorTemplate.COLORFUL_COLORS);
        barChart.setData(new BarData(ds));
        XAxis xa = barChart.getXAxis();
        xa.setValueFormatter(new IndexAxisValueFormatter(labels));
        xa.setPosition(XAxis.XAxisPosition.BOTTOM);
        xa.setGranularity(1f);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void renderLine(List<DateSumBean> list) {
        lineChart.clear();
        if (list.isEmpty()) return;
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            entries.add(new Entry(i, list.get(i).getAmount()));
            dates.add(list.get(i).getDate());
        }
        LineDataSet ds = new LineDataSet(entries, "金额");
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setDrawFilled(true);
        ds.setCircleRadius(4f);
        ds.setLineWidth(2f);
        ds.setColor(Color.parseColor("#2196F3"));
        ds.setFillColor(Color.parseColor("#2196F3"));
        lineChart.setData(new LineData(ds));
        XAxis xa = lineChart.getXAxis();
        xa.setValueFormatter(new IndexAxisValueFormatter(dates));
        xa.setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }
}