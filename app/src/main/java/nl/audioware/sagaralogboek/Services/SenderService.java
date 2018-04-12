package nl.audioware.sagaralogboek.Services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVG;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVGParser;
import nl.audioware.sagaralogboek.NetworkGetters.NGAddEntry;
import nl.audioware.sagaralogboek.NetworkGetters.NGFinishEntry;
import nl.audioware.sagaralogboek.NetworkGetters.NGUpdateEntry;
import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.R;

public class SenderService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "SenderService";

    SharedPreferences Settings;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    public static String ACTION_START = "ActionStart";
    public static String ACTION_STOP = "ActionStop";
    public static String ACTION_ENTRY_ID = "ActionEntryID";
    public static String ACTION_UPLOAD_FAILED = "ActionUploadFailed";

    public int itemID = -1;
    public int entryID = -1;

    public Item item;
    public Category category;

    int refresh_speed = 120000;
    int refresh_dist = 0;

    boolean realtime = true;

    ArrayList<Location> locations = new ArrayList<>();

    public JSONObject data_start;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);
        // *********************** Start Service *********************** //
        if(intent.getAction().equals(ACTION_START) && !new FileHandler().LockFileExists(this) && itemID==-1){
            Log.e(TAG, "onStartCommand, ACTION_START");

            itemID = intent.getIntExtra("itemID", -1);
            realtime = intent.getBooleanExtra("realtime", true);
            try {
                data_start = new JSONObject(intent.getStringExtra("data_start"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "itemID: " + itemID);
            includeFromFile(this);

            int NOTIFICATION_ID = Integer.valueOf("100" + itemID);
            Notification notification = getNotification();
            startForeground(NOTIFICATION_ID, notification);
        } else {
            Log.e(TAG, "onStartCommand, ELSE");
            itemID = intent.getIntExtra("itemID", -1);

            // *********************** Insert received entryID *********************** //
            if (intent.getAction().equals(ACTION_ENTRY_ID)) {
                Log.e(TAG, "onStartCommand, ACTION_ENTRY_ID");
                if (intent.hasExtra("entryID")) {
                    entryID = intent.getIntExtra("entryID", -1);

                    JSONObject LockFileContent = new JSONObject();
                    try {
                        LockFileContent.put("itemID", itemID);
                        LockFileContent.put("entryID", entryID);
                        LockFileContent.put("realtime", realtime);
                        new FileHandler().WriteLockFile(this, LockFileContent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.e(TAG, "entryID: " + entryID);
                }
            // *********************** Stop the service *********************** //
            } else if (intent.getAction().equals(ACTION_STOP)) {
                Log.e(TAG, "onStartCommand, ACTION_STOP");
                if (intent.hasExtra("entryID")) {
                    entryID = intent.getIntExtra("entryID", -1);
                }
                new FileHandler().ReleaseLockFile(this);
                stopSelf();
            } else if (intent.getAction().equals(ACTION_UPLOAD_FAILED)) {
                locations = intent.getExtras().getParcelable("location");
            }
        }

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        buildGoogleApiClient();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(SenderService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void createLocationGetter() {
        mLocationRequest = LocationRequest.create();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setFastestInterval(5000); // 5 seconden
        mLocationRequest.setInterval(refresh_speed);
        mLocationRequest.setSmallestDisplacement(refresh_dist);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, SenderService.this);
            Log.d("onConnected", "onConnected");
        } catch (SecurityException ignored) {

        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        mGoogleApiClient.disconnect();
        stopForeground(true);
        sendStopData();
        super.onDestroy();
    }

    private Notification getNotification() {
        String CHANNEL_ID = "status_01";// The id of the channel.
        CharSequence name = "Status";// The user-visible name of the channel.

        Log.e(TAG, "getNotification");
        Notification.Builder builder = new Notification.Builder(this);

        builder.setSmallIcon(R.drawable.ic_boat)
                .setContentTitle("Bezig met varen")
                .setContentText(item.getName());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            builder.setChannelId(CHANNEL_ID);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Intent startIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startIntent, 0);
        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    public Drawable svgFileDrawable(File dataFile) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(dataFile);
        Log.d("dataFile", dataFile.getAbsolutePath());
        SVG svg = SVGParser.getSVGFromInputStream(inputStream);
        return svg.createPictureDrawable();
    }

    public void includeFromFile(Context context) {
        File CacheDataFolder = new File(context.getCacheDir(), "data/");
        String categoriesFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "categories.json"));
        String itemsFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "items.json"));
        try {
            JSONArray categoriesArray = new JSONArray(categoriesFileData);
            JSONArray itemsArray = new JSONArray(itemsFileData);
            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject tmpObject = itemsArray.getJSONObject(i);
                if (tmpObject.optInt("id") == itemID) {
                    for (int j = 0; j < categoriesArray.length(); j++) {
                        JSONObject tmpObjectCat = categoriesArray.getJSONObject(j);
                        if (tmpObjectCat.optInt("id") == tmpObject.optInt("category_id")) {
                            category = new Category(tmpObjectCat);
                            item = new Item(tmpObject, category);
                            break;
                        }
                    }

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendStartData(Location location) {
        String BaseUrl = Settings.getString("BaseUrl", "");
        String UserName = Settings.getString("User", "");
        String iv = Settings.getString("iv", "");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        new NGAddEntry(getApplicationContext(), BaseUrl, UserName, iv, itemID, "Normal", location, data_start).get();
    }

    private void sendLocationData(){
        String BaseUrl = Settings.getString("BaseUrl", "");
        String UserName = Settings.getString("User", "");
        String iv = Settings.getString("iv", "");
        new NGUpdateEntry(getApplicationContext(), BaseUrl, UserName, iv, entryID, locations).get();
        locations.clear();
    }

    private void sendStopData(){
        if(locations.size()>0){
            sendLocationData();
        }
        Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);
        String BaseUrl = Settings.getString("BaseUrl", "");
        String UserName = Settings.getString("User", "");
        String iv = Settings.getString("iv", "");
        new NGFinishEntry(getApplicationContext(), BaseUrl, UserName, iv, itemID, entryID).get();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        createLocationGetter();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: "+ i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        if(entryID==-1){
            sendStartData(location);
        } else {
            if(realtime) {
                locations.add(location);
                sendLocationData();
            } else {
                locations.add(location);
            }
        }
        Log.d(TAG, "onLocationChanged: " + entryID + "   " + location.getLatitude() + "    " + location.getLongitude());
    }
}