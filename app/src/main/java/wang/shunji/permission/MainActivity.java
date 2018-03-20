package wang.shunji.permission;

import android.Manifest;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.xiweinet.permissionchecker.Permission.PermissionCallback;
import com.xiweinet.permissionchecker.Permission.PermissionChecker;
import com.xiweinet.permissionchecker.Permission.PermissionItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        f(R.id.tv_permission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<PermissionItem> mPermissionItems = new ArrayList<PermissionItem>();
                mPermissionItems.add(new PermissionItem(Manifest.permission.RECORD_AUDIO, "麦克风", R.drawable.permission_ic_micro_phone,"语音聊天和视频会议需要此权限"));
                mPermissionItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, "存储", R.drawable.permission_ic_storage,"文档编辑和保存临时数据需要此权限"));
                mPermissionItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, "电话", R.drawable.permission_ic_phone,"分享功能和视频会议需要此权限"));
                mPermissionItems.add(new PermissionItem(Manifest.permission.CAMERA, "相机", R.drawable.permission_ic_camera,"视频会议和我的信息需要此权限"));
                mPermissionItems.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, "位置", R.drawable.permission_ic_location,"视频会议需要此权限"));
                PermissionChecker.create(MainActivity.this)
                        .permissions(mPermissionItems)
                        .requestCount(1)//1-3
                        .title(getResources().getString(R.string.app_name)+"需要以下权限")
//                        .msg("必要的权限，不开启将无法正常工作")
                        .animStyle(R.style.PermissionAnimFade)
                        .filterColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                        .checkMutiPermission(new PermissionCallback() {
                            @Override
                            public void onClose() {
//                                Toast.makeText(getActivity(), "onClose", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFinish() {
//                                Toast.makeText(getActivity(), "您可以到设置->应用管理->集商通->权限，重新授权", Toast.LENGTH_SHORT).show();

                            }

                            //拒绝权限
                            @Override
                            public void onDeny(String permission, int position) {
//                                Toast.makeText(getActivity(), "onDeny", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onGuarantee(String permission, int position) {
//                                Toast.makeText(getActivity(), "onGuarantee", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        });
    }

    private <T extends View>T f(int viewId){
        return findViewById(viewId);
    }
}
