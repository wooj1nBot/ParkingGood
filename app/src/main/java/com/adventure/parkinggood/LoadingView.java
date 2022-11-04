package com.adventure.parkinggood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class LoadingView {
   Context context;
    AlertDialog dialog;
    public LoadingView(Context context){
        this.context = context;
    }

    public void show(String message){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mdialog = inflater.inflate(R.layout.loading_layout, null);
        AlertDialog.Builder buider = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        buider.setView(mdialog);
        buider.setCancelable(false);
        dialog = buider.create();
        dialog.show();
        TextView textView = dialog.findViewById(R.id.textView87);
        textView.setText(message);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }
    public void stop(){
        dialog.dismiss();
    }
}
