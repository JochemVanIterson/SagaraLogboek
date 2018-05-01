package nl.audioware.sagaralogboek.Objects;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
//import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import nl.audioware.sagaralogboek.Libraries.FileHandler;

public class DbEntry {
    private int id;
    private int item_id;
    private int user_id;
    private User user;
    private String device_id;
    private String type;
    //private ArrayList<LatLng> latLngs = new ArrayList<>();
    private JSONObject data_start;
    private JSONObject data_stop;
    private Date datetime_start;
    private Date last_update;
    private Date datetime_stop;

    public boolean selected = false;
    public boolean visible = false;

    public DbEntry(Context context, JSONObject object){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.id = object.optInt("id");
        this.item_id = object.optInt("item_id");
        this.user_id = object.optInt("user_id");
        this.user = new FileHandler().getUser(context, user_id);
        this.device_id = object.optString("device_id");
        this.type = object.optString("type");
        if(object.optJSONArray("location_data")!=null) {
            try {
                Create_LocArray(object.optJSONArray("location_data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            Log.d("object", object.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(object.has("data_start") && !object.optString("data_start", "null").equalsIgnoreCase("null")) {
            try {
                data_start = new JSONObject(object.getString("data_start"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("data_start", object.optString("data_start", "null"));
        } else {
            Log.d("data_start", "empty");
            data_start = new JSONObject();
        }
        if(object.has("data_stop") && !object.optString("data_stop", "null").equalsIgnoreCase("null")) {
            try {
                data_stop = new JSONObject(object.getString("data_stop"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("data_stop", object.optString("data_stop", "null"));
        } else {
            Log.d("data_stop", "empty");
            data_stop = new JSONObject();
        }
        try {
            this.datetime_start = fmt.parse(object.optString("datetime_start"));
            this.last_update = fmt.parse(object.optString("last_update"));
            if(!object.isNull("datetime_stop")){
                this.datetime_stop = fmt.parse(object.optString("datetime_stop"));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void Create_LocArray(JSONArray array) throws JSONException {
        //Log.d("Create_LocArray", array.toString(2));
        for (int i = 0; i<array.length() ; i++) {
            JSONObject tmpObject = array.optJSONObject(i);
            //Log.d("Create_LocArray", i + "\n"+ tmpObject.toString(2));
            Double latitude = tmpObject.optDouble("latitude");
            Double longitude = tmpObject.optDouble("longitude");
            //latLngs.add(new LatLng(latitude, longitude));
        }
    }

    public int getId() {
        return id;
    }
    public int getItem_id() {return item_id;}
    public int getUser_id() {return user_id;}
    public User getUser() {return user;}

    public String getDevice_id() {return device_id;}
    public String getType() {return type;}
    
    //public ArrayList<LatLng> getLatLngs() {return latLngs;}

    public JSONObject getData_start() {return data_start;}
    public JSONObject getData_stop() {return data_stop;}

    public Date getDatetime_start() {return datetime_start;}
    public Date getLast_update() {return last_update;}
    public Date getDatetime_stop() {return datetime_stop;}

    public ArrayList<Entry> getChartData(long offset){
        ArrayList<Entry> list = new ArrayList<Entry>();
        float startTime = (datetime_start.getTime());
        if(data_start!=null && data_start.has("diesel_peil")){
            Log.d("data_startChart", data_start.toString());
            list.add(new Entry(startTime, data_start.optInt("diesel_peil", 0)));
        }
        //if(datetime_stop!=null){
        //    float stopTime = (datetime_stop.getTime()-offset)/3600000f;
        //    try {
        //        Log.d("data_stop", data_stop.toString(2));
        //    } catch (JSONException e) {
        //        e.printStackTrace();
        //    }
        //    list.add(new Entry(stopTime, data_stop.optInt("diesel_peil", 0)));
        //}
        return list;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
}
