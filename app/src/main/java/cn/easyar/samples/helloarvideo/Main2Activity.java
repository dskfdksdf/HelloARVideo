package cn.easyar.samples.helloarvideo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

public class Main2Activity extends Activity implements GeocodeSearch.OnGeocodeSearchListener {
    private MapView mMapView = null;
    private MyLocationStyle myLocationStyle;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    GeocodeSearch geocoderSearch;
    LatLng latLngx;
    App app;
    AMap aMap = null;
    Marker marker;
    TextView textar;
    BitmapDescriptor bitmapDescriptor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main2);
        app = (App) getApplication();
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();//这里还是为空
           // Log.e("xxx","aMap "+aMap);
        }
        textar=(TextView)findViewById(R.id.textar);
        textar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Main2Activity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.ease_icon_marka);
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(b);
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
        myLocationStyle.showMyLocation(true);
//        aMap.setMyLocationStyle(myLocationStyle.myLocationIcon(bitmap));//设置定位蓝点的Style
//        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        //设置希望展示的地图缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
//        aMap.setLocationSource(this);//通过aMap对象设置定位数据源的监听
        mUiSettings.setZoomPosition(1);
        mUiSettings.setMyLocationButtonEnabled(false); //显示默认的定位按钮
        mUiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setOnMyLocationChangeListener(onmy);//用户位置改变
//地图点击事件
        aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                latLngx = latLng;
//                Toast.makeText(MainActivity.this,latLng.toString(),Toast.LENGTH_SHORT).show();
                geocoderSearch = new GeocodeSearch(Main2Activity.this);
                geocoderSearch.setOnGeocodeSearchListener(Main2Activity.this);
                // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                LatLonPoint a = new LatLonPoint(latLng.latitude,latLng.longitude);
                RegeocodeQuery query = new RegeocodeQuery(a, 200,GeocodeSearch.AMAP);
                geocoderSearch.getFromLocationAsyn(query);
            }
        });

    }

    AMap.OnMyLocationChangeListener onmy = new AMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {

//            Toast.makeText(MainActivity.this,location.toString(),Toast.LENGTH_SHORT).show();
        }
    };
    //跳转
    public void seek(View view){
        Intent it = new Intent(this,SeekActivity.class);

        startActivity(it);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        CameraUpdate mCameraUpdate = null;
        if(app.getTip()!=null){
            if (app.getTip().getPoint()!=null){//经纬度为空 解决了
                mCameraUpdate  = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(app.getTip().getPoint().getLatitude(),app.getTip().getPoint().getLongitude()),16,30,0));
                aMap.moveCamera(mCameraUpdate);
                maeKer(app.getTip().getPoint().getLatitude(),app.getTip().getPoint().getLongitude(),app.getTip().getName(),app.getTip().getDistrict());
            }
        }
        mMapView.onResume();
        app.setTip(null);
    }
    MarkerOptions markerO = null;
    //绘制点标记
    public void maeKer(double a,double b,String name,String str){
        LatLng latLng = new LatLng(a,b);
        markerO =  new MarkerOptions().position(latLng).title(name).snippet(str).icon(bitmapDescriptor);
        marker = aMap.addMarker(markerO);
        Animation animation = new RotateAnimation(marker.getRotateAngle(),marker.getRotateAngle()+360,0,0,0);
        long duration = 1000L;//默认为int
        animation.setDuration(duration);
        animation.setInterpolator(new LinearInterpolator());

        marker.setAnimation(animation);
        marker.startAnimation();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }
    //逆地理编码  经纬度转化为地址
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        maeKer(latLngx.latitude,latLngx.longitude,regeocodeResult.getRegeocodeAddress().getFormatAddress(),"附近");
    }
    //地理彪马  地址转换为经纬度
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }
}
