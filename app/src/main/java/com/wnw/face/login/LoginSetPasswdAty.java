package com.wnw.face.login;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wnw.face.R;
import com.wnw.face.bean.User;
import com.wnw.face.view.MainActivity;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


/**
 * Created by wxx on 2017/3/26.
 * 这个类是由LoginGetCodesAty跳转而来
 * 主要完成以下功能：
 * 1. 判断用户设置的密码是否合适，不合适，完成按钮为不可点击，合适就变为可点击
 * 2. 如果增加用户二次输入密码，判断用户两次输入的密码是否一致
 * 3. 在这里需要将用户的密码进行加工，转移到另一个类中存入数据库，或者直接调入数据库
 * 4. 完成用户的按钮接听工作
 */
public class LoginSetPasswdAty extends Activity implements
        View.OnClickListener{
    /**定义一个PhoneNum用户存储在LoginGetCodesAty中Intent传递过来的手机号码，在初始化时完成*/
    private String phoneNum = null;

    private String password = null;

    /**定义一个JudgePasswdIsLegal对象，调用其passwdIsLegal(String passwd)方法输入密码是否合法*/
    private JudgePasswdIsLegal judgePasswdIsLegal;

    /**返回键*/
    private ImageView back_imgbtn;

    /**完成键*/
    private Button login_setpasswd_ok;

    /**password的EditTxt*/
    private EditText login_edit_setpasswd;

    /**昵称*/
    private EditText nickName;

    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_set_passwd);

        Intent intent = getIntent();
        phoneNum = intent.getStringExtra("phone");
        //phoneNum = getPhoneNum();
        init();
    }


    /**初始化组件，并且设置事件监听*/
    public void init(){

        //初始化judgePasswdIsLegal对象
        judgePasswdIsLegal = new JudgePasswdIsLegal();

        /**将本地的Activity添加到ActivityCollector中*/
        ActivityCollector.addActivity(this);

        back_imgbtn = (ImageView)findViewById(R.id.back_imgbtn);
        login_setpasswd_ok = (Button)findViewById(R.id.login_setpasswd_ok);
        login_edit_setpasswd = (EditText)findViewById(R.id.login_edit_setpasswd);
        nickName = (EditText)findViewById(R.id.nickName);

        back_imgbtn.setOnClickListener(this);
        login_setpasswd_ok.setOnClickListener(this);

        /**一旦用户输入发生改变，就调用passwdIsLegal()方法判断输入的密码格式是不是合法*/
        login_edit_setpasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                /**如果用户输入的合法，就设置login_setpasswd_ok为可点击的，并且设置背景颜色*/
                boolean isLegal = judgePasswdIsLegal.passwdIsLegal(s.toString());
                if (isLegal) {
                    login_setpasswd_ok.setClickable(true);
                    login_setpasswd_ok.setTextColor(getResources().getColor(R.color.text_color_yes));
                    login_setpasswd_ok.setBackgroundColor(getResources().getColor(R.color.btn_color_yes));
                } else {
                    login_setpasswd_ok.setClickable(false);
                    login_setpasswd_ok.setTextColor(getResources().getColor(R.color.text_color_no));
                    login_setpasswd_ok.setBackgroundColor(getResources().getColor(R.color.btn_color_no));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_setpasswd_ok:
                /**一旦用户输入密码成功，有以下工作需要完成：
                 * 0. 判断两个输入框是否为空
                 * 1. 将用户的手机号码和密码存储起来，对密码进行相应的加工
                 * 2. 调用ActivityCollector中的finishAllActivity()方法，将用户注册这三个步骤需要的
                 *     所有Activity进行销毁
                 * 3.  打开登陆界面，进行用户登录*/
                if(login_edit_setpasswd.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginSetPasswdAty.this, "请输入密码", Toast.LENGTH_SHORT).show();
                }
                if(nickName.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginSetPasswdAty.this, "请输入昵称", Toast.LENGTH_SHORT).show();
                }
                if(!login_edit_setpasswd.getText().toString().trim().isEmpty() && !nickName.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginSetPasswdAty.this, "正在拼命注册中...", Toast.LENGTH_SHORT).show();
                    user = new User();
                    user.setPhone(phoneNum);
                    user.setPassword(login_edit_setpasswd.getText().toString().trim());
                    user.setNickname(nickName.getText().toString().trim());
                    BmobFile bmobFile = new BmobFile("user.png","","http://bmob-cdn-10027.b0.upaiyun.com/2017/03/26/5c510f47ff85485cb0017934e3dd17c1.png");
                    user.setImg(bmobFile);
                    insertUser();
                }
                break;

            case R.id.back_imgbtn:
                finish();
                break;

            default:
                break;
        }
    }

    private void insertUser(){
        user.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    Toast.makeText(LoginSetPasswdAty.this, "注册成功！", Toast.LENGTH_SHORT).show();
                    user.setObjectId(s);
                    saveAccount();
                    ActivityCollector.finishAllActivity();
                    Intent intent = new Intent(LoginSetPasswdAty.this,MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }else{
                    Toast.makeText(LoginSetPasswdAty.this, "注册失败！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**将用户的手机号码和密码保存起来,从LoginGetCodesAty方法中传递过来的phoneNums*/
    private void saveAccount(){
        SharedPreferences.Editor editor = getSharedPreferences("account",
                MODE_PRIVATE).edit();
        editor.clear();
        editor.putString("id", user.getObjectId());
        editor.putString("phone",user.getPhone());
        editor.putString("nickname",user.getNickname());
        editor.putString("password", user.getPassword());
        editor.putString("url",user.getImg().getFileUrl());
        editor.putString("imgName", user.getImg().getFilename());
        editor.apply();
    }

    /**在finish()方法中，将Activity从activityCollector中删除*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
