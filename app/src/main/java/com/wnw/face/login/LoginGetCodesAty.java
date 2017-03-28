package com.wnw.face.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wnw.face.R;

import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by wxx on 2017/3/26.
 * 这个Activity是由LoginEditNumAty跳转而来，在这个Activity中
 * 完成的功能有以下：
 * 1. 从LoginEditNumAty跳转而来，在OnResume方法中应该开启倒计时功能,一开始是不可点击的，
 *    计时结束后设置为可点击，并且处理相应的事件
 * 2. 在这里用ContentProvider来是先读取验证码功能，需要具备权限，并将读取到的验证码填入EditText中
 * 3. 根据第三方的接口判断验证码是否正确
 * 4. 监听用户的按键信息：返回键，下一步键，重新获取验证码键
 * 5. 应该在onCreate()方法中调用Intent来获取手机号码，并且获取手机验证码
 *    这样避免了由于该Activity是从LoginSetPasswdAty中返回从而产生逻辑冲突
 */

public class LoginGetCodesAty extends Activity implements View.OnClickListener{

    /**定义Button中按钮的背景颜色，字体颜色*/
   /* int text_color_no = Color.parseColor("#9e9c9c");
    int text_color_yes = Color.parseColor("#ffffff");
    int btn_color_no = Color.parseColor("#ebd7d5d5");
    int btn_color_yes = Color.parseColor("#ce1717");*/

    /**第三方Mob短信验证码的key*/
    String  appKey = "1c64fa43eb176";
    String appSecret = "0ccdd6d882841ad60ed495206fc566da";

    /**返回键*/
    private ImageView back_imgbtn;

    /**重新获取验证码键*/
    private Button login_getcode_again_btn;

    /**下一步键*/
    private Button login_getcodes_next_btn;

    /**EditText*/
    private EditText login_edit_codes ;
    /**TextView,显示：请输入某某号码收到的验证码*/
    private TextView login_text_display;

    /**定义一个电话字符串*/
    private String phoneNums;

    /**ActivityCollection对象*/

    /**定义倒计时的时间总数,120second*/
    int times = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_getcodes);
        init();          //初始化组件
        displayText();   //获取手机号码和显示TextView中的内容
        sendKey();       //当从LoginEditNumAty跳转过来的时候，在调用onCreate()方法的时候，就开始发送短信验证码
    }

    /**初始化组件函数*/
    private void init(){

        /**将本地的Activity添加到ActivityCollector中*/
        ActivityCollector.addActivity(this);

        back_imgbtn = (ImageView) findViewById(R.id.back_imgbtn);
        login_getcode_again_btn = (Button)findViewById(R.id.login_getcode_again_btn);
        login_getcodes_next_btn = (Button)findViewById(R.id.login_getcodes_next_btn);
        login_text_display = (TextView)findViewById(R.id.login_text_display);
        login_edit_codes = (EditText)findViewById(R.id.login_edit_codes);

        back_imgbtn.setOnClickListener(this);
        login_getcodes_next_btn.setOnClickListener(this);
        login_getcode_again_btn.setOnClickListener(this);

        /**在这里给EditText添加改变监听：
         * 1. 验证用户输入位数是4
         * 2. 验证用户输入的是4个数字*/

        login_edit_codes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /**验证用户*/
                String input_key = s.toString();
                String telRegex = "[0-9]";  //代表可以是0-9的数字
                if (!TextUtils.isEmpty(s)) {
                    //满足条件，设置下一步按钮是可点击的,否则不可点击

                    login_getcodes_next_btn.setClickable(true);
                    login_getcodes_next_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_yes));
                    login_getcodes_next_btn.setTextColor(getResources().getColor(R.color.text_color_yes));
                } else {
                    login_getcodes_next_btn.setClickable(false);
                    login_getcodes_next_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_no));
                    login_getcodes_next_btn.setTextColor(getResources().getColor(R.color.text_color_no));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        /**1. 启动短信验证码SDK*/
        SMSSDK.initSDK(this, appKey, appSecret);

        final EventHandler eventHandler = new EventHandler(){
            @Override
            public void afterEvent(int i, int i1, Object o) {
                Message message = new Message();
                message.arg1 = i;
                message.arg2 = i1;
                message.obj = o;
                handler.sendMessage(message);
            }

            @Override
            public void onRegister() {
                super.onRegister();
            }

            @Override
            public void onUnregister() {
                super.onUnregister();
            }

            @Override
            public void beforeEvent(int i, Object o) {
                super.beforeEvent(i, o);
            }
        };
        /**2. 注册回调监听接口*/
        SMSSDK.registerEventHandler(eventHandler);
    }

    /**获取从LoginEditNumAty传递过来的Intent对象，并且接收数据，在TextView中显示该号码*/
    private void displayText(){
        Intent intent = getIntent();
        this.phoneNums = intent.getStringExtra("phone");
        Log.d("wnw", phoneNums);
        String disText = "请输入"+phoneNums.substring(0,3)+"****"+phoneNums.substring(7,11)+"收到的短信验证码";
        login_text_display.setText(disText);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            /**如果在Longin_getcode*_again_btn能够点击，并且用户点击了，应该发送短信验证码*/
            case R.id.login_getcode_again_btn:
                sendKey();
                break;

            case R.id.login_getcodes_next_btn:
                /*Intent intent = new Intent(this,LoginSetPasswdAty.class);
                startActivity(intent);*/

                /**将用户接收到的验证码再次提交进行核对*/
                SMSSDK.submitVerificationCode("86",phoneNums,login_edit_codes.getText().toString());
                break;

            case R.id.back_imgbtn:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            default:
                break;
        }
    }


    /**发送验证码
     * 1.启动短信验证码SDK
     * 2. 注册回调监听接口
     * 3. 得到手机号
     * 4. 通过SDK发送短信验证码
     * 5. 把按钮设置为不可点击
     * 6. 开始倒计时
     * */
    private void sendKey(){


        /**3. 获取手机号，上门已经获取了，所以这里不需要重新获取*/

        /**4. 通过sdk发送短信验证码,这里86代表中国*/
        SMSSDK.getVerificationCode("86", phoneNums);

        /**5. 把按钮设置为不可点击*/
        login_getcode_again_btn.setClickable(false);
        login_getcode_again_btn.setText("重新发送(" + times + ")");

        /**6. 利用多线程开启倒计时*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(;times>0;times--){
                    handler.sendEmptyMessage(-9);  //代表正在倒计时
                    if(times <= 0){
                        break;
                    }
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(-8);   //代表倒计时结束
            }
        }).start();

    }

    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if(msg.what == -9){
                login_getcode_again_btn.setText("重新发送(" + times + ")");
                login_getcode_again_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_no));
                login_getcode_again_btn.setTextColor(getResources().getColor(R.color.text_color_no));
                login_getcode_again_btn.setClickable(false);

            }else if(msg.what == -8){
                login_getcode_again_btn.setText("获取验证码");
                login_getcode_again_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_yes));
                login_getcode_again_btn.setTextColor(getResources().getColor(R.color.text_color_yes));
                login_getcode_again_btn.setClickable(true);

                times = 120;
            }else {
                int event = msg.arg1;
                int result = msg.arg2;
                Object data = msg.obj;
                /**说明发送成功*/
                if(result == SMSSDK.RESULT_COMPLETE){
                    /**
                     * 有三种情况：
                     * 1. 提交验证码成功, Toast验证成功，打开LoginSetPasswdAty这个Activity
                     * 2. 正在获取验证码,Toast正在获取验证码
                     * 3. 获取验证码失败
                     * */
                    Log.d("wnw","send to it success");
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE){
                        Toast.makeText(LoginGetCodesAty.this,"验证成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginGetCodesAty.this,LoginSetPasswdAty.class);
                        intent.putExtra("phone",phoneNums);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                    }else if(event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        Log.d("wnw","EVENT_GET_VERIFICATION_CODE");
                        Toast.makeText(LoginGetCodesAty.this,"正在获取验证码",Toast.LENGTH_SHORT).show();
                    }else if(event == SMSSDK.EVENT_GET_CONTACTS){
                        Log.d("wnw","EVENT_GET_CONTACTS");
                    }else if(event == SMSSDK.EVENT_GET_FRIENDS_IN_APP){
                        Log.d("wnw","EVENT_GET_FRIENDS_IN_APP");
                    }else if(event == SMSSDK.EVENT_GET_NEW_FRIENDS_COUNT){
                        Log.d("wnw","EVENT_GET_NEW_FRIENDS_COUNT");
                    }
                    else if(event == SMSSDK.EVENT_SUBMIT_USER_INFO){
                        Log.d("wnw","EVENT_SUBMIT_USER_INFO");
                    }
                    else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        Log.d("wnw","EVENT_GET_SUPPORTED_COUNTRIES");
                    }
                    else{
                        ((Throwable)data).printStackTrace();
                    }

                }else{
                    /**在这里解析服务器返回的错误代码，根据错误代码到Mob官方上查找相应的错误*/
                    Toast.makeText(LoginGetCodesAty.this,"验证码错误",Toast.LENGTH_SHORT).show();
                    try {
                        Throwable throwable = (Throwable) data;
                        throwable.printStackTrace();
                        JSONObject object = new JSONObject(throwable.getMessage());
                        String des = object.optString("detail");//错误描述
                        int status = object.optInt("status");//错误代码
                        Log.d("wnw",status+"");
                        if (status > 0 && !TextUtils.isEmpty(des)) {
                            //Toast.makeText(LoginGetCodesAty.this, des+" "+status, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        //do something
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    /**在finish()方法中，将Activity从activityCollector中删除，同时将短信SDK取消注册*/
    @Override
    protected void onDestroy() {
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
