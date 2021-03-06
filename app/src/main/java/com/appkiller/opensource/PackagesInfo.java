package com.appkiller.opensource;

import java.util.List;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class PackagesInfo {
    private List<ApplicationInfo> appList;

    public PackagesInfo(Context ctx) {
        PackageManager pm = ctx.getApplicationContext().getPackageManager();
        appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    public ApplicationInfo getInfo(String name) {
        if (name == null) {
            return null;
        }
        for (ApplicationInfo appinfo : appList) {
            if (name.equals(appinfo.processName)) {
                // System.out.println(name + "===" + appinfo.className);
                return appinfo;
            }
        }
        return null;
    }

}
