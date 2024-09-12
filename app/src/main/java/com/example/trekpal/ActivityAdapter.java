package com.example.trekpal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<Activity> activityList;

    public ActivityAdapter(List<Activity> activityList) {
        this.activityList = activityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.tvActName.setText(activity.getActivityName());
        holder.tvActType.setText(activity.getActivityType());
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvActName;
        public TextView tvActType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActName = itemView.findViewById(R.id.tvActName);
            tvActType = itemView.findViewById(R.id.tvActType);
        }
    }
}
