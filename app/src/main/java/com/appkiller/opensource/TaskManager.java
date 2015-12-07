package com.appkiller.opensource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.appkiller.opensource.TaskListAdapters.ProcessListAdapter;
import com.appkiller.opensource.TaskListAdapters.TasksListAdapter;

//import com.google.masf.OneTimeCache;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class TaskManager extends Activity {
    public static final boolean DEBUG = true;
    public static final String TAG = "FreeTaskManager";
    private ProcessInfo pinfo = null;
    ActivityManager am = null;
    private PackagesInfo packageinfo = null;
    // private List<RunningTaskInfo> tasklist = null;
    private PackageManager pm;

    private static final int STAT_TASK = 0;
    private static final int STAT_SERVICE = 1;
    private static final int STAT_SYSTEM = 2;
    protected static final String ACTION_LOAD_FINISH = "org.freecoder.taskmanager.ACTION_LOAD_FINISH";

    private int currentStat = STAT_TASK;
    private ProcessListAdapter adapter;
    private BroadcastReceiver loadFinish = new LoadFinishReceiver();
    private ArrayList<DetailProcess> listdp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.main);
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        pm = this.getApplicationContext().getPackageManager();
        findViewById(R.id.btn_task).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                currentStat = STAT_TASK;
                refresh();
            }

        });
        packageinfo = new PackagesInfo(this);
                
    }

    ListView getListView() {
        return (ListView) this.findViewById(R.id.listbody);
    }
    
    public void refresh() {
        setProgressBarIndeterminateVisibility(true);
        if (currentStat == STAT_TASK) {

            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    pinfo = new ProcessInfo();
                    getRunningProcess();
                    
                    Intent in = new Intent(ACTION_LOAD_FINISH);
                    TaskManager.this.sendBroadcast(in);
                }

            });
            t.start();
        }
        // tasklist = am.getRunningTasks(100);
    }

    public ProcessInfo getProcessInfo() {
        return pinfo;
    }

    public PackagesInfo getPackageInfo() {
        return packageinfo;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // System.out.println("change");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ACTION_LOAD_FINISH);
        this.registerReceiver(loadFinish, filter);
        packageinfo = new PackagesInfo(this);
        // Make sure the progress bar is visible

        refresh();
    }

    @SuppressWarnings("unchecked")
    public void getRunningProcess() {
        List<RunningAppProcessInfo> list2 = am.getRunningAppProcesses();
        listdp = new ArrayList<DetailProcess>();
        for (RunningAppProcessInfo ti : list2) {
            // System.out.println(ti.processName + "/" + ti.pid + "/" + ti.lru + "/" + ti.importance
            // + "/"
            // + Arrays.toString(ti.pkgList) + "\n\n");
            if (ti.processName.equals("system") || ti.processName.equals("com.android.phone")) {
                continue;
            }
            DetailProcess dp = new DetailProcess(this, ti);
            dp.fetchApplicationInfo(packageinfo);
            dp.fetchPackageInfo();
            dp.fetchPsRow(pinfo);
            // dp.fetchTaskInfo(this);
            if (dp.isGoodProcess()) {
                listdp.add(dp);
                // System.out.println(Arrays.toString(dp.getPkginfo().activities));
            }
        }
        Collections.sort(listdp);
        adapter = new ProcessListAdapter(this, listdp);
    }

    // public RunningTaskInfo getTaskInfo(String name) {
    // if (tasklist != null) {
    // for (RunningTaskInfo ti : tasklist) {
    // if (ti.baseActivity.getPackageName().equals(name)) {
    // return ti;
    // }
    // }
    // }
    // return null;
    // }

    public List<RunningTaskInfo> _getRunningTask() {

        List<RunningTaskInfo> list = am.getRunningTasks(100);
        for (RunningTaskInfo ti : list) {
            System.out.println(ti.baseActivity.getPackageName() + "/" + ti.baseActivity.getClassName() + "/"
                    + ti.id);
        }
        ListAdapter adapter = new TasksListAdapter(this, list);
        getListView().setAdapter(adapter);
        List<RunningAppProcessInfo> list2 = am.getRunningAppProcesses();
        for (RunningAppProcessInfo ti : list2) {
            System.out.println(ti.processName + "/" + ti.pid + "/" + ti.lru + "/" + ti.importance + "/"
                    + Arrays.toString(ti.pkgList) + "\n\n");
        }
        List<RunningServiceInfo> list3 = am.getRunningServices(100);
        for (RunningServiceInfo ti : list3) {
            System.out.println(ti.service.getPackageName() + "/" + ti.service.getClassName());
        }
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);
        System.out.println(outInfo.availMem);
        return list;
    }

    // am.restartPackage("org.freecoder.securesms");

    // Intent i = new Intent(Intent.ACTION_MAIN);
    // i.setClassName("org.freecoder.securesms", "org.freecoder.securesms.SecureSms");
    // startActivity(i);

    private class LoadFinishReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context ctx, Intent intent) {
            TaskManager.this.setProgressBarIndeterminateVisibility(false);
            TaskManager.this.getListView().setAdapter(adapter);
            TaskManager.this.getListView().setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (currentStat == STAT_TASK) {
                        DetailProcess dp = listdp.get(arg2);
                        MiscUtil.getTaskMenuDialog(TaskManager.this, dp).show();
                    }
                    //System.out.println(arg2);
                }
                
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(loadFinish);
    }

    
}
