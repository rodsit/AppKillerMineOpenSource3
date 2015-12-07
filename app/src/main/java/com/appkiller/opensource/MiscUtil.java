package com.appkiller.opensource;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

public class MiscUtil {
	public static final int MENU_UNINSTALL = 0;
	public static final int QUICK_KILL = 1;
    public static final int MENU_SWITCH = 2;
   

    public static PackageInfo getPackageInfo(PackageManager pm, String name) {
        PackageInfo ret = null;
        try {
            ret = pm.getPackageInfo(name, PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e) {
            // e.printStackTrace();
        }
        return ret;
    }

    public static Dialog getTaskMenuDialog(final TaskManager ctx, final DetailProcess dp) {

        return new AlertDialog.Builder(ctx).setTitle(dp.getTitle()).setItems(
                R.array.menu_task_operation, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case MENU_SWITCH: {
                                if (dp.getPackageName().equals(ctx.getPackageName())) return;
                                Intent i = dp.getIntent();
                                if (i == null) {
                                    Toast.makeText(ctx, R.string.message_switch_fail, Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                try {
                                    ctx.startActivity(i);
                                } catch (Exception ee) {
                                    Toast.makeText(ctx, ee.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                return;
                            }
                            case MENU_UNINSTALL: {
                                Intent intent = new Intent();
                                final int apiLevel = Build.VERSION.SDK_INT;
                                if (apiLevel >= 9) { // above 2.3
                                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                    Uri uri = Uri.fromParts("package", dp.getPackageName(), null);
                                    intent.setData(uri);
                                } else { // below 2.3
                                    final String appPkgName = (apiLevel == 8 ? "pkg"
                                            : "com.android.settings.ApplicationPkgName");
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                                    intent.putExtra(appPkgName, dp.getPackageName());
                                }
                                try {
                                    ctx.startActivity(intent);
                                } catch (Exception e) {
                                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                return;
                            	
  
                            }
                            case QUICK_KILL:{
                                ctx.am.restartPackage(dp.getPackageName());
                                if (dp.getPackageName().equals(ctx.getPackageName())) return;
                                ctx.refresh();
                                return;
                            }
  /*                          case MENU_DETAIL: {
                                Uri uri = Uri.fromParts("package", dp.getPackageName(), null);
                                Intent it = new Intent("android.settings.", uri);
                                try {
                                    ctx.startActivity(it);
                                } catch (Exception e) {
                                    Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                return;
                            }  */
                        }

                        /* User clicked so do some stuff */
                         String[] items =
                         ctx.getResources().getStringArray(R.array.menu_task_operation);
                         Toast.makeText(ctx, "You selected: " + which + " , " + items[which],
                         Toast.LENGTH_SHORT).show();
                    }
                }).create();
    }
}
