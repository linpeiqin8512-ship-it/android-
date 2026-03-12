package com.example.jizhangsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhangsystem.R;
import com.example.jizhangsystem.bean.Record;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private Context context;
    private List<Record> recordList;
    private OnItemLongClickListener longClickListener;

    // 用于权限判断
    private int currentUserId;
    private int currentUserRole;

    public interface OnItemLongClickListener {
        void onLongClick(Record record);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public RecordAdapter(Context context, List<Record> recordList) {
        this.context = context;
        this.recordList = recordList;

        // 初始化时直接读取当前登录者的身份
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        this.currentUserId = sp.getInt("id", 0);
        this.currentUserRole = sp.getInt("role", 1);
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record_list, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = recordList.get(position);

        // 1. 日期精简
        String rawDate = record.getDate();
        if (rawDate != null && rawDate.contains(" ")) rawDate = rawDate.split(" ")[0];
        if (rawDate != null && rawDate.length() >= 10) {
            holder.tvDate.setText(rawDate.substring(5, 7) + "月" + rawDate.substring(8, 10) + "日");
        } else {
            holder.tvDate.setText(rawDate);
        }

        // 2. 备注和图标
        holder.tvRemark.setText((record.getRemark() == null || record.getRemark().isEmpty()) ? record.getCategory() : record.getRemark());
        holder.tvCategory.setText(record.getCategory().substring(0, 1));
        setCategoryStyle(holder.tvCategory, record.getCategory());

        // 3. 金额颜色
        double absAmount = Math.abs(record.getAmount());
        String moneyStr = String.format("%.2f", absAmount);
        if ("支出".equals(record.getTypeName())) {
            holder.tvAmount.setText("- " + moneyStr);
            holder.tvAmount.setTextColor(Color.parseColor("#FF5252"));
        } else {
            holder.tvAmount.setText("+ " + moneyStr);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"));
        }

        // ★ 权限核心逻辑：点击修改 ★
        holder.itemView.setOnClickListener(v -> {
            // 判断规则：
            // 1. 如果是管理员 (role == 0)，随便改
            // 2. 如果是账单主人 (record.userId == currentUserId)，随便改
            // 3. 其他情况（如家长看孩子），拦截
            if (currentUserRole == 0 || record.getUserId() == currentUserId) {
                Intent intent = new Intent(context, com.example.jizhangsystem.ui.AddRecordActivity.class);
                intent.putExtra("id", record.getId());
                intent.putExtra("user_id", record.getUserId());
                intent.putExtra("amount", record.getAmount());
                intent.putExtra("type", record.getTypeName());
                intent.putExtra("category", record.getCategory());
                intent.putExtra("remark", record.getRemark());
                intent.putExtra("date", record.getDate());
                context.startActivity(intent);
            } else {
                // 家长看孩子账单点进来，走这里
                Toast.makeText(context, "家长仅有查看权，不可修改子女账单", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onLongClick(record);
            return true;
        });
    }

    private void setCategoryStyle(TextView tv, String category) {
        String colorStr;
        switch (category) {
            case "餐饮美食": colorStr = "#FF9800"; break;
            case "交通出行": colorStr = "#2196F3"; break;
            case "服饰美容": colorStr = "#E91E63"; break;
            case "生活日用": colorStr = "#4CAF50"; break;
            case "休闲娱乐": colorStr = "#9C27B0"; break;
            case "工资收入": colorStr = "#FFC107"; break;
            default: colorStr = "#90A4AE"; break;
        }
        GradientDrawable drawable = (GradientDrawable) tv.getBackground();
        if (drawable != null) drawable.setColor(Color.parseColor(colorStr));
    }

    @Override
    public int getItemCount() { return recordList.size(); }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvRemark, tvDate, tvAmount;
        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_item_category);
            tvRemark = itemView.findViewById(R.id.tv_item_remark);
            tvDate = itemView.findViewById(R.id.tv_item_date);
            tvAmount = itemView.findViewById(R.id.tv_item_amount);
        }
    }
}