package nl.audioware.sagaralogboek.NetworkGetters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Activities.BigMapActivity;
import nl.audioware.sagaralogboek.Activities.ItemActivity;

public class NGGetLocations {
    DefaultNetGetter netGetter;

    public NGGetLocations(final Activity activity, String BaseUrl, String User, String iv){
        String url = BaseUrl + "Scripts/LocationData.php";

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", User);
        params.put("iv", iv);

        netGetter = new DefaultNetGetter(activity, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                try {
                    Log.d("DataGetter", response);
                    JSONObject JsonResponse = new JSONObject(response);
                    //JSONObject itemObject = JsonResponse.getJSONObject("item");
                    //Log.d("itemObject", itemObject.toString(3));
                    //JSONArray entryObject = JsonResponse.optJSONArray("entry");
                    //Log.d("entryObject", entryObject.toString(3));
                    ((BigMapActivity)activity).drawMarkers(JsonResponse);
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
