package com.adventure.parkinggood;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser User;
    private FirebaseFirestore db;
    TextInputEditText name_edit;
    TextInputEditText email_edit;
    TextInputEditText password_edit;
    TextInputEditText phone_edit;
    CardView join_btn;
    ImageView imageView;
    CheckBox checkBox;
    private StorageReference storageRef;
    boolean isver = false;
    private static final int MY_PERMISSION_STORAGE = 1111;
    TextView tv_join;
    File file;
    private int REQUEST_CODE_CHOOSE = 76;
    private Uri resultUri;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        checkPermission();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://savel-18caf.appspot.com");
        storageRef = storage.getReference();
        mAuth.setLanguageCode("ko");
        imageView = findViewById(R.id.profile);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.etPassword);
        name_edit = findViewById(R.id.name_edit);
        phone_edit = findViewById(R.id.etPhone);
        join_btn = findViewById(R.id.login_btn);
        tv_join = findViewById(R.id.textView8);

        checkBox = findViewById(R.id.checkBox);
        RelativeLayout profile = findViewById(R.id.profile_img);
        mAuth.addIdTokenListener(new FirebaseAuth.IdTokenListener() {
            @Override
            public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
                User = firebaseAuth.getCurrentUser();
            }
        });
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final View mdialog = inflater.inflate(R.layout.profile_dialog, null);
                    AlertDialog.Builder buider = new AlertDialog.Builder(SignUpActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
                    buider.setView(mdialog);
                    Dialog dialog = buider.create();
                    dialog.show();

                    TextView tv_default = mdialog.findViewById(R.id.tv_default);
                    TextView tv_album = mdialog.findViewById(R.id.tv_album);

                    tv_default.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            imageView.setImageResource(R.drawable.profile);
                            resultUri = null;
                            dialog.dismiss();
                        }
                    });
                    tv_album.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            Matisse.from(SignUpActivity.this)
                                    .choose(MimeType.ofImage())
                                    .countable(true)
                                    .showSingleMediaType(true)
                                    .capture(false)
                                    .maxSelectable(1)
                                    .autoHideToolbarOnSingleTap(true)
                                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                    .thumbnailScale(0.85f)
                                    .imageEngine(new GlideEngine())
                                    .forResult(REQUEST_CODE_CHOOSE);
                            dialog.dismiss();
                        }
                    });
                }else {
                    checkPermission();
                }

            }
        });
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                   enableJoinButton();
                }else {
                    join_btn.setCardBackgroundColor(Color.parseColor("#eeeeee"));
                    join_btn.setClickable(false);
                }
            }
        });
        phone_edit.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        join_btn.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                String email = email_edit.getText().toString().replaceAll(" ", "");;
                String password = password_edit.getText().toString().replaceAll(" ", "");
                String name = name_edit.getText().toString();
                String phone = phone_edit.getText().toString().replaceAll(" ", "");
                if (name.length() > 0) {
                    if (!email.contains("@")) {
                        email_edit.setError("올바른 이메일 주소를 적어주세요.");
                    } else {
                        if (password.length() < 6) {
                            password_edit.setError("6자리 이상의 비밀번호를 입력해주세요.");
                        } else {
                            com.adventure.parkinggood.User user = new User();
                            user.name = name;
                            user.email = email;

                            if(phone.length() > 0){
                                user.phone = phone;
                            }

                            if(isver){
                                doneVerifyEmail();
                            }else {
                                sendVerifyEmail(email, password, user);
                            }
                        }
                    }
                }else {
                    email_edit.setError("이름을 입력해주세요.");
                }
            }
        });

        email_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    enableJoinButton();
                }else {
                    join_btn.setCardBackgroundColor(Color.parseColor("#eeeeee"));
                    join_btn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    enableJoinButton();
                }else {
                    join_btn.setCardBackgroundColor(Color.parseColor("#eeeeee"));
                    join_btn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        name_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() > 0){
                    enableJoinButton();
                }else {
                    join_btn.setCardBackgroundColor(Color.parseColor("#eeeeee"));
                    join_btn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void enableJoinButton(){
        if(checkBox.isChecked() && password_edit.getText().toString().length() > 0 && email_edit.getText().toString().length() > 0  && name_edit.getText().toString().length() > 0) {
            join_btn.setCardBackgroundColor(Color.parseColor("#F89E3E"));
            join_btn.setClickable(true);
        }else {
            join_btn.setCardBackgroundColor(Color.parseColor("#eeeeee"));
            join_btn.setClickable(false);
        }
    }

    public void doneVerifyEmail(){
        mAuth = FirebaseAuth.getInstance();
        LoadingView loadingView = new LoadingView(SignUpActivity.this);
        loadingView.show("loading...");
        final FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    loadingView.stop();
                    User = mAuth.getCurrentUser();
                    if(User.isEmailVerified()){
                        Toast.makeText(SignUpActivity.this, "회원가입이 완료되었습니다!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(SignUpActivity.this, "이메일 인증이 완료되지 않았습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void sendVerifyEmail(String email, String password, User user){
        final LoadingView loadingView = new LoadingView(SignUpActivity.this);
        loadingView.show("Loading...");
        mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                User = mAuth.getCurrentUser();
                User.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "인증용 이메일을 전송했습니다.", Toast.LENGTH_LONG).show();
                            isver = true;
                            tv_join.setText("Complete Sign Up");
                            email_edit.setEnabled(false);
                            password_edit.setEnabled(false);
                            name_edit.setEnabled(false);
                            phone_edit.setEnabled(false);
                            user.uid = User.getUid();

                            if(resultUri != null){
                                saveProfile(resultUri, user, loadingView);
                            }else {
                                saveData(user, loadingView);
                            }
                        }else {
                            loadingView.stop();
                            Toast.makeText(SignUpActivity.this, "이메일 인증 실패", Toast.LENGTH_LONG).show();
                        }
                    }});
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingView.stop();
                email_edit.setError("이미 가입된 이메일 주소입니다.");
            }
        });
    }

    public void saveProfile(Uri uri, User user, LoadingView loadingView){
        final StorageReference storageReference = storageRef.child("profile").child(User.getUid()).child("profile.jpg");
        UploadTask uploadTask = storageReference.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    if (file != null) {
                        file.delete();
                        Uri downloadUri = task.getResult();
                        user.profile = downloadUri.toString();
                        saveData(user, loadingView);
                    }else {
                        loadingView.stop();
                        resultUri = null;
                        Toast.makeText(SignUpActivity.this, "Profile file does not exist.", Toast.LENGTH_LONG).show();
                    }
                }else {
                    loadingView.stop();
                    Toast.makeText(SignUpActivity.this, "데이터 저장에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                }
            }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingView.stop();
                Toast.makeText(SignUpActivity.this, "데이터 저장에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void saveData(User user, LoadingView loadingView){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    loadingView.stop();
                    Toast.makeText(SignUpActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                user.token = task.getResult();
                db.collection("users").document(User.getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingView.stop();
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "이메일 인증을 완료해주세요!", Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(SignUpActivity.this, "데이터 저장에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }
        });
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 다시 보지 않기 버튼을 만드려면 이 부분에 바로 요청을 하도록 하면 됨 (아래 else{..} 부분 제거)
            // ActivityCompat.requestPermissions((Activity)mContext, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_CAMERA);

            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_STORAGE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> mSelected = Matisse.obtainResult(data);
            String path = getDir("tmp", Context.MODE_PRIVATE).getPath();
            file = new File(path,"crop.jpg");
            UCrop.Options options = new UCrop.Options();
            options.setCircleDimmedLayer(true);
            options.withAspectRatio(1,1);
            options.withMaxResultSize(100,100);
            options.setAllowedGestures(UCropActivity.SCALE,UCropActivity.NONE,UCropActivity.NONE);
            options.setFreeStyleCropEnabled(false);
            UCrop.of(mSelected.get(0), Uri.fromFile(file))
                    .withOptions(options)
                    .start(SignUpActivity.this,UCrop.REQUEST_CROP);
        }
        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP){
            resultUri = UCrop.getOutput(data);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
            requestOptions.skipMemoryCache(true);
            Glide.with(SignUpActivity.this).load(resultUri).apply(requestOptions).into(imageView);
        }
    }

    @Override
    public void onBackPressed() {
        if ( pressedTime == 0 ) {
            Toast.makeText(SignUpActivity.this, "한번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if ( seconds > 2000 ) {
                Toast.makeText(SignUpActivity.this, "한번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                pressedTime = 0 ;
            }
            else {
                super.onBackPressed();

                finish(); // app 종료 시키기
            }
        }
    }

}