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
    private OnActivityClickListener listener;


    public ActivityAdapter(List<Activity> activityList, OnActivityClickListener listener) {
        this.activityList = activityList;
        this.listener = listener;
    }

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }


    @Override
    public int getItemViewType(int position) {
        return activityList.get(position).isInvitation() ? 1 : 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            // Inflate invitation layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_groupinvitation, parent, false);
        } else {
            // Inflate regular activity layout
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        holder.tvActName.setText(activity.getActivityName());
        holder.tvActType.setText(activity.getActivityType());
        holder.tvActDate.setText(activity.getActivityDate());

        // Set click listener for each activity item
        holder.itemView.setOnClickListener(v -> listener.onActivityClick(activity));
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvActName;
        public TextView tvActType;
        public TextView tvActDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActName = itemView.findViewById(R.id.tvActName);
            tvActType = itemView.findViewById(R.id.tvActType);
            tvActDate = itemView.findViewById(R.id.tvActDate);
        }
    }
}
