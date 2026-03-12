package com.example.jizhangsystem.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.example.jizhangsystem.R;
import com.example.jizhangsystem.bean.Record;
import com.example.jizhangsystem.dao.RecordDao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText etAmount, etRemark;
    private RadioGroup rgType;
    private Spinner spCategory;
    private TextView tvDateSelector;
    private Button btnSave;
    private RecordDao recordDao;

    private String selectedDate = "";
    private int editRecordId = -1;
    private int originalRecordUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);

        recordDao = new RecordDao();
        etAmount = findViewById(R.id.et_amount);
        etRemark = findViewById(R.id.et_remark);
        rgType = findViewById(R.id.rg_type);
        spCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save);
        tvDateSelector = findViewById(R.id.tv_date_selector);

        //获取 Intent数据(判断是新增还是修改)
        editRecordId = getIntent().getIntExtra("id", -1);
        originalRecordUserId = getIntent().getIntExtra("user_id", -1);

        if (editRecordId != -1) {
            // 回填旧数据
            btnSave.setText("保存修改");

            // 金额回填
            double amt = getIntent().getDoubleExtra("amount", 0.0);
            etAmount.setText(String.valueOf(Math.abs(amt)));

            // 备注回填
            etRemark.setText(getIntent().getStringExtra("remark"));

            // 日期回填
            selectedDate = getIntent().getStringExtra("date");
            if (selectedDate != null && selectedDate.contains(" ")) {
                selectedDate = selectedDate.split(" ")[0];
            }
            tvDateSelector.setText(selectedDate);

            // 类型回填
            String type = getIntent().getStringExtra("type");
            if ("收入".equals(type)) rgType.check(R.id.rb_income);
            else rgType.check(R.id.rb_expense);

            // 寻找字符串在 Spinner 里的位置
            String category = getIntent().getStringExtra("category");
            setSpinnerValue(spCategory, category);

        } else {
            // 初始化默认值
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
            tvDateSelector.setText(selectedDate);
        }

        tvDateSelector.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveRecord());
    }

    // 让 Spinner 选中指定的文字
    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void showDatePicker() {
        new TimePickerBuilder(this, (date, v) -> {
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
            tvDateSelector.setText(selectedDate);
        }).setType(new boolean[]{true, true, true, false, false, false}).build().show();
    }

    private void saveRecord() {
        // 获取当前界面上最新的输入内容
        String amountStr = etAmount.getText().toString().trim();
        String newRemark = etRemark.getText().toString().trim();
        String newCategory = spCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = (rgType.getCheckedRadioButtonId() == R.id.rb_expense) ? "支出" : "收入";
        // 处理金额正负
        if ("支出".equals(type) && amount > 0) amount = -amount;
        if ("收入".equals(type) && amount < 0) amount = -amount;

        // 封装 Record 对象
        Record record = new Record();
        record.setAmount(amount);
        record.setTypeName(type);
        record.setCategory(newCategory);
        record.setRemark(newRemark);
        record.setDate(selectedDate);

        new Thread(() -> {
            boolean success;
            if (editRecordId != -1) {
                record.setId(editRecordId);
                record.setUserId(originalRecordUserId);
                success = recordDao.updateRecord(record);
            } else {
                int myId = getSharedPreferences("userInfo", MODE_PRIVATE).getInt("id", 1);
                record.setUserId(myId);
                success = recordDao.addRecord(record);
            }
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "数据库更新失败", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}