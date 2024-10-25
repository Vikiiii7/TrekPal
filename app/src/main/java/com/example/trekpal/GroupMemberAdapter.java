package com.example.trekpal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private List<GroupMember> memberList;

    public GroupMemberAdapter(List<GroupMember> memberList) {
        this.memberList = memberList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GroupMember member = memberList.get(position);
        holder.usernameTextView.setText(member.getUsername());
        holder.uniqueCodeTextView.setText(member.getUniqueCode());
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView uniqueCodeTextView;

        public ViewHolder(View view) {
            super(view);
            usernameTextView = view.findViewById(R.id.tvUsername);
            uniqueCodeTextView = view.findViewById(R.id.tvUniqueCode);
        }
    }
}

