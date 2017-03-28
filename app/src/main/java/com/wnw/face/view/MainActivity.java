package com.wnw.face.view;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.megvii.cloud.http.CommonOperate;
import com.megvii.cloud.http.Response;
import com.wnw.face.R;
import com.wnw.face.bean.User;
import com.wnw.face.login.ActivityCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.getExternalStorageDirectory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener{

    private static final int PICK_CODE =1;   //打开相机的CODE

    private ImageView myPhoto;            //展示的图片ImageView
    private View mWaitting;               //额外画上图片的信息展示框
    private String ImagePath=null;        //拍照，从相册选择得到的图片地址
    private Paint mypaint;                //画笔
    private Bitmap myBitmapImage;         //展示图片
    private FloatingActionButton fab;     //提交的圆形Buttom

    private NavigationView navigationView;  //侧边滑动的view
    private LinearLayout pickPhoto;   //可以选择图片的界面
    private ImageView camera;         //选择相机
    private ImageView album;          //选择相册

    private CircleImageView userImg;  //头像
    private TextView nickNameView;    //昵称
    private TextView phoneView;       //电话号码
    private ImageView editNickName;   //编辑昵称

    private User user;            // 登录的用户
    private String url = null;    //用户的头像url地址
    private String imgName = null;//用户的头像name名字

    //face++需要用到的东西
    private String key = "hOZijVKEMrfV1kx9JpDb2U4pTxD3M2in";
    private String secret = "i0i0HZBDTbQ4u54MTVHgvF3mLL45gCuh";
    private String attr = "gender,age,smiling";

    private static final int TAKE_PHOTO_REQUEST_CODE = 1;   //拍照Code
    private static final int PICK_PHONE_REQUEST_CODE = 2;   //Write Code
    private boolean  isOpenCamera;                          // true打开相机，false 打开相册

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCollector.addActivity(this);

        //获取登录的User
        getUser();
        //初始化画笔
        mypaint=new Paint();
        //初始化View
        initViews();
    }

    //初始化一些View，菜单等
    private void initViews()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        myPhoto=(ImageView)findViewById(R.id.id_photo);
        mWaitting=findViewById(R.id.id_waitting);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        userImg = (CircleImageView)navigationView.getHeaderView(0).findViewById(R.id.icon_user);
        nickNameView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.username);
        phoneView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.phone);
        editNickName = (ImageView)navigationView.getHeaderView(0).findViewById(R.id.edit_nickname);
        editNickName.setOnClickListener(this);

        pickPhoto = (LinearLayout)findViewById(R.id.pick_phone);
        camera = (ImageView)findViewById(R.id.camera);
        album = (ImageView)findViewById(R.id.album);

        camera.setOnClickListener(this);
        album.setOnClickListener(this);

        //设置NavigationView里面的参数：昵称，电话，头像
        phoneView.setText(user.getPhone());
        nickNameView.setText(user.getNickname());
        Glide.with(this).load(url).into(userImg);

        userImg.setOnClickListener(this);
    }

    //获取本地保存的登录用户信息
    private void getUser(){
        user = new User();
        SharedPreferences preferences = getSharedPreferences("account", MODE_PRIVATE);
        user.setObjectId(preferences.getString("id", ""));
        user.setPhone(preferences.getString("phone", ""));
        user.setNickname(preferences.getString("nickname", ""));
        user.setPassword(preferences.getString("password", ""));
        url = preferences.getString("url", "");
        imgName = preferences.getString("imgName", "");
    }

    // 初始化，加载菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //菜单选中监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open_camera) {
            //检车相机权限
            isOpenCamera = true;
            checkCameraPermission();
            return true;
        }else if (id == R.id.action_open_album) {
            //检查相册权限
            isOpenCamera = false;
            checkAlbumPermission();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_setting) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fab:
                //显示进度条圆形
                mWaitting.setVisibility(View.VISIBLE);
                Toast.makeText(this, "正在努力中...", Toast.LENGTH_SHORT).show();
                //这里需要注意判断用户是否没有选择图片直接点击了detect按钮
                //否则会报一个空指针异常而造成程序崩溃
                if(ImagePath!=null&&!ImagePath.trim().equals(""))
                {
                    //如果不是直接点击的图片则压缩当前选中的图片
                    resizePhoto();
                }else
                {
                    Toast.makeText(this, "没有添加任何图片", Toast.LENGTH_SHORT).show();
                    break;
                }
                //发送图片到Face++去检测
                sendPost(myBitmapImage);
                break;
            case R.id.icon_user:
                //用户头像点击
                Intent intent = new Intent(this, ImgUploadActivity.class);
                startActivityForResult(intent, 2);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.edit_nickname:
                //用户昵称点击
                Intent intent1 = new Intent(this, EditNickNameActivity.class);
                intent1.putExtra("id",user.getObjectId());
                intent1.putExtra("nickname", user.getNickname());
                startActivityForResult(intent1, 4);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.camera:
                //相机点击
                isOpenCamera = true;
                checkCameraPermission();
                break;
            case R.id.album:
                //相册点击
                isOpenCamera = false;
                checkAlbumPermission();
                break;
            default:
                break;
        }
    }

    /**
     * 1. 拍照需要获得CAMERA和Write权限
     * 2. 相册需要获得Write权限
     * */

    //检查相机权限
    private void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    TAKE_PHOTO_REQUEST_CODE);
        }
        else {
            //如果相机权限已经打开，检查Write权限
            checkAlbumPermission();
        }
    }

    //检查相册Write权限
    private void checkAlbumPermission(){
       if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PICK_PHONE_REQUEST_CODE);
        }else{
           //因为打开相机也需要这个权限，所以要判断是打开相机还是打开相册
           if(isOpenCamera){
               openCamera();
           }else {
               openAlbum();
           }
       }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //取得拍照权限：查看Write权限
                checkAlbumPermission();
            }else {
                Toast.makeText(MainActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICK_PHONE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //取得Write权限,判断是拍照，还是调用相册
                if(isOpenCamera){
                    openCamera();
                }else{
                    openAlbum();
                }
            }else {
                Toast.makeText(MainActivity.this, "权限拒绝", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //打开相册
    private void openAlbum(){
        //获取系统选择图片intent
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        //开启选择图片功能响应码为PICK_CODE
        startActivityForResult(intent,PICK_CODE);
    }

    //打开相机
    private void openCamera(){
        //创建File对象，用于存储拍照后的照片
        new Thread(new Runnable() {
            @Override
            public void run() {
                File outputImage = new File(getExternalStorageDirectory() + "/pictures", "output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what =  1;
                message.obj = outputImage;
                handler.sendMessage(message);
            }
        }).start();
    }

    /**处理异步操作*/
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:     //创建一个文件来保存拍照得到的图片
                    Uri uri = null;
                    File outputImage = (File)msg.obj;
                    if(Build.VERSION.SDK_INT >= 24){
                        uri = FileProvider.getUriForFile(MainActivity.this,"com.example.cameraalbumtest.fileprovider", outputImage);
                    }else {
                        uri = Uri.fromFile(outputImage);
                    }
                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, 3);
                    break;
                case 2:   //压缩图片完成
                    pickPhoto.setVisibility(View.GONE);
                    myPhoto.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                    myPhoto.setImageBitmap(myBitmapImage);
                    fab.setAnimation(new AlphaAnimation(0, 1));
                    break;
            }
        }
    };

    //设置响应intent请求
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode==PICK_CODE)  //选择图片返回
        {
            if(intent!=null)
            {
               Uri uri =intent.getData();
                /**
                 * 获取图片路径,获取所有图片资源,设置指针获得一个ContentResolver的实例,
                 * 不同的手机，获取cursor的方式不同
                 * 如果返回cursor返回的为空， 则通过另一种方式返回
                 * */
                Cursor cursor = getContentResolver().query(uri,null,null,null,null);
                if(cursor == null){

                    File myFile = new File(uri.getPath());
                    Uri selectedImage=getImageContentUri(this,myFile);
                    cursor = getContentResolver().query(selectedImage,null,null,null,null);
                }

                cursor.moveToFirst();
                //返回索引项位置
                int index=cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                //返回索引项路径
                ImagePath=cursor.getString(index);
                cursor.close();
                //这个jar包要求请求的图片大小不得超过3m所以要进行一个压缩图片操作
                resizePhoto();
            }
        }else if(requestCode == 2){//获取头像的url路径，返回图片
            if(resultCode == RESULT_OK){
                url = intent.getStringExtra("url");
                imgName = intent.getStringExtra("imgName");
                Glide.with(this).load(url).error(R.mipmap.error).into(userImg);
            }
        }else if (requestCode == 4){   //编辑昵称返回
            if(resultCode == RESULT_OK){
                user.setNickname(intent.getStringExtra("nickname"));
                nickNameView.setText(user.getNickname());
            }
        }else if(requestCode == 3){//拍照返回得到图片
            if(resultCode == RESULT_OK){
                try{
                    if(intent != null && intent.getData() != null){

                    }
                    ImagePath = getExternalStorageDirectory() + "/pictures/output_image.jpg";
                    resizePhoto();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 不同的手机，获取cursor的方式不一样
     * */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    //压缩图片,开多线程去操作，防止主线程阻塞
    private void resizePhoto() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //得到BitmapFactory的操作权
                BitmapFactory.Options options = new BitmapFactory.Options();
                // 如果设置为 true ，不获取图片，不分配内存，但会返回图片的高宽度信息。
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(ImagePath,options);
                //计算宽高要尽可能小于1024
                double ratio=Math.max(options.outWidth*1.0d/1024f,options.outHeight*1.0d/1024f);
                //设置图片缩放的倍数。假如设为 4 ，则宽和高都为原来的 1/4 ，则图是原来的 1/16 。
                options.inSampleSize=(int)Math.ceil(ratio);
                //我们这里并想让他显示图片所以这里要置为false
                options.inJustDecodeBounds=false;
                //利用Options的这些值就可以高效的得到一幅缩略图。
                myBitmapImage= BitmapFactory.decodeFile(ImagePath,options);

                //交给Handler处理
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
            }
        }).start();
    }

    //发送请求
    private void sendPost(final Bitmap bitmap){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CommonOperate commonOperate = new CommonOperate(key,secret,false);
                //从0，0点挖取整个视图，后两个参数是目标大小
                Bitmap bitmapsmall = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
                //这里api要求传入一个字节数组数据，因此要用字节数组输出流
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    /*Bitmap.compress()方法可以用于将Bitmap-->byte[]
                      既将位图的压缩到指定的OutputStream。如果返回true，
                      位图可以通过传递一个相应的InputStream BitmapFactory.decodeStream（重建）
                      第一个参数可设置JPEG或PNG格式,第二个参数是图片质量，第三个参数是一个流信息*/
                bitmapsmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] arrays=stream.toByteArray();
                try {
                    Response response =commonOperate.detectByte(arrays, 0, attr);

                    Message message=Message.obtain();
                    message.what=MSG_SUCESS;
                    message.obj=response;
                    myhandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static final int MSG_SUCESS=11;
    private static final int MSG_ERROR=22;
    private Handler myhandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case MSG_SUCESS:
                    //关闭缓冲条
                    mWaitting.setVisibility(View.GONE);
                    //拿到新线程中返回的JSONObject数据
                    Response response= (Response) msg.obj;
                    //准备Bitmap，这里会解析JSONObject传回的数据
                   byte[]  datas = response.getContent();
                    String s = new String(datas);
                    Log.d("wnw", s);
                    try{
                        JSONObject object = new JSONObject(s);
                        prepareBitmap(object);
                        //让主线程的相框刷新
                        myPhoto.setImageBitmap(myBitmapImage);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case MSG_ERROR:
                    mWaitting.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "请求失败，查看网络", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //处理图片
    private void prepareBitmap(JSONObject JS) {
        //新建一个Bitmap使用它作为Canvas操作的对象
        Bitmap bitmap=Bitmap.createBitmap(myBitmapImage.getWidth(),myBitmapImage.getHeight(),myBitmapImage.getConfig());
        //实例化一块画布
        Canvas canvas=new Canvas(bitmap);
        //把原图先画到画布上面
        canvas.drawBitmap(myBitmapImage, 0, 0, null);
        //解析传回的JSONObject数据
        try {
            //JSONObject中包含着众多JSONArray，但是我们这里需要关键字为face的数组中的信息
            JSONArray faces=JS.getJSONArray("faces");
            //获取得到几个人脸
            int faceCount=faces.length();
            //让提示文本显示人脸数
            //下面对每一张人脸都进行单独的信息绘制
            Log.d("wnw"," faceCount:"  + faceCount);
            if(faceCount == 0){
                Toast.makeText(this, "当前图片不能识别人脸", Toast.LENGTH_SHORT).show();
            }
            for(int i=0;i<faceCount;i++)
            {
                //拿到每张人脸的信息
                JSONObject face=faces.getJSONObject(i);
                //拿到人脸的详细位置信息
                JSONObject position=face.getJSONObject("face_rectangle");
                float x=(float)position.getDouble("left");
                float y=(float)position.getDouble("top");
                float w=(float)position.getDouble("width");
                float h=(float)position.getDouble("height");

                //设置画笔颜色
                mypaint.setColor(0xffffffff);
                //设置画笔宽度
                mypaint.setStrokeWidth(3);
                //绘制一个矩形框

                canvas.drawLine(x,y,x+w,y,mypaint);
                canvas.drawLine(x,y,x,y+h,mypaint);
                canvas.drawLine(x,y+h,x+w,y+h,mypaint);
                canvas.drawLine(x+w,y,x+w,y+h,mypaint);

                //得到年龄信息
                int age = face.getJSONObject("attributes").getJSONObject("age").getInt("value");
                //得到性别信息：Female或者Male
                String gender = face.getJSONObject("attributes").getJSONObject("gender").getString("value");
                //得到微笑信息：笑容分析结果，value的值为一个[0,100]的浮点数，小数点后3位有效数字，数值大表示笑程度高。
                // threshold代表笑容的阈值，超过该阈值认为有笑容。
                float threshold = (float)face.getJSONObject("attributes").getJSONObject("smile").getDouble("threshold");
                float smileValue = (float)face.getJSONObject("attributes") .getJSONObject("smile").getDouble("value");
                String mood ="";

                if(smileValue - threshold > 50){
                    mood = "爆笑";
                }else if(smileValue - threshold > 10){
                    mood = "微笑";
                }else if(smileValue - threshold > 0){
                    mood = "笑";
                }else if(smileValue - threshold < 0){
                    mood = "不笑";
                }else if(smileValue - threshold < -10){
                    mood = "难受";
                }else if(smileValue - threshold < -40){
                    mood = "想哭";
                }

                //现在要把得到的文字信息转化为一个图像信息，我们写一个专门的函数来处理
                Bitmap ageBitmap=buildAgeBitmap(age,mood,("Male").equals(gender));
                //进行图片提示气泡的缩放，这个很有必要，当人脸很小的时候我们需要把提示气泡也变小
                int agewidth=ageBitmap.getWidth();
                int agehight=ageBitmap.getHeight();
               if(bitmap.getWidth()<myPhoto.getWidth()&&bitmap.getHeight()<myPhoto.getHeight())
                {
                    //设置缩放比
                    float ratio=Math.max(bitmap.getWidth()*1.0f/
                            myPhoto.getWidth(),bitmap.getHeight()*1.0f/myPhoto.getHeight());

                    //完成缩放
                    ageBitmap=Bitmap.createScaledBitmap(ageBitmap,(int)(agewidth*ratio*0.9),(int)(agehight*ratio*0.9),false);
                }
                //在画布上画出提示气泡
                if(y < 100){  //头像太靠上
                    canvas.drawBitmap(ageBitmap,x,y+ageBitmap.getHeight()+30,null);
                }else{
                    canvas.drawBitmap(ageBitmap,x,y-ageBitmap.getHeight()-30,null);
                }
                //得到新的bitmap
                myBitmapImage=bitmap;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Bitmap buildAgeBitmap(int age, String mood,boolean isMale) {
        //这里要将文字信息转化为图像信息，如果拿Canvas直接画的话操作量太大
        //因此这里有一些技巧，将提示气泡设置成一个TextView，他的背景就是气泡的背景
        //他的内容左侧是显示性别的图片右侧是年龄
        TextView tv= (TextView) mWaitting.findViewById(R.id.id_age_and_gender);
        //这里要记得显示数字的时候后面最好跟一个""不然有时候会显示不出来
        if(isMale)
        {
            //判断性别
            tv.setText("男 \n\r"+age + "岁\n\r" + mood);
        }else
        {
            tv.setText("女 \n\r"+age + "岁\n\r" + mood);
        }

        //使用setDrawingCacheEnabled(boolean flag)提高绘图速度
        //View组件显示的内容可以通过cache机制保存为bitmap
        //这里要获取它的cache先要通过setDrawingCacheEnable方法把cache开启，
        // 然后再调用getDrawingCache方法就可 以获得view的cache图片了。
        tv.setDrawingCacheEnabled(true);
        Bitmap bitmap=Bitmap.createBitmap(tv.getDrawingCache());
        //关闭许可
        tv.destroyDrawingCache();
        return bitmap;
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            /**
             * 当侧边栏处于展开状态时，按下返回键，关闭侧边栏
             * */

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }

            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
