package com.example.jizhangsystem.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhangsystem.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class UserMenuBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnActionClickListener {
        void onActionClick(@NonNull String actionId);
    }

    public static final String ACTION_CHANGE_AVATAR = "change_avatar";
    public static final String ACTION_CHANGE_NICKNAME = "change_nickname";
    public static final String ACTION_ADMIN_RESET_USERS = "admin_reset_users";
    public static final String ACTION_PARENT_ADD_CHILD = "parent_add_child";
    public static final String ACTION_CHILD_BIND_PARENT = "child_bind_parent";
    public static final String ACTION_LOGOUT = "logout";

    private static final String ARG_NICKNAME = "arg_nickname";
    private static final String ARG_ROLE = "arg_role";

    private OnActionClickListener listener;

    public static void show(@NonNull FragmentManager fm, @NonNull String nickname, int role, @NonNull OnActionClickListener listener) {
        UserMenuBottomSheetDialogFragment sheet = new UserMenuBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NICKNAME, nickname);
        args.putInt(ARG_ROLE, role);
        sheet.setArguments(args);
        sheet.setOnActionClickListener(listener);
        sheet.show(fm, "UserMenuBottomSheet");
    }

    public void setOnActionClickListener(@Nullable OnActionClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_Jizhangsystem_UserMenuBottomSheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_user_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String nickname = "用户";
        int role = 1;
        Bundle args = getArguments();
        if (args != null) {
            nickname = args.getString(ARG_NICKNAME, "用户");
            role = args.getInt(ARG_ROLE, 1);
        }

        TextView title = view.findViewById(R.id.tv_sheet_title);
        title.setText("你好，" + nickname);

        RecyclerView rv = view.findViewById(R.id.rv_menu);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        List<MenuAction> actions = buildActions(role);
        rv.setAdapter(new ActionsAdapter(actions, actionId -> {
            dismiss();
            if (listener != null) listener.onActionClick(actionId);
        }));
    }

    private List<MenuAction> buildActions(int role) {
        List<MenuAction> list = new ArrayList<>();
        list.add(new MenuAction(ACTION_CHANGE_AVATAR, "更换头像", R.drawable.ic_action_avatar));
        list.add(new MenuAction(ACTION_CHANGE_NICKNAME, "更改昵称", R.drawable.ic_action_edit));

        if (role == 0) {
            list.add(new MenuAction(ACTION_ADMIN_RESET_USERS, "【管理】成员账号重置", R.drawable.ic_action_admin));
        } else if (role == 1) {
            list.add(new MenuAction(ACTION_PARENT_ADD_CHILD, "【管理】添加子女账号", R.drawable.ic_action_add_child));
        } else if (role == 2) {
            list.add(new MenuAction(ACTION_CHILD_BIND_PARENT, "【管理】绑定父母账号", R.drawable.ic_action_link));
        }

        list.add(new MenuAction(ACTION_LOGOUT, "退出登录", R.drawable.ic_action_logout));
        return list;
    }

    private static final class MenuAction {
        final String id;
        final String label;
        final @DrawableRes int iconRes;

        MenuAction(@NonNull String id, @NonNull String label, @DrawableRes int iconRes) {
            this.id = id;
            this.label = label;
            this.iconRes = iconRes;
        }
    }

    private static final class ActionsAdapter extends RecyclerView.Adapter<ActionsAdapter.VH> {
        interface Click {
            void onClick(@NonNull String actionId);
        }

        private final List<MenuAction> items;
        private final Click click;

        ActionsAdapter(@NonNull List<MenuAction> items, @NonNull Click click) {
            this.items = items;
            this.click = click;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_menu_action, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            MenuAction item = items.get(position);
            holder.label.setText(item.label);
            holder.icon.setImageResource(item.iconRes);
            holder.itemView.setOnClickListener(v -> click.onClick(item.id));
            holder.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class VH extends RecyclerView.ViewHolder {
            final TextView label;
            final ImageView icon;
            final View divider;

            VH(@NonNull View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.tv_label);
                icon = itemView.findViewById(R.id.iv_icon);
                divider = itemView.findViewById(R.id.view_divider);
            }
        }
    }
}

