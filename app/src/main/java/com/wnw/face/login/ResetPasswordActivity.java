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

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;


/**
 * Created by wxx on 2017/3/7.
 */

public class ResetPasswordActivity  extends Activity implements
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

    /**password input again*/
    private EditText login_edit_setpasswd_again;

    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

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

        back_imgbtn = (ImageView)findViewById(R.id.back_password_recover);
        login_setpasswd_ok = (Button)findViewById(R.id.password_recover_setpasswd_ok);
        login_edit_setpasswd = (EditText)findViewById(R.id.password_recover_edit_setpasswd);
        login_edit_setpasswd_again = (EditText)findViewById(R.id.password_recover_edit_setpasswd_again);

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
            case R.id.password_recover_setpasswd_ok:
                if(login_edit_setpasswd.getText().toString().trim().isEmpty()){
                    Toast.makeText(ResetPasswordActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
                }
                else if(login_edit_setpasswd_again.getText().toString().trim().isEmpty()){
                    Toast.makeText(ResetPasswordActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                }else if(judgePasswdEqual()){
                    //去更新密码
                    Toast.makeText(this, "正在拼命中...",Toast.LENGTH_SHORT).show();
                    user = new User();
                    user.setPhone(phoneNum);
                    user.setPassword(login_edit_setpasswd.getText().toString().trim());
                    updatePassword();
                }else{
                    Toast.makeText(this, "输入的两次密码不一致",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.back_password_recover:
                finish();
                break;

            default:
                break;
        }
    }

    private User newUser;
    boolean insertSuccess = false;
    private void updatePassword(){
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("phone",user.getPhone());
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    if(object != null){
                        int length = object.size();
                        if(length > 0){
                            newUser = object.get(0);
                            insertSuccess = true;
                        }else {
                            Toast.makeText(ResetPasswordActivity.this, "该手机号没有注册过账号", Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });

        if(insertSuccess){
            newUser.setPassword(user.getPassword());
            newUser.update(newUser.getObjectId(), new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        Toast.makeText(ResetPasswordActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                        saveAccount();
                        ActivityCollector.finishAllActivity();
                        Intent intent = new Intent(ResetPasswordActivity.this,MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }else{
                        Log.i("bmob","更新失败："+e.getMessage()+","+e.getErrorCode());
                    }
                }
            });
        }
    }

    /**将用户的手机号码和密码保存起来,从LoginGetCodesAty方法中传递过来的phoneNums*/
    private void saveAccount(){
        SharedPreferences.Editor editor = getSharedPreferences("account",
                MODE_PRIVATE).edit();
        editor.clear();
        editor.putString("id", newUser.getObjectId());
        editor.putString("phone",newUser.getPhone());
        editor.putString("nickname",newUser.getNickname());
        editor.putString("password", newUser.getPassword());
        editor.putString("url",newUser.getImg().getFileUrl());
        editor.putString("imgName", newUser.getImg().getFilename());
        editor.apply();
    }



    /**
     * 判断两次密码输入是否相同
     *
     * */
    private boolean judgePasswdEqual(){
        return login_edit_setpasswd.getText().toString().trim().equals(login_edit_setpasswd_again.getText().toString().trim());
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
