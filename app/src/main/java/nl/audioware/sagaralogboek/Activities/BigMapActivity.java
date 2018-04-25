package nl.audioware.sagaralogboek.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.NetworkGetters.NGGetItem;
import nl.audioware.sagaralogboek.NetworkGetters.NGGetLocations;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.User;
import nl.audioware.sagaralogboek.R;

public class BigMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapboxMap mapboxMap;

    SharedPreferences Settings;

    FloatingActionButton fab;

    private ArrayList<LatLng> LatLongs = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoiam9uYXNvbjEyMyIsImEiOiJjaXEzdDZqaTkwMDc5aHJtMmxtamUybGZ4In0.dCDpvuXzdNAWidXp-q3BzQ");

        Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

        setContentView(R.layout.activity_big_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
            latLngBounds.includes(LatLongs);
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 100));
            fab.setVisibility(View.GONE);
        });

        mapView = (MapView) findViewById(R.id.big_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    public void drawMarkers(JSONObject response){
        try {
            LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
            JSONArray locationArray = response.getJSONArray("data");
            for(int locationWalker = 0; locationWalker<locationArray.length(); locationWalker++){
                JSONObject object = locationArray.getJSONObject(locationWalker);
                JSONObject locationData = object.getJSONObject("location_data");
                LatLongs.add(new LatLng(locationData.getDouble("latitude"), locationData.getDouble("longitude")));
                latLngBounds.include(new LatLng(locationData.getDouble("latitude"), locationData.getDouble("longitude")));
                Item item = new FileHandler().getItem(this, object.getInt("item_id"));
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(locationData.getDouble("latitude"), locationData.getDouble("longitude")))
                        .title(item.getName())
                );
            }
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 100));
            fab.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setAttributionEnabled(false);
        mapboxMap.getUiSettings().setLogoEnabled(true);
        mapboxMap.setMaxZoomPreference(18);
        mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
            @Override
            public void onMoveBegin(MoveGestureDetector detector) {
                fab.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMove(MoveGestureDetector detector) {

            }

            @Override
            public void onMoveEnd(MoveGestureDetector detector) {

            }
        });
        new NGGetLocations(this, Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", "")).get();
    }
}