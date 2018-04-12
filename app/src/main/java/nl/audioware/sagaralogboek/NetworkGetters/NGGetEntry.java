package nl.audioware.sagaralogboek.NetworkGetters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Activities.ItemActivity;
import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.R;

public class NGGetEntry {
    DefaultNetGetter netGetter;

    public NGGetEntry(final Activity activity, String BaseUrl, String User, String iv, int id){
        String url = BaseUrl + "Scripts/Entry.php?action=Get";

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", User);
        params.put("iv", iv);
        params.put("id", String.valueOf(id));

        netGetter = new DefaultNetGetter(activity, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                try {
                    Log.d("DataGetter", response);
                    JSONObject JsonResponse = new JSONObject(response);
                    JSONObject dataObject = JsonResponse.getJSONObject("data");
                    Log.d("dataObject", dataObject.toString(3));
                    //((ItemActivity)activity).drawLineMap(dataObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", response);
                }
            }

            @Override
            public void get(String tag){

                super.get(tag);
            }
        };
    }

    public void get(){
        netGetter.get("DataGetter");
    }
}
