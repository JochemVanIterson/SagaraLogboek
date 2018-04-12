package nl.audioware.sagaralogboek.NetworkGetters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Services.SenderService;

public class NGUpdateEntry {
    DefaultNetGetter netGetter;

    public NGUpdateEntry(Context context, String BaseUrl, String User, String iv, int entry_id, final ArrayList<Location> locations){
        String url = BaseUrl + "Scripts/Entry.php?action=UpdateMulti";

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", User);
        params.put("iv", iv);

        params.put("entry_id", String.valueOf(entry_id));
        JSONArray dataUpdate = new JSONArray();
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            try {
                SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String date = simpleDate.format(new Date());
                JSONObject tmpObject = new JSONObject();
                tmpObject.put("longitude", location.getLongitude());
                tmpObject.put("latitude", location.getLatitude());
                tmpObject.put("accuracy", location.getAccuracy());
                tmpObject.put("speed", location.getSpeed());
                tmpObject.put("date", date);
                dataUpdate.put(tmpObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        params.put("dataUpdate", dataUpdate.toString());

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String DeviceID = telephonyManager.getDeviceId();
        params.put("device_id", DeviceID);

        netGetter = new DefaultNetGetter(context, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                Log.d("DataGetter", "NGUpdateEntry: " + response);

            }

            @Override
            public void ActionError(Context context, VolleyError error){
                super.ActionError(context, error);
                Intent senderServiceIntent = new Intent(SenderService.ACTION_UPLOAD_FAILED);
                senderServiceIntent.setClass(context, SenderService.class);
                senderServiceIntent.putExtra("location", locations);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(senderServiceIntent);
                } else {
                    context.startService(senderServiceIntent);
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
