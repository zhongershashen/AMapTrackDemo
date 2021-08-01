package com.amap.trackdemo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.track.AMapTrackClient;
import com.amap.trackdemo.R;
import com.amap.trackdemo.util.Constants;
import com.amap.trackdemo.view.FeatureView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private  EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        requestIgnoreBatteryOptimizations();
        requestPermissionsIfAboveM();

        setTitle("猎鹰sdk demo " + AMapTrackClient.getVersion());
        editText = findViewById(R.id.TerminalName);
        Button button = findViewById(R.id.TerminalButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constants.TERMINAL_NAME = editText.getText().toString();
                editText.clearFocus();
            }
        });
        ListView listView = findViewById(R.id.activity_main_list);
        ListAdapter adapter = new CustomArrayAdapter(
                this.getApplicationContext(), demos);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DemoDetails demo = (DemoDetails) parent.getAdapter().getItem(position);
                if (Constants.TERMINAL_NAME.equals("")) {
                    Toast.makeText(MainActivity.this,
                            "请输入工地名称", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(MainActivity.this, demo.activityClass));
                }
            }
        });
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestIgnoreBatteryOptimizations() {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Map<String, String> permissionHintMap = new HashMap<>();
    private void requestPermissionsIfAboveM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Map<String, String> requiredPermissions = new HashMap<>();
//            requiredPermissions.put(Manifest.permission.ACCESS_BACKGROUND_LOCATION,"后台持续定位");
            requiredPermissions.put(Manifest.permission.ACCESS_FINE_LOCATION, "定位");
            requiredPermissions.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储");
            requiredPermissions.put(Manifest.permission.READ_PHONE_STATE, "读取设备信息");
            for (String permission : requiredPermissions.keySet()) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionHintMap.put(permission, requiredPermissions.get(permission));
                }
            }
            if (!permissionHintMap.isEmpty()) {
                requestPermissions(permissionHintMap.keySet().toArray(new String[0]), REQUEST_CODE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> failPermissions = new LinkedList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                failPermissions.add(permissions[i]);
            }
        }
        if (!failPermissions.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String permission : failPermissions) {
                sb.append(permissionHintMap.get(permission)).append("、");
            }
            sb.deleteCharAt(sb.length() - 1);
            String hint = "未授予必要权限: " +
                    sb.toString() +
                    "，请前往设置页面开启权限";
            new AlertDialog.Builder(this)
                    .setMessage(hint)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            System.exit(0);
                        }
                    }).show();
        }
    }

    private static class DemoDetails {
        private final String title;
        private final String description;
        private final Class<? extends android.app.Activity> activityClass;

        public DemoDetails(String title, String description,
                           Class<? extends android.app.Activity> activityClass) {
            super();
            this.title = title;
            this.description = description;
            this.activityClass = activityClass;
        }
    }

    private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {
        public CustomArrayAdapter(Context context, DemoDetails[] demos) {
            super(context, R.layout.feature, R.id.title, demos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FeatureView featureView;
            if (convertView instanceof FeatureView) {
                featureView = (FeatureView) convertView;
            } else {
                featureView = new FeatureView(getContext());
            }
            DemoDetails demo = getItem(position);
            featureView.setTitle(demo.title);
            featureView.setDescription(demo.description);
            return featureView;
        }
    }

    private static final DemoDetails[] demos = {
            new DemoDetails("轨迹上报服务", "上传轨迹", TrackServiceActivity.class),
//            new DemoDetails("轨迹查询", "查询最近的轨迹", TrackSearchActivity.class),
            new DemoDetails("实时位置及里程", "查询实时位置和行驶里程", OtherSearchActivity.class)
    };



}
