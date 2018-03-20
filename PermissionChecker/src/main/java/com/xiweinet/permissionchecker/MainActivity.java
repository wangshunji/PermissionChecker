package com.xiweinet.permissionchecker;

import android.Manifest;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.xiweinet.permissionchecker.Permission.PermissionChecker;
import com.xiweinet.permissionchecker.Permission.PermissionCallback;
import com.xiweinet.permissionchecker.Permission.PermissionItem;
import com.xiweinet.myapplication.R;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private List<PermissionItem> mPermissionItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPermissionItems = new ArrayList<PermissionItem>();
//        mPermissionItems.add(new PermissionItem(Manifest.permission.RECORD_AUDIO, "录音", R.drawable.permission_ic_micro_phone,"我们要录音我们要录音我们要录音我们要录音我们要录音我们要录音"));
        mPermissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", R.drawable.permission_ic_storage,"我们要存储我们要存储我们要存储我们要存储我们要存储我们要存储"));
//        mPermissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "电话", R.drawable.permission_ic_phone,"我们要电话我们要电话我们要电话我们要电话我们要电话我们要电话我们要电话"));
        mPermissionItems.add(new PermissionItem(Manifest.permission.CAMERA, "相机", R.drawable.permission_ic_camera,"我们要相机我们要相机我们要相机我们要相机我们要相机我们要相机我们要相机我们要相机"));
        mPermissionItems.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "位置", R.drawable.permission_ic_location,"我们要位置我们要位置我们要位置我们要位置我们要位置我们要位置"));
        PermissionChecker.create(MainActivity.this)
                .permissions(mPermissionItems)
                .requestCount(3)//1-3
                .title("集商通需要以下权限")
                .msg("为了保护世界的和平，开启这些权限吧！\n你我一起拯救世界")
                .animStyle(R.style.PermissionAnimFade)
                .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Toast.makeText(MainActivity.this, "onClose", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish() {
                        Toast.makeText(MainActivity.this, "onFinish", Toast.LENGTH_SHORT).show();

                    }

                    //拒绝权限
                    @Override
                    public void onDeny(String permission, int position) {
                        Toast.makeText(MainActivity.this, "onDeny", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onGuarantee(String permission, int position) {
                        Toast.makeText(MainActivity.this, "onGuarantee", Toast.LENGTH_SHORT).show();

                    }
                });


    }
}
