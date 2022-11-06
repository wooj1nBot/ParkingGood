package com.adventure.parkinggood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    TableLayout tableLayout;

    int row = 3;
    int column = 3;
    int floor = 1;
    public static final int PARKING_SIZE = 200;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Intent intent = getIntent();
        db = FirebaseFirestore.getInstance();
        ParkingPlace place = (ParkingPlace) intent.getSerializableExtra("place");
        TextView tv_name = findViewById(R.id.textView12);
        ImageView iv_done = findViewById(R.id.iv_done);
        tv_name.setText(place.getName());
        tableLayout = findViewById(R.id.table);
        EditText ed_row = findViewById(R.id.ed_row);
        EditText ed_floor = findViewById(R.id.ed_floor);
        EditText ed_col = findViewById(R.id.ed_column);
        Button btn_setting = findViewById(R.id.btn_mode2);
        btn_setting.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String r = ed_row.getText().toString();
                String f = ed_floor.getText().toString();
                if(f.length() > 0){
                    if(r.length() > 0){
                        String c = ed_col.getText().toString();
                        if(c.length() > 0){
                            row = Integer.parseInt(r);
                            column = Integer.parseInt(c);
                            floor = Integer.parseInt(f);
                            setTable(row, column);
                        }
                    }
                }
            }
        });

        iv_done.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(row > 0){
                    if(column > 0){
                        place.setRow(row);
                        place.setColumn(column);
                        place.setFloor(floor);
                        place.parkings = new ArrayList<>();
                        saveParkingPlace(place);
                    }
                }


            }
        });

        tableLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tableLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setTable(row, column);
            }
        });
    }

    public void saveParkingPlace(ParkingPlace place){
        LoadingView loadingView = new LoadingView(SettingActivity.this);
        loadingView.show("saving...");
        db.collection("place").document(place.id).set(place).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                 loadingView.stop();
                 if(task.isSuccessful()){
                     Toast.makeText(SettingActivity.this, "주차장 설정이 완료되었습니다.", Toast.LENGTH_LONG).show();
                     finish();
                 }else {
                     Log.d("dwdw", "Faf");
                     Toast.makeText(SettingActivity.this, "주차장 설정에 실패했습니다.", Toast.LENGTH_LONG).show();
                 }
            }
        });
    }

    public void setTable(int r, int c){
        tableLayout.removeAllViews();
        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow[] row = new TableRow[r];
        TextView[][] text = new TextView[r][c];
        for (int tr = 0; tr < r; tr++) {                  // for문을 이용한 줄수 (TR)

            row[tr] = new TableRow(this);

            char rc = (char) (65 + tr);

            int margin = 10;

            for (int td = 0; td < c; td++) {              // for문을 이용한 칸수 (TD)

                text[tr][td] = new TextView(this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(PARKING_SIZE,PARKING_SIZE);
                if(td == 0){
                    layoutParams.setMargins(0, margin, margin, margin);
                }else if(td == c-1){
                    layoutParams.setMargins(margin, margin, 0, margin);
                }else {
                    layoutParams.setMargins(margin, margin, margin, margin);
                }
                text[tr][td].setLayoutParams(layoutParams);
                text[tr][td].setText(rc +""+ (td+1));// 데이터삽입
                text[tr][td].setBackgroundResource(R.drawable.stroke_activie);
                text[tr][td].setTextSize(16);                     // 폰트사이즈

                text[tr][td].setTextColor(Color.WHITE);     // 폰트컬러

                text[tr][td].setGravity(Gravity.CENTER);    // 폰트정렬

                row[tr].addView(text[tr][td]);

            } // td for end

            tableLayout.addView(row[tr], rowLayout);

        } // tr for end


    }
}