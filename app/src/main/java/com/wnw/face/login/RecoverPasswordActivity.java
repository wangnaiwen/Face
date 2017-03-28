package com.wnw.face.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.wnw.face.R;

/**
 * Created by wxx on 2017/3/7.
 */

public class RecoverPasswordActivity extends Activity implements View.OnClickListener{

    /**定义Button中按钮的背景颜色，字体颜色*/
    /*int text_color_no = Color.parseColor("#9e9c9c");
    int text_color_yes = Color.parseColor("#ffffff");
    int btn_color_no = Color.parseColor("#ebd7d5d5");
    int btn_color_yes = Color.parseColor("#ce1717");*/

    /**下一步的按钮*/
    private Button nextStep;

    /**Title栏中后退的按钮*/
    private ImageView backRP;

    /**输入手机号码的EditText*/
    private EditText phoneEdit;


    /**定义一个JudgePhoneNums对象，调用其judgePhoneNums(String phoneNums)方法
     * 判断用户输入的字符串是不是一个手机号码,每次EditText发生改变,都会调用这个方法
     * 然后根据返回,设置login_editnum_next_btn是可点击还是不可点击的状态
     * */
    private JudgePhoneNums judgePhoneNums = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_recover_phone_input);
        init();
    }

    /**初始化组件，并且为主键添加事件监听*/
    private void init(){
        judgePhoneNums = new JudgePhoneNums();
        /**将本地的Activity添加到ActivityCollector中*/
        ActivityCollector.addActivity(this);

        nextStep = (Button)findViewById(R.id.password_recover_editnum_next_btn);
        backRP = (ImageView)findViewById(R.id.back_password_recover);
        phoneEdit = (EditText)findViewById(R.id.password_recover_edit_number);

        nextStep.setOnClickListener(this);
        backRP.setOnClickListener(this);

        /**为login_edit_number添加监听事件，如果发生改变，就调用judgePhoneNums(String phoneNums)*/
        phoneEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Boolean isPhoneNums = judgePhoneNums.judgePhoneNums(s.toString());
                /**如果用户输入的是一个手机号码，并且同意注册文件，则设置login_editnum_next_btn这个按钮为可点击的状态
                 * 如果不是，则设置为不可点击的状态,并设置相应的背景色
                 * */
                if(isPhoneNums){
                    nextStep.setClickable(true);
                    nextStep.setTextColor(getResources().getColor(R.color.text_color_yes));
                    nextStep.setBackgroundColor(getResources().getColor(R.color.btn_color_yes));
                }else{
                    nextStep.setClickable(false);
                    nextStep.setTextColor(getResources().getColor(R.color.text_color_no));
                    nextStep.setBackgroundColor(getResources().getColor(R.color.btn_color_no));
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
            case R.id.password_recover_editnum_next_btn:
                /**在这里应该把手机号码这个参数传递过去给LoginGetCodesAty*/
                String phoneNumbers = phoneEdit.getText().toString();
                Intent intent = new Intent(this,RecoverPasswordGetCodeActivity.class);
                intent.putExtra("phone",phoneNumbers);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.back_password_recover:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            default:
                break;
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
