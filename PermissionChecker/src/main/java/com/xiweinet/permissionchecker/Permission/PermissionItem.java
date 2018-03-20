package com.xiweinet.permissionchecker.Permission;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/10 0010.
 */

public class PermissionItem implements Serializable {
    public String PermissionName;//权限名称
    public String Permission;//权限
    public int PermissionIconRes;//权限icon
    public String PermissionExplain;//权限解释

    public PermissionItem(String permission, String permissionName, int permissionIconRes,String permissionExplain) {
        Permission = permission;
        PermissionName = permissionName;
        PermissionIconRes = permissionIconRes;
        PermissionExplain=permissionExplain;
    }

    public PermissionItem(String permission) {
        Permission = permission;
    }
}
