package com.adventure.parkinggood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerView;
    private FirebaseFirestore db;
    TextView tv_name;
    TextView tv_email;
    RelativeLayout btn_add;
    CircleImageView iv_profile;
    RecyclerView rc;
    RecyclerView travel_rc;
    int cnt = 0;

    List<String> list = new ArrayList<>();
    List<String> progress_ids = new ArrayList<>();
    List<String> upcoming_ids = new ArrayList<>();
    List<String> end_ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
        ImageView iv_friend = findViewById(R.id.iv_friend);

        if(currentUser != null && currentUser.isEmailVerified()) {
            FloatingActionButton fb_add = findViewById(R.id.fb_add);
            fb_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }
            });
            drawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer_root);
            navigationView = (NavigationView) findViewById(R.id.nv_main_navigation_root);
            travel_rc = findViewById(R.id.travel_rc);
            headerView = navigationView.getHeaderView(0);
            tv_name = headerView.findViewById(R.id.tv_name);
            tv_email = headerView.findViewById(R.id.tv_email);
            iv_profile = headerView.findViewById(R.id.profile);
            btn_add = headerView.findViewById(R.id.btn_add);
            rc = headerView.findViewById(R.id.rc);
            ImageView iv_logout = headerView.findViewById(R.id.iv_logout);
            rc.setLayoutManager(new LinearLayoutManager(this));
            travel_rc.setLayoutManager(new LinearLayoutManager(this));
            btn_add.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    showAddFriendDialog();
                }
            });
            iv_logout.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    logout();
                }
            });

            iv_friend.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if(drawerLayout.isDrawerOpen(Gravity.RIGHT)){
                        drawerLayout.closeDrawer(Gravity.RIGHT);
                    }else {
                        drawerLayout.openDrawer(Gravity.RIGHT);
                    }
                }
            });

            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getUserData();
                }
            });

        }else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserData();
    }

    public void logout(){
        String uid = currentUser.getUid();
        if(!uid.equals("") && db != null) {
            db.collection("users").document(uid).update("status", 0);
        }
        FirebaseMessaging.getInstance().deleteToken();
        FirebaseAuth.getInstance().signOut();
        Intent intents = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intents);
        finish();
    }

    public void getUserData(){

        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);
                    if(user != null){
                        String name = (String) user.name;
                        String email = (String) user.email;
                        tv_name.setText(name);
                        tv_email.setText(email);
                        if(user.profile != null){
                            Glide.with(MainActivity.this).load(Uri.parse(user.profile)).into(iv_profile);
                        }
                        List<String> friends = user.getFriends();

                        if(friends != null) {
                            getFriendData(friends);
                        }

                    }
                }
            }
        });
    }

    public void getFriendData(List<String> friends){
        List<User> users = new ArrayList<>();
        cnt = 0;
        for(String uid : friends){
            db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    cnt++;
                    if(task.isSuccessful()){
                        User user = task.getResult().toObject(User.class);
                        if(user != null){
                            users.add(user);
                        }
                    }
                    if(cnt == friends.size()){
                        UserListAdopter userListAdopter = new UserListAdopter(users, false, MainActivity.this, null);
                        rc.setAdapter(userListAdopter);
                    }
                }
            });
        }
    }


    public void showAddFriendDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.addfriends_dialog, (RelativeLayout) findViewById(R.id.dialog));
        EditText ed_email = view.findViewById(R.id.ed_email);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        String email = ed_email.getText().toString();
                        if(email.length() > 0){
                            if(email.equals(currentUser.getEmail())){
                                ed_email.setError("You can't be friends with yourself");
                            }else {
                                if(!email.contains("@")){
                                    ed_email.setError("Please enter a valid email address.");
                                }else {
                                    LoadingView loadingView = new LoadingView(MainActivity.this);
                                    loadingView.show("Searching for Friends...");
                                    db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){
                                                List<DocumentSnapshot> list = task.getResult().getDocuments();
                                                if(list.size() > 0){
                                                    String uid = (String) list.get(0).get("uid");
                                                    addFriends(uid, loadingView, dialog);
                                                }else {
                                                    loadingView.stop();
                                                    ed_email.setError("There are no friends with this email.");
                                                }
                                            }else {
                                                loadingView.stop();
                                                ed_email.setError("There are no friends with this email.");
                                            }
                                        }
                                    });
                                }
                            }
                        }else {
                            ed_email.setError("Please enter your email address");
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    public void addFriends(String uid, LoadingView loadingView, DialogInterface dialog){
        db.collection("users").document(currentUser.getUid()).update("friends", FieldValue.arrayUnion(uid)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    db.collection("users").document(uid).update("friends", FieldValue.arrayUnion(currentUser.getUid())).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingView.stop();
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Friend added!" , Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                getUserData();
                            }else {
                                Toast.makeText(MainActivity.this, "Failed to add friend." , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    loadingView.stop();
                    Toast.makeText(MainActivity.this, "Failed to add friend." , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if ( pressedTime == 0 ) {
            Toast.makeText(MainActivity.this, "Press once more to exit." , Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if ( seconds > 2000 ) {
                Toast.makeText(MainActivity.this, "Press once more to exit." , Toast.LENGTH_SHORT).show();
                pressedTime = 0 ;
            }
            else {
                super.onBackPressed();

                finish(); // app 종료 시키기
            }
        }
    }

}