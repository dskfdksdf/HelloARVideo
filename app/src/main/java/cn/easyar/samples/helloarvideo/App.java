package cn.easyar.samples.helloarvideo;

import android.app.Application;
import com.amap.api.services.help.Tip;
/**
 * Created by 毛奇志 on 2018/3/17.
 */

public class App extends Application{
    public String city = "中国";
    private Tip tip;//App继承于Application,Tip是App的一个属性，所以App才是Bean   app.getTip()

    public Tip getTip() {
        return tip;
    }

    public void setTip(Tip tip) {
        this.tip = tip;
    }
}
