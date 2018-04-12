package nl.audioware.sagaralogboek.Libraries;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class ServiceRunning {
    final public boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if(service.service.getClassName().contains(context.getPackageName())){
                Log.d("getClassName", service.service.getClassName());
            }
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //final public boolean isServiceRunning(Context context, String serviceName){
    //    ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    //    List<ActivityManager.RunningServiceInfo> l = am.getRunningServices(Integer.MAX_VALUE);
    //    Iterator<ActivityManager.RunningServiceInfo> i = l.iterator();
    //    while (i.hasNext()) {
    //        ActivityManager.RunningServiceInfo runningServiceInfo = i.next();
    //
    //        if(runningServiceInfo.service.getClassName().contains(context.getPackageName())){
    //            Log.d("getClassName", runningServiceInfo.service.getClassName());
    //        }
    //        if(runningServiceInfo.service.getClassName().equals(context.getPackageName() + "." + serviceName)){
    //            return true;
    //        }
    //    }
    //    return false;
    //}
}
