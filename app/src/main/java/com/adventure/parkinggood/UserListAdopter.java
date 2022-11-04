package com.adventure.parkinggood;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdopter extends RecyclerView.Adapter<UserListAdopter.ViewHolder> {

    List<User> users;
    Context context;
    boolean isTraveler;
    Activity activity;
    AlertDialog dialog;

    UserListAdopter(List<User> users, boolean isTraveler, Activity activity, AlertDialog dialog){
        this.users = users;
        this.isTraveler = isTraveler;
        this.activity = activity;
        this.dialog = dialog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
        View view = inflater.inflate(R.layout.user_list, parent, false) ;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
          User user = users.get(position);
          holder.tv_name.setText(user.name);
          holder.tv_email.setText(user.email);
          if(user.profile != null){
              Glide.with(context).load(Uri.parse(user.profile)).into(holder.iv_profile);
          }else {
              holder.iv_profile.setImageResource(R.drawable.profile);
          }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        TextView tv_email;
        CircleImageView iv_profile;

        ViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_email = itemView.findViewById(R.id.tv_email);
            iv_profile = itemView.findViewById(R.id.profile);

        }
    }


}
