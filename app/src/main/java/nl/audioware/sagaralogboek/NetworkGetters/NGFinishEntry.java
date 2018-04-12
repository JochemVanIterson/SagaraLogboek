package nl.audioware.sagaralogboek.NetworkGetters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Services.SenderService;

public class NGFinishEntry {
    DefaultNetGetter netGetter;

    public NGFinishEntry(Context context, String BaseUrl, String User, String iv, int item_id, int entry_id){
        String url = BaseUrl + "Scripts/Entry.php?action=Finish";

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", User);
        params.put("iv", iv);

        params.put("item_id", String.valueOf(item_id));
        params.put("entry_id", String.valueOf(entry_id));

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String DeviceID = telephonyManager.getDeviceId();
        params.put("device_id", DeviceID);

        netGetter = new DefaultNetGetter(context, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                try {
                    Log.d("DataGetter", "NGFinishEntry: " + response);
                    JSONObject JsonResponse = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", response);
                }
            }

            @Override
            public void get(String tag){
                //dialog.show();
                super.get(tag);
            }
        };
    }

    public void get(){
        netGetter.get("DataGetter");
    }
}
