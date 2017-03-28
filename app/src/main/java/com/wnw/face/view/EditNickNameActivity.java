package com.wnw.face.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wnw.face.R;
import com.wnw.face.bean.User;
import com.wnw.face.login.ActivityCollector;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by wxx on 2017/3/25.
 */

public class EditNickNameActivity extends Activity implements View.OnClickListener{

    private ImageView back;
    private TextView finish;
    private EditText editNickName;

    private String id;
    private String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_edit_nickname);

        getData();
        initView();
    }

    private void initView(){
        back = (ImageView)findViewById(R.id.back_edit_nickname);
        finish = (TextView)findViewById(R.id.nickname_finish);
        editNickName = (EditText)findViewById(R.id.nickname_edit);

        editNickName.setText(nickName);
        back.setOnClickListener(this);
        finish.setOnClickListener(this);
    }

    //得到从MainActivity传递过来你的数据
    private void getData(){
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        nickName = intent.getStringExtra("nickname");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_edit_nickname:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.nickname_finish:
                if(editNickName.getText().toString().trim().isEmpty()){
                    Toast.makeText(this, "昵称不为空", Toast.LENGTH_SHORT).show();
                }else if(editNickName.getText().toString().trim().equals(nickName)){
                    Toast.makeText(this, "昵称没有发生改变", Toast.LENGTH_SHORT).show();
                }else {
                    saveNickName();
                }
                break;
            default:

                break;
        }
    }

    private void saveNickName(){
        User user = new User();
        user.setNickname(editNickName.getText().toString().trim());
        user.update(id, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(EditNickNameActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    nickName = editNickName.getText().toString().trim();

                    //将修改过的数据保存SharePreference中
                    SharedPreferences sharedPreferences = getSharedPreferences("account",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("nickname", nickName);
                    editor.apply();

                    //返回数据给MainActivity
                    Intent intent = new Intent();
                    intent.putExtra("nickname", nickName);
                    setResult(RESULT_OK, intent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                }else{
                    e.printStackTrace();
                    Toast.makeText(EditNickNameActivity.this, "更新失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //监听返回键被按下
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    //Activity被销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
