package com.personalfinance.app.User;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.personalfinance.app.CS_Data.Data_ZIP;
import com.personalfinance.app.Config.AppNetConfig;
import com.personalfinance.app.MainActivity;
import com.personalfinance.app.R;
import com.personalfinance.app.Util.HttpUtil;
import com.personalfinance.app.Util.PictureFormatUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserCenter extends AppCompatActivity  implements View.OnClickListener{

    SQLiteDatabase db;
    final String DATABASE_PATH = "data/data/" + "com.personalfinance.app" + "/databases/personal.db";
    private String Username;
    private Drawable Userheadportrait;
    private Intent intent;
    private Cursor cursor;

    private TextView userCenter_back, close_account, userCenter_name;
    private RelativeLayout logout, userCenter_headportraitR;
    private ImageView userCenter_headportrait;


    private Dialog Camera_Ablumdialog;
    private Button[] Camera_Ablumbutton = new Button[3];
    private Uri imageUri, cropImageUri;
    private Thread database;

    private final static int TAKE_PHOTO = 6;
    private final static int CHOOSE_PHOTO = 7;
    private final static int CROP_RESULT_CODE =8;

    private Dialog LoadingDialog;
    private String NetWork_Code;
    private final static int Close_success = 1;
    private final static int Close_fail = 2;
    private final static int Close_havent = 3;
    private final static int Logout_Datasync = 4;
    private final static int Exit_Activity =5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Close_success:
                    loadDialogUtils.closeDialog(LoadingDialog);
                    Toast.makeText(UserCenter.this, "注销成功", Toast.LENGTH_SHORT).show();
                    intent = new Intent(UserCenter.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case Close_fail:
                    loadDialogUtils.closeDialog(LoadingDialog);
                    Toast.makeText(UserCenter.this, "注销失败", Toast.LENGTH_SHORT).show();
                    break;
                case Close_havent:
                    loadDialogUtils.closeDialog(LoadingDialog);
                    Toast.makeText(UserCenter.this, "注销失败,不存在该用户", Toast.LENGTH_SHORT).show();
                    break;
                case Logout_Datasync://数据进行同步
                    JSONArray jsonArray = (JSONArray) msg.obj;
                   // Log.d("TAG1", "handler进行数据同步");
                    Data_sync(jsonArray);
                    break;
                case Exit_Activity://退出该活动
                   // Log.d("TAG1", "本地更改登录状态完毕");
                    intent = new Intent(UserCenter.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usercenter);
      // db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        intent = getIntent();
        Username = intent.getStringExtra("Username");
        Userheadportrait = PictureFormatUtil.Bytes2Drawable(getResources(), intent.getByteArrayExtra("Headportrait"));

        userCenter_back=(TextView)findViewById(R.id.userCenter_back);
        userCenter_name = (TextView) findViewById(R.id.usercenter_username);
        userCenter_headportrait = (ImageView) findViewById(R.id.userCenter_headportrait);
        userCenter_headportraitR = (RelativeLayout) findViewById(R.id.userCenter_headportraitR);
        logout = (RelativeLayout) findViewById(R.id.logout);
        close_account = (TextView) findViewById(R.id.close_account);

        userCenter_name.setText(Username);
        userCenter_headportrait.setImageDrawable(Userheadportrait);
        userCenter_back.setOnClickListener(this);
        userCenter_headportraitR.setOnClickListener(this);
        logout.setOnClickListener(this);
        close_account.setOnClickListener(this);
    }
    public void onClick(View v){
        Log.d("TAGa","判断");
        if(database!=null){
            Log.d("TAGa","判断为空");
        }else{
            Log.d("TAGa","判断不为空");
        }
        //while(database.isAlive()){}
        switch (v.getId()){
            case R.id.userCenter_back://返回主页面
                intent = new Intent(UserCenter.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.logout://退出登录
                setLogout();
                break;
            case R.id.close_account://注销账号
                //联网进行用户注销，将服务器用户注销
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("温馨提示");
                builder.setMessage("你确定要注销用户么？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setClose_account();
                    }
                });
                builder.setNegativeButton("手滑了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.userCenter_headportraitR://进行用户头像更改
                //用户头像更改，如果联网则进行同步，反之等备份时同步
                InitDialog();
                Camera_Ablumdialog.show();
                break;
        }
    }

    /**
     * 退出登录
     */
    private void setLogout() {
        //退出登录
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                ContentValues values = new ContentValues();
                values.put("User_Login", 0);
                db.update("userinfo", values, "User_Name=?", new String[]{Username});
                db.close();
                // Log.d("TAG1", "本地更改登录状态");
            }
        }).start();

        new Thread(new Runnable() {//进行同步操作
            @Override
            public void run() {
                Message message = new Message();
                message.what = Logout_Datasync;
                try {
                    db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                    message.obj = Data_ZIP.Data_Sync(db, Username);
                    db.close();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 数据同步
     *
     * @param jsonArray
     */
    private void Data_sync(JSONArray jsonArray) {
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create(jsonArray.toString(), mediaType);
        String address = AppNetConfig.Data_syncCS;

        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.d("TAG1", "退出登录,备份失败,没有网络");
                handler.sendEmptyMessage(Exit_Activity);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                //成功响应，接收数据
                int isRegister = -1;
                String responseText = response.body().string();
                String resultCode = "500";
                //byte[] bytesq = null;
                // Log.d("TAG1", "response     :" + responseText);
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        //Log.d("TAG1", "resultCode=a     " + resultCode);
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        // Log.d("TAG1", "resultCode=b    " + resultCode);
                        if (resultCode.equals("200")) {//退出时备份成功
                            db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                            ContentValues values = new ContentValues();
                            values.put("Time", jsonObject.getLong("Time"));
                            db.update("userinfo", values, "User_Name=?",
                                    new String[]{Username});
                            db.close();
                            Log.d("TAG1", "备份成功");
                        }
                    } catch (JSONException e) {
                        Log.d("TAG1", "出现错误");
                        e.printStackTrace();
                    }
                } else {
                    Log.d("TAG1", "weikong  " + resultCode);
                }
                handler.sendEmptyMessage(Exit_Activity);
            }
        });
    }

    /**
     * 注销账户
     */
    public void setClose_account() {
        LoadingDialog();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("User_Name", Username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MediaType mediaType = MediaType.Companion.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), mediaType);
        String address = AppNetConfig.CloseAccount;
        HttpUtil.sendOkHttpRequest(address, requestBody, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                // Toast.makeText(UserCenter.this, "请连接网络", Toast.LENGTH_SHORT).show();\
                Log.d("TAG", "注销失败，请连接网络");
                NetWork_Code = "500";
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                //成功响应，接收数据
                int isRegister = -1;
                String responseText = response.body().string();
                String resultCode = "500";
                if (!TextUtils.isEmpty(responseText)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseText);
                        resultCode = jsonObject.getString("resultCode");
                        //  Log.d("liangjialing",   resultCode);
                        if (resultCode.equals("200")) {//成功,本地也注销
                            db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                            db.delete("userinfo", "User_Name=?", new String[]{Username});
                            db.close();
                        }
                        NetWork_Code = resultCode;
                    } catch (JSONException e) {
                        Log.d("liangjialing", "解析出错  ");
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    /**
     * 设置头像
     */
    private void InitDialog() {
        View view_dialog = LayoutInflater.from(this).inflate(R.layout.camera_albumdialog, null);
        Camera_Ablumdialog.setContentView(view_dialog);
        Window dialogWindow = Camera_Ablumdialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        Camera_Ablumbutton[0] = (Button) view_dialog.findViewById(R.id.Camera_button);
        Camera_Ablumbutton[1] = (Button) view_dialog.findViewById(R.id.Album_button);
        Camera_Ablumbutton[2] = (Button) view_dialog.findViewById(R.id.Cancel_button);
        DialogClick dc = new DialogClick();
            for (Button button : Camera_Ablumbutton) {
            button.setOnClickListener(dc);
        }
    }

    private class DialogClick implements View.OnClickListener {
        public void onClick(View v) {
           // while(database.isAlive()){}
            switch (v.getId()) {
                case R.id.Camera_button:
                    if (ContextCompat.checkSelfPermission(UserCenter.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(UserCenter.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(UserCenter.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        openSysCamera();
                    }
                    break;
                case R.id.Album_button:
                    if (ContextCompat.checkSelfPermission(UserCenter.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(UserCenter.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                        // Log.d("TAGqqq","未开");
                    } else {
                        openAlbum();
                    }
                    break;
                case R.id.Cancel_button:
                    Camera_Ablumdialog.cancel();
                    break;
                default:
                    break;
            }
        }
    }
/**
 * 返回拍照或相册选择的图片
 */
 private void setHead_Portrait(Bitmap bitmap){
     userCenter_headportrait.setImageBitmap(bitmap);
     final Bitmap bitmap1=bitmap;
     database=new Thread(new Runnable() {
         @Override
         public void run() {
             db = SQLiteDatabase.openDatabase(DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
             byte[] bytes=PictureFormatUtil.Bitmap2Bytes(bitmap1);
             ContentValues values=new ContentValues();
             values.put("Head_Portrait",bytes);
             db.update("userinfo",values,"User_Name=?",new String[]{Username});
             db.close();
         }
     });
     database.start();
 }
    /**
     * 权限回调
     * @param requestCode
     * @param permission
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission,
                                           int[] grantResults) {
        //请求权限时返回请求码的处理
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSysCamera();
                } else {
                    Toast.makeText(this, "未开通权限", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                //权限结果
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "未开通权限", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO://处理拍照后的图片
                //拍照后回调的位置，直接进行裁剪跳转
                if (resultCode == RESULT_OK) {
                    Log.d("TAGqq", "拍照跳转到裁剪");
                    startCropImage(imageUri);
                }
                else{
                    Log.d("TAGqq","result_cancel");
                }
                break;
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    imageUri = data.getData();//得到选择照片的Uri
                    Log.d("TAGqq", "CHOOSE_PHOTO: " + imageUri);
                    startCropImage(imageUri);
                }
                else{
                    Log.d("TAGqq","result_cancel");
                }
                break;
            case CROP_RESULT_CODE:

                if (resultCode == RESULT_OK) {
                    try {
                        Log.d("TAGqq", "裁剪完成，显示图片");
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver()
                                        .openInputStream(cropImageUri));
                        //picture.setImageBitmap(bitmap); // 将裁剪后的照片显示出来
                        setHead_Portrait(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.d("TAGqq","result_cancel");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 打开系统相机
     */
    private void openSysCamera() {
        File outputImage = new File(getExternalCacheDir(), createFileName());
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {//封装过的URI对象,不是真实的
            //内容提供器需要注册
            imageUri = FileProvider.getUriForFile(UserCenter.this,
                    getPackageName() + ".fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//启用拍照功能，拍照成功保存到你建立的文件中。
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    /**
     * 打开相册，选择照片
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    /**
     * 裁减图片操作
     *
     * @param
     */
    private void startCropImage(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//开通读写Uri的权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop", "true");// 使图片处于可裁剪状态
        // 裁剪框的比例（根据需要显示的图片比例进行设置）
       /* if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {}*/
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 320);        //输出图片大小
        intent.putExtra("outputY", 320);
        intent.putExtra("scale", true);//是否保留比例
        intent.putExtra("noFaceDetection", true);
        // 传递原图路径
        File cropFile = new File(getExternalCacheDir() + "/head_protrait.jpg");
        try {
            if (cropFile.exists()) {
                cropFile.delete();
            }
            cropFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cropImageUri = Uri.fromFile(cropFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 设置图片的输出格式
        intent.putExtra("return-data", false);
        startActivityForResult(intent, CROP_RESULT_CODE);
    }

    /**
     * 为图片创建名称
     */
    private static String createFileName() {
        String fileName = "";
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        fileName = dateFormat.format(date) + ".jpg";
        return fileName;
    }

    /**
     * 加载界面
     */
    public void LoadingDialog() {
        LoadingDialog = loadDialogUtils.createLoadingDialog(UserCenter.this, "注销中...");
        //开启一个线程进行等待
        new Thread(new Runnable() {
            @Override
            public void run() {
                //开启等待界面
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (NetWork_Code.equals("200")) {
                    handler.sendEmptyMessage(Close_success);
                } else if (NetWork_Code.equals("201")) {
                    handler.sendEmptyMessage(Close_havent);
                } else if (NetWork_Code.equals("500")) {
                    handler.sendEmptyMessage(Close_fail);
                }
            }
        }).start();
    }
}

