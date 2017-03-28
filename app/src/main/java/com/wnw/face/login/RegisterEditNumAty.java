package com.wnw.face.login;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wnw.face.R;
import com.wnw.face.bean.User;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;


/**
 * Created by wxx on 2017/3/26.
 * 这个Activity是在快速注册中的第一步：输入手机号码
 * 在这里需要完成的功能如下：
 * 1. 验证用户在EditText中输入的是不是手机号码
 * 2. 在用户点击下一步时，调用第三方发送短信的key向用户发送验证码
 * 3. 跳转到LoginGetCodesAty这个Activity中
 */
public class RegisterEditNumAty extends Activity implements View.OnClickListener{

    /**定义Button中按钮的背景颜色，字体颜色*/
    /*int text_color_no = Color.parseColor("#9e9c9c");
    int text_color_yes = Color.parseColor("#ffffff");
    int btn_color_no = Color.parseColor("#ebd7d5d5");
    int btn_color_yes = Color.parseColor("#ce1717");*/

    /**下一步的按钮*/
    private Button login_editnum_next_btn;

    /**Title栏中后退的按钮*/
    private ImageView back_imgbtn;

    /**输入手机号码的EditText*/
    private EditText login_edit_number;

    /**用户是否同意注册文件的CheckBox*/
    private CheckBox login_checkbox = null;

    /**CheckBox右边的注册协议TextView，并为其添加事件监听*/
    private TextView login_protocol = null;

    /**定义一个JudgePhoneNums对象，调用其judgePhoneNums(String phoneNums)方法
     * 判断用户输入的字符串是不是一个手机号码,每次EditText发生改变,都会调用这个方法
     * 然后根据返回,设置login_editnum_next_btn是可点击还是不可点击的状态
     * */
    private JudgePhoneNums judgePhoneNums = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_editnum);
        init();
    }

    /**初始化组件，并且为主键添加事件监听*/
    private void init(){
        judgePhoneNums = new JudgePhoneNums();
        /**将本地的Activity添加到ActivityCollector中*/
        ActivityCollector.addActivity(this);

        login_editnum_next_btn = (Button)findViewById(R.id.login_editnum_next_btn);
        back_imgbtn = (ImageView)findViewById(R.id.back_imgbtn);
        login_edit_number = (EditText)findViewById(R.id.login_edit_number);
        login_checkbox = (CheckBox)findViewById(R.id.login_checkbox);
        login_protocol = (TextView)findViewById(R.id.login_protocol);

        login_editnum_next_btn.setOnClickListener(this);
        back_imgbtn.setOnClickListener(this);
        login_protocol.setOnClickListener(this);

        /**为checkbox添加事件监听，如果是通过判断isCheck来设置相应的操作,它和EditText组成联合监听事件*/
        login_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                /**判断用户输入的String是不是一个手机号码*/
                boolean isPhoneNumbers = judgePhoneNums.judgePhoneNums(login_edit_number.getText().toString());
                if(isChecked && isPhoneNumbers){
                    login_editnum_next_btn.setClickable(true);
                    login_editnum_next_btn.setTextColor(getResources().getColor(R.color.text_color_yes));
                    login_editnum_next_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_yes));
                }else{
                    login_editnum_next_btn.setClickable(false);
                    login_editnum_next_btn.setTextColor(getResources().getColor(R.color.text_color_no));
                    login_editnum_next_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_no));
                }
            }
        });

        /**为login_edit_number添加监听事件，如果发生改变，就调用judgePhoneNums(String phoneNums)*/
        login_edit_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Boolean isPhoneNums = judgePhoneNums.judgePhoneNums(s.toString());
                /**如果用户输入的是一个手机号码，并且同意注册文件，则设置login_editnum_next_btn这个按钮为可点击的状态
                 * 如果不是，则设置为不可点击的状态,并设置相应的背景色
                 * */
                if(isPhoneNums && login_checkbox.isChecked()){
                    login_editnum_next_btn.setClickable(true);
                    login_editnum_next_btn.setTextColor(getResources().getColor(R.color.text_color_yes));
                    login_editnum_next_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_yes));
                }else{
                    login_editnum_next_btn.setClickable(false);
                    login_editnum_next_btn.setTextColor(getResources().getColor(R.color.text_color_no));
                    login_editnum_next_btn.setBackgroundColor(getResources().getColor(R.color.btn_color_no));
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    /**监听用户的动作，并且根据动作做出对应的响应*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_editnum_next_btn:
                checkRegistered();
                break;
            case R.id.login_protocol:
                /**在这里打开用户注册协议书*/

                break;
            case R.id.back_imgbtn:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            default:
                break;
        }
    }

    //验证号码是否注册过
    private boolean isRegistered = false;
    private void checkRegistered(){
        String p = login_edit_number.getText().toString().trim();
        //执行查询方法
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("phone",p);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    if (object == null){
                        isRegistered = false;
                        getCode();
                    }else {
                        if( object.size() > 0){
                            isRegistered = true;
                            getCode();
                        }else{
                            isRegistered = false;
                            getCode();
                        }
                    }
                }else{
                    e.printStackTrace();
                }
            }
        });
    }

    //没有注册，就跳转继续注册
    private void getCode(){
        if(isRegistered){
            //注册过
            Toast.makeText(this, "该手机已经注册过", Toast.LENGTH_SHORT).show();
        }else {
            /**在这里应该把手机号码这个参数传递过去给LoginGetCodesAty*/

            String phoneNumbers = login_edit_number.getText().toString();
            Intent intent = new Intent(this,LoginGetCodesAty.class);
            intent.putExtra("phone",phoneNumbers);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
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
