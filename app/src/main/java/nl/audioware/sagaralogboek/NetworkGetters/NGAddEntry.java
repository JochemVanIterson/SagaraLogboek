package nl.audioware.sagaralogboek.NetworkGetters;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.R;
import nl.audioware.sagaralogboek.Services.SenderService;

public class NGAddEntry {
    DefaultNetGetter netGetter;

    public NGAddEntry(Context context, String BaseUrl, String User, String iv, final int item_id, String type, final Location location, JSONObject data_start) {
        String url = BaseUrl + "Scripts/Entry.php?action=Insert";

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", User);
        params.put("iv", iv);

        params.put("item_id", String.valueOf(item_id));
        params.put("type", type);

        JSONObject dataUpdate = new JSONObject();
        try {
            SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String date = simpleDate.format(new Date());
            dataUpdate.put("longitude", location.getLongitude());
            dataUpdate.put("latitude", location.getLatitude());
            dataUpdate.put("accuracy", location.getAccuracy());
            dataUpdate.put("speed", location.getSpeed());
            dataUpdate.put("date", date);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put("dataUpdate", dataUpdate.toString());
        params.put("data_start", data_start.toString());
        Log.d("data_start", data_start.toString());

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String DeviceID = telephonyManager.getDeviceId();
        params.put("device_id", DeviceID);

        netGetter = new DefaultNetGetter(context, url, params) {
            @Override
            public void ActionDone(String response, final Context context) {
                try {
                    Log.d("DataGetter", response);
                    JSONObject JsonResponse = new JSONObject(response);
                    int entryID = JsonResponse.getInt("entry_id");
                    Intent senderServiceIntent = new Intent(SenderService.ACTION_ENTRY_ID);
                    senderServiceIntent.setClass(context, SenderService.class);
                    senderServiceIntent.putExtra("entryID", entryID);
                    senderServiceIntent.putExtra("itemID", item_id);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(senderServiceIntent);
                    } else {
                        context.startService(senderServiceIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", response);
                }
            }

            @Override
            public void ActionError(Context context, VolleyError error){
                super.ActionError(context, error);
                Intent senderServiceIntent = new Intent(SenderService.ACTION_UPLOAD_FAILED);
                senderServiceIntent.setClass(context, SenderService.class);
                ArrayList<Location> locations = new ArrayList<>();
                locations.add(location);
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
