package cn.easyar.samples.helloarvideo.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import cn.easyar.samples.helloarvideo.R;

public class BaseActivity extends AppCompatActivity {
    public static final String originUrl="http://192.168.1.103:8080/Version5/";
    public static final String ERROR="请求失败，请检查网络设置或将手机移至网络良好地段";
    public static  Integer USER_ID=1;
    public static String USER_NAME="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Log.e("BaseActivity", getClass().getSimpleName());
    }
}
