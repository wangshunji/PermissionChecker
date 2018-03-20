package com.xiweinet.permissionchecker.Permission;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiweinet.myapplication.R;

import java.util.List;
import java.util.ListIterator;


/**
 * Created by Administrator on 2017/5/10 0010.
 */

public class PermissionActivity extends AppCompatActivity {

    public static int PERMISSION_TYPE_SINGLE = 1;
    public static int PERMISSION_TYPE_MUTI = 2;
    private int mPermissionType;
    private String mTitle;
    private String mMsg;
    private int mRequestCount;
    private static PermissionCallback mCallback;
    private List<PermissionItem> mCheckPermissions;
    private Dialog mDialog;//第一次权限申请的dialog

    private static final int REQUEST_CODE_SINGLE = 1;
    private static final int REQUEST_CODE_MUTI = 2;
    public static final int REQUEST_CODE_MUTI_SINGLE = 3;
    private static final int REQUEST_SETTING = 110;
    private boolean IsJump = false;//是否跳过

    private static final String TAG = "PermissionActivity";
    private CharSequence mAppName;
    private int mStyleId;
    private int mFilterColor;
    private int mAnimStyleId;
    private Dialog mDialog1;//第二次权限申请的dialog
    private int mSize;

    public static void setCallBack(PermissionCallback callBack) {
        PermissionActivity.mCallback = callBack;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallback = null;
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDatas();
        if (mPermissionType == PERMISSION_TYPE_SINGLE) {
            //单个权限申请
            if (mCheckPermissions == null || mCheckPermissions.size() == 0)
                return;

            requestPermission(new String[]{mCheckPermissions.get(0).Permission}, REQUEST_CODE_SINGLE);
        } else {
            mAppName = getApplicationInfo().loadLabel(getPackageManager());
            //多个权限
            showPermissionDialog();
        }

    }


    private String getPermissionTitle() {
        return TextUtils.isEmpty(mTitle) ? String.format(getString(R.string.permission_dialog_title), mAppName) : mTitle;
    }

    private void showPermissionDialog() {

        String title = getPermissionTitle();
        String msg = TextUtils.isEmpty(mMsg) ? String.format(getString(R.string.permission_dialog_msg), mAppName) : mMsg;

        PermissionView contentView = new PermissionView(this);
//        contentView.setGridViewColum(mCheckPermissions.size() < 3 ? mCheckPermissions.size() : 3);
        contentView.setTitle(title);
        contentView.setMsg(msg);
        //这里没有使用RecyclerView，可以少引入一个库
        contentView.setListViewAdapter(new PermissionAdapter(mCheckPermissions));
        if (mStyleId == -1) {
            //用户没有设置，使用默认app主题
            mStyleId = R.style.PermissionDefaultNormalStyle;
            mFilterColor = getResources().getColor(R.color.permissionColorBule
            );
        }
        contentView.setStyleId(mStyleId);
        contentView.setFilterColor(mFilterColor);
        contentView.setBtnNextOnClickListener(new View.OnClickListener() {//下一步
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing())
                    mDialog.dismiss();
                String[] strs = getPermissionStrArray();
                ActivityCompat.requestPermissions(PermissionActivity.this, strs, REQUEST_CODE_MUTI);
            }
        });
        contentView.setBtnJumpOnClickListener(new View.OnClickListener() {//跳过
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                IsJump = true;
                SharePreUtil.putBoolean("IsJump", PermissionActivity.this, "IsJump", IsJump);
                onClose();

            }
        });
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(contentView);
        if (mAnimStyleId != -1)
            mDialog.getWindow().setWindowAnimations(mAnimStyleId);

        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (mCallback != null)
                    mCallback.onClose();
                finish();
            }
        });
        mDialog.show();
    }

    /**
     * 重新请求
     *
     * @param permissionItems
     */
    private void reRequestPermission(final List<PermissionItem> permissionItems) {
        int index = 0;
        if (permissionItems.size() >= 1) {
            final String permission = permissionItems.get(index).Permission;
            String permissionName = permissionItems.get(index).PermissionName;
            String explainMsg = permissionItems.get(index).PermissionExplain;
            int iconRes = permissionItems.get(index).PermissionIconRes;

            String alertTitle = String.format(getString(R.string.permission_title), permissionName);
            showAlertDialog(alertTitle, explainMsg, iconRes, permissionName, new View.OnClickListener() {
                @Override
                public void onClick(View v) {//取消
                    PermissionItem item = getPermissionItem(permission);
                    mCheckPermissions.remove(item);
                    mDialog1.dismiss();
                    if (mCallback != null)
                        mCallback.onClose();
                    //继续请求下一个拒绝的权限
                    reRequestPermission(permissionItems);
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View v) {//确定
                    mDialog1.dismiss();

                    requestPermission(new String[]{permission}, REQUEST_CODE_MUTI_SINGLE);
                }

            });
        } else {
            IsJump = true;
            SharePreUtil.putBoolean("IsJump", PermissionActivity.this, "IsJump", IsJump);
            onFinish();
        }
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(PermissionActivity.this, permissions, requestCode);
    }

    /**
     * 被拒后调用此方法，一个一个的解释
     */
    private void showAlertDialog(String title, String explainMsg, int iconRes, String permissName, View.OnClickListener cancelClicklistener, final View.OnClickListener submitClickListener) {
        int blue = Color.blue(mFilterColor);
        int green = Color.green(mFilterColor);
        int red = Color.red(mFilterColor);
        float[] cm = new float[]{
                1, 0, 0, 0, red,// 红色值
                0, 1, 0, 0, green,// 绿色值
                0, 0, 1, 0, blue,// 蓝色值
                0, 0, 0, 1, 1 // 透明度
        };
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);

        View dialogView = View.inflate(PermissionActivity.this, R.layout.dialog_request_permission2, null);
        mDialog1 = new Dialog(PermissionActivity.this);
        mDialog1.setContentView(dialogView);
        mDialog1.setCanceledOnTouchOutside(true);
        //标题
        ((TextView) dialogView.findViewById(R.id.tvTitle)).setText(title);
        //权限解释
        ((TextView) dialogView.findViewById(R.id.tvDesc)).setText(explainMsg);
        //权限icon
        ImageView icon = ((ImageView) dialogView.findViewById(R.id.iv_permission_icon));
        icon.setImageResource(iconRes);
        icon.setColorFilter(filter);
        //权限名
        ((TextView) dialogView.findViewById(R.id.tv_permission_name)).setText(permissName);
        //取消
        dialogView.findViewById(R.id.cancel).setOnClickListener(cancelClicklistener);
        //确定
        dialogView.findViewById(R.id.submit).setOnClickListener(submitClickListener);
        mDialog1.show();
//        typedArray.recycle();

    }

    private String[] getPermissionStrArray() {
        String[] str = new String[mCheckPermissions.size()];
        for (int i = 0; i < mCheckPermissions.size(); i++) {
            str[i] = mCheckPermissions.get(i).Permission;
        }
        return str;
    }


    private void getDatas() {
        Intent intent = getIntent();
        mPermissionType = intent.getIntExtra(ConstantValue.DATA_PERMISSION_TYPE, PERMISSION_TYPE_SINGLE);
        mTitle = intent.getStringExtra(ConstantValue.DATA_TITLE);
        mMsg = intent.getStringExtra(ConstantValue.DATA_MSG);
        mFilterColor = intent.getIntExtra(ConstantValue.DATA_FILTER_COLOR, 0);
        mStyleId = intent.getIntExtra(ConstantValue.DATA_STYLE_ID, -1);
        mAnimStyleId = intent.getIntExtra(ConstantValue.DATA_ANIM_STYLE, -1);
        mRequestCount = intent.getIntExtra(ConstantValue.DATA_COUNT, -1);
        mCheckPermissions = (List<PermissionItem>) intent.getSerializableExtra(ConstantValue.DATA_PERMISSIONS);
    }

    //设置权限后系统回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_SINGLE:
                String permission = getPermissionItem(permissions[0]).Permission;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onGuarantee(permission, 0);
                } else {
                    onDeny(permission, 0);
                }
                finish();
                break;
            case REQUEST_CODE_MUTI:
                for (int i = 0; i < grantResults.length; i++) {
                    //权限允许后，删除需要检查的权限
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        PermissionItem item = getPermissionItem(permissions[i]);
                        mCheckPermissions.remove(item);
                        onGuarantee(permissions[i], i);

                    } else if (!ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, permissions[i])) {
                        //返回 false 就表示勾选了不再询问
                        PermissionItem item = getPermissionItem(permissions[i]);
                        mCheckPermissions.remove(item);
                        onDeny(permissions[i], i);
                    } else {
                        //权限拒绝
                        onDeny(permissions[i], i);
                    }
                }
                 //检查可以询问的次数
                if (mRequestCount > 1 && mCheckPermissions.size() > 0) {
                    boolean dialogIsShow = true;
                    if (mDialog1 != null) {
                        dialogIsShow = (!mDialog1.isShowing());//用户从设置返回时会出现两个dialog
                    }
                    //用户拒绝了某个或多个权限，重新申请
                    if (dialogIsShow)
                        reRequestPermission(mCheckPermissions);
                } else {
                    onFinish();
                }
                break;
            case REQUEST_CODE_MUTI_SINGLE:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    try {
                        //重新申请后再次拒绝 ,先判断是不是勾选了不再询问
                        if (mRequestCount == 3 && ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, permissions[0])) {
                            //permissions可能返回空数组，所以try-catch
                            String name = getPermissionItem(permissions[0]).PermissionName;
                            String title = String.format(getString(R.string.permission_title), name);
                            String msg = String.format(getString(R.string.permission_denied_with_naac), mAppName, name, mAppName);
                            // TODO: 2018/3/10  这里换成commDialog
                            //最后一次弹框，引导用户去设置界面
                            showAlertDialog(title, msg, 0, "", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {//取消
                                    mDialog1.dismiss();
                                    if (mCallback != null)
                                        mCallback.onClose();
                                    //继续申请下一个被拒绝的权限
                                    RequestNextPermission(permissions[0]);
                                }
                            }, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {//确定
                                    try {
                                        Uri packageURI = Uri.parse("package:" + getPackageName());
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                                        startActivityForResult(intent, REQUEST_SETTING);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        onClose();
                                    }
                                }
                            });
                            onDeny(permissions[0], 0);
                        } else {
                            //把用户勾选不再询问的移除
                            //继续申请下一个被拒绝的权限
                            RequestNextPermission(permissions[0]);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        onClose();
                    }
                } else {
                    try {
                        onGuarantee(permissions[0], 0);
                        //继续申请下一个被拒绝的权限
                        RequestNextPermission(permissions[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        onClose();
                    }

                }
                break;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * 继续下一个权限申请
     */
    private void RequestNextPermission(String permissions) {
        PermissionItem item = getPermissionItem(permissions);
        mCheckPermissions.remove(item);
        //继续申请下一个被拒绝的权限
        if (mCheckPermissions.size() >= 1) {
            reRequestPermission(mCheckPermissions);
        } else {
            //全部允许了或全部拒绝了
            IsJump = true;
            SharePreUtil.putBoolean("IsJump", PermissionActivity.this, "IsJump", IsJump);
            onFinish();
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //用户从权限设置返回
        if (requestCode == REQUEST_SETTING) {
            if (mDialog1 != null && mDialog1.isShowing())
                mDialog1.dismiss();
            if (mDialog != null && mDialog.isShowing())
                mDialog1.dismiss();
            checkPermission();
            PermissionItem item = getPermissionItem(mCheckPermissions.get(0).Permission);
            mCheckPermissions.remove(item);
            if (mCheckPermissions.size() > 0) {
                //继续请求下一个拒绝的
                reRequestPermission(mCheckPermissions);
            } else {
                onFinish();
            }
        }

    }

    private void checkPermission() {

        ListIterator<PermissionItem> iterator = mCheckPermissions.listIterator();
        while (iterator.hasNext()) {
            int checkPermission = ContextCompat.checkSelfPermission(getApplicationContext(), iterator.next().Permission);
            if (checkPermission == PackageManager.PERMISSION_GRANTED) {
                iterator.remove();
            }
        }
    }

    //完成
    private void onFinish() {
        if (mCallback != null)
            mCallback.onFinish();
        finish();
    }

    //关闭
    private void onClose() {
        if (mCallback != null)
            mCallback.onClose();
        finish();
    }

    //拒绝
    private void onDeny(String permission, int position) {
        if (mCallback != null)
            mCallback.onDeny(permission, position);
    }

    //授权
    private void onGuarantee(String permission, int position) {
        if (mCallback != null)
            mCallback.onGuarantee(permission, position);
    }

    private PermissionItem getPermissionItem(String permission) {
        for (PermissionItem permissionItem : mCheckPermissions) {
            if (permissionItem.Permission.equals(permission))
                return permissionItem;
        }
        return null;
    }
}
