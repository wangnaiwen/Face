package com.wnw.face.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wnw.face.R;
import com.wnw.face.bean.User;
import com.wnw.face.view.MainActivity;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by wxx on 2016/10/17.88498646
 */

public class LoginActivity extends Activity implements
        View.OnClickListener{

    private EditText phone;      //电话输入框
    private EditText password;   //密码输入框
    private Button login;        //登录按钮
    private TextView newUser;      //创建新用户Button
    private TextView forgetPasswd; //忘记密码Button
    private User user;           //登录之前的user
    private User mUser;         //登录之后，查出来的user

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);

        //Bmob初始化
        Bmob.initialize(this, "07531a463483b260bbc015905c3b5824");

        //如果已经登录过，并且没有退出账号，默认登录，直接跳转到MainActivity
        SharedPreferences sharedPreferences = getSharedPreferences("account", MODE_PRIVATE);
        if(sharedPreferences != null){
            String id = sharedPreferences.getString("id", "");
            if(!id.equals("")){  //说明已经登录
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
        initView();
    }

    //初始化View
    private void initView(){
        phone = (EditText)findViewById(R.id.login_phone);
        password = (EditText)findViewById(R.id.login_password);
        login = (Button)findViewById(R.id.btn_login);
        newUser = (TextView)findViewById(R.id.login_new_user);
        forgetPasswd = (TextView)findViewById(R.id.login_forget_passwd);

        login.setOnClickListener(this);
        newUser.setOnClickListener(this);
        forgetPasswd.setOnClickListener(this);

        user = new User();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                if(validateEditText()){
                    Toast.makeText(this, "手机和密码都不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "正在拼命验证中...",Toast.LENGTH_SHORT).show();
                    //验证密码
                    user.setPhone(phone.getText().toString().trim());
                    user.setPassword(password.getText().toString().trim());
                    //开始获得数据
                    findUser(user);
                }
                break;
            case R.id.login_new_user:
                Intent intent = new Intent(this, RegisterEditNumAty.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.login_forget_passwd:
                Intent intent1= new Intent(this, RecoverPasswordActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            default:
                break;
        }
    }

    /**
     * 验证两个EditText是否都已经不为空了
     * */
    private boolean validateEditText(){
        return phone.getText().toString().trim().isEmpty() || password.getText().toString().trim().isEmpty();
    }

    //登录成功后，打开MainActivity
    private void openMainAty(){
        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra("netUser",netUser);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    //登录云端，查找账号，返回长度为0代表不成功，大于零则成功
    private void findUser(User user){
        BmobQuery<User> userBmobQuery = new BmobQuery<User>();
        userBmobQuery.addWhereEqualTo("phone",user.getPhone());
        userBmobQuery.addWhereEqualTo("password", user.getPassword());
        userBmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    if(object != null){
                        int length = object.size();
                        if(length > 0){
                            mUser = object.get(0);
                            Toast.makeText(LoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                            /**
                             * 保存这个账号的SharePreference
                             * */
                            saveAccount();
                            openMainAty();
                        }else {
                            Toast.makeText(LoginActivity.this, "手机号码或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    //登录成功，保存登录账号信息到本地
    private void saveAccount(){
        SharedPreferences.Editor editor = getSharedPreferences("account",
                MODE_PRIVATE).edit();
        editor.clear();
        editor.putString("id", mUser.getObjectId());
        editor.putString("phone",mUser.getPhone());
        editor.putString("nickname",mUser.getNickname());
        editor.putString("password", mUser.getPassword());
        BmobFile file = mUser.getImg();
        editor.putString("url", file.getFileUrl());
        editor.putString("imgName", file.getFilename());
        editor.apply();
    }

}
