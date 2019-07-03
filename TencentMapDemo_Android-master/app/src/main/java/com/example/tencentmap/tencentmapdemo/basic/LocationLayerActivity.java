package com.example.tencentmap.tencentmapdemo.basic;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.tencentmap.tencentmapdemo.R;
import com.example.tencentmap.tencentmapdemo.basic.SupportMapFragmentActivity;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptor;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class LocationLayerActivity extends SupportMapFragmentActivity implements EasyPermissions.PermissionCallbacks,LocationSource,TencentLocationListener, TencentMap.OnMapLongClickListener {

    private OnLocationChangedListener locationChangedListener;

    private TencentLocationManager locationManager;
    private TencentLocationRequest locationRequest;
    private MyLocationStyle locationStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //定位需要申请的权限
        String[] perms = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        };

        if (EasyPermissions.hasPermissions(this, perms)) {//检查是否获取该权限
            Log.i("location", "已获取权限");
        } else {
            EasyPermissions.requestPermissions(this, "必要的权限", 0, perms);
        }
        //设置显示定位的图标
        mapUiSettings.setMyLocationButtonEnabled(true);
        //建立定位
        initLocation();
    }

    /**
     * 定位的一些初始化设置
     */
    private void initLocation(){
        //用于访问腾讯定位服务的类, 周期性向客户端提供位置更新
        locationManager = TencentLocationManager.getInstance(this);
        //设置坐标系
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_WGS84);
        //创建定位请求
        locationRequest = TencentLocationRequest.create();
        //设置定位周期（位置监听器回调周期）为10s
        locationRequest.setInterval(10000);
        Log.v("location period", "100000");
        //地图上设置定位数据源
        tencentMap.setLocationSource(this);
        //设置当前位置可见
        tencentMap.setMyLocationEnabled(true);
        //设置定位图标样式
        setLocMarkerStyle();
    }

    /**
     * 设置定位图标样式
     */
    private void setLocMarkerStyle(){
        locationStyle = new MyLocationStyle();
        //创建图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getBitMap(R.drawable.navi_marker_location));
        locationStyle.icon(bitmapDescriptor);
        //设置定位圆形区域的边框宽度
        locationStyle.strokeWidth(3);
        //设置圆区域的颜色
        locationStyle.fillColor(R.color.style);
        tencentMap.setMyLocationStyle(locationStyle);
    }

    private Bitmap getBitMap(int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = 55;
        int newHeight = 55;
        float widthScale = ((float)newWidth)/width;
        float heightScale = ((float)newHeight)/height;
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, heightScale);
        bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
        return bitmap;
    }

    /**
     * 实现位置监听
     * @param tencentLocation
     * @param i
     * @param s
     */
    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {

        if(i == TencentLocation.ERROR_OK && locationChangedListener != null){
            Location location = new Location(tencentLocation.getProvider());
            //设置经纬度以及精度
            location.setLatitude(tencentLocation.getLatitude());
            location.setLongitude(tencentLocation.getLongitude());
            location.setAccuracy(tencentLocation.getAccuracy());
            locationChangedListener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //GPS, WiFi, Radio 等状态发生变化
        Log.v("State changed", s +"===" +  s1);
    }


    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;

        int err = locationManager.requestLocationUpdates(locationRequest, this, Looper.myLooper());
        switch (err) {
            case 1:
                Toast.makeText(this,"设备缺少使用腾讯定位服务需要的基本条件",Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this,"manifest 中配置的 key 不正确",Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(this,"自动加载libtencentloc.so失败",Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }

    @Override
    public void deactivate() {
        locationManager.removeUpdates(this);
        locationManager = null;
        locationRequest = null;
        locationChangedListener=null;
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.e("location quest: ","success");
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.e("location quest: ","failed");
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (locationChangedListener == null)
            return;
        Location location = new Location("LongPressLocationProvider");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        location.setAccuracy(20);
        locationChangedListener.onLocationChanged(location);
    }
}
