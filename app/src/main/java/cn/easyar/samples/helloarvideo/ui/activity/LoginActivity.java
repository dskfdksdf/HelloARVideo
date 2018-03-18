package cn.easyar.samples.helloarvideo.ui.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.easyar.samples.helloarvideo.R;
import cn.easyar.samples.helloarvideo.ui.fragment.Login;
import cn.easyar.samples.helloarvideo.ui.fragment.Regist;

public class LoginActivity extends BaseActivity implements Login.OnDataTransmissionListener {
    private FragmentManager fm;private Login login;
    private Regist regist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fm = getSupportFragmentManager();
        setDefaultFragment();
    }
    private void setDefaultFragment() {
        FragmentTransaction transaction = fm.beginTransaction();
     /*   hideFragments(transaction);//隐藏登陆或注册碎片
        if (login == null) {
            login = new Login();
            login.setmllistener(this);//将LoginActivity设置为OnDataTransmissionListener  Login碎片中
            transaction.add(R.id.logincontent, login).addToBackStack(null);//加入返回栈
        } else {
            transaction.show(login);//login为空,新建并add  login不为空，直接show(）出来
        }*/
        if(login==null)
            login=new Login();
        login.setmllistener(this);
        transaction.replace(R.id.logincontent,login);//初始化时碎片替换   这里取消返回栈  否则出现白板  good
        //第一次碎片替换，不要有返回栈，会看到白板 对于碎片，我只用replace(),第一次也不用add,也用replace()
        transaction.commit();

    }
    @Override
    public void dataTransmission(int data) {
        FragmentTransaction transaction = fm.beginTransaction();
        switch (data) {
            case 2:
                if (regist == null)
                    regist = new Regist();
                transaction.replace(R.id.logincontent, regist).addToBackStack(null);//2到注册碎片 这里要返回栈
                //直接从注册碎片中退出程序 不好的用户体验 所以要返回栈  从用户心理来讲，一定是从登陆页面退出程序的，就像是从主页面退出到登陆页面一样
                break;
            default:
                break;
        }
        transaction.commit();
    }
}
