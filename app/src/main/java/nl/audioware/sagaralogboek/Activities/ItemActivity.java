package nl.audioware.sagaralogboek.Activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import nl.audioware.sagaralogboek.Adapters.mapAdapter;
import nl.audioware.sagaralogboek.Dialogs.startDialog;
import nl.audioware.sagaralogboek.Dialogs.stopDialog;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVG;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVGParser;
import nl.audioware.sagaralogboek.NetworkGetters.NGGetItem;
import nl.audioware.sagaralogboek.Objects.DbEntry;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.User;
import nl.audioware.sagaralogboek.R;

public class ItemActivity extends AppCompatActivity implements OnMapReadyCallback {
    int ItemID;
    public static Item item;

    boolean isFavorite = false;
    MenuItem actionFav;

    public static boolean isSailing = false;

    SharedPreferences PubSettings, Settings;

    private ScrollView scrollView;

    private LineChart mChart;

    private MapView mapView;
    private MapboxMap mapboxMap;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    public  RecyclerView.Adapter mAdapter;

    public ArrayList<DbEntry> MapEntries = new ArrayList<>();
    public ArrayList<DbEntry> MapEntriesVisible = new ArrayList<>();
    public int VisibleEntries = 2;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *********************** UI setup *********************** //
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoiam9uYXNvbjEyMyIsImEiOiJjaXEzdDZqaTkwMDc5aHJtMmxtamUybGZ4In0.dCDpvuXzdNAWidXp-q3BzQ");
        context = this;

        ItemID = getIntent().getIntExtra("ItemID", -1);

        setContentView(R.layout.activity_item);
        PubSettings = getSharedPreferences(getString(R.string.prefs_Pub), MODE_PRIVATE);
        Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

        scrollView = findViewById(R.id.item_scrollView);

        loadDataFromFile();

        new NGGetItem(this, Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", ""), ItemID).get();

        // *********************** Action bar *********************** //
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(item.getName());

        // *********************** Image *********************** //
        File imageFile = new File(item.getImageURL());
        ImageView imgView = findViewById(R.id.imageView);
        try {
            Drawable drawable = svgFileDrawable(imageFile);
            imgView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            imgView.setImageDrawable(drawable);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // *********************** Chart *********************** //
        if(item.getData().has("diesel_peil")) {

        } else {
            CardView cv_chart = findViewById(R.id.cv_chart);
            cv_chart.setVisibility(View.GONE);
        }

        // *********************** Favorite *********************** //
        String FavoritesSTR = PubSettings.getString("favorites", "");
        try {
            JSONArray Favorites = new JSONArray(FavoritesSTR);
            int arrayPos = JSONArrayValueOf(Favorites, this.item.getId());
            if(arrayPos!=-1)isFavorite = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // *********************** Map *********************** //
        mapView = (MapView) findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // *********************** Map List *********************** //
        mRecyclerView = findViewById(R.id.mapRecyclerView);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new mapAdapter(this, MapEntriesVisible);
        mRecyclerView.setAdapter(mAdapter);


        // *********************** Fab button *********************** //
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSailing){
                    stopDialog((Activity)context);
                } else {
                    startDialog((Activity) context);
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        scrollView.smoothScrollTo(0, 0);
    }

    public void insertData(JSONObject object){
        // *********************** Receive Data *********************** //
        item = new FileHandler().json2item(this, object.optJSONObject("item"));
        if(!object.optString("entry", "null").equals("null")) {
            JSONArray entriesJS = object.optJSONArray("entry");
            if (entriesJS.length() > 0) {
                for (int i = 0; i < entriesJS.length(); i++) {
                    MapEntries.add(new DbEntry(this, entriesJS.optJSONObject(i)));
                }
            }
        }

        // *********************** Sailing User *********************** //
        FloatingActionButton fab = findViewById(R.id.fab);
        if(item.getSailingUser()!=null){
            Log.d("isSailing", String.valueOf(item.getSailingUser().getId()));
            TextView tvUserSailing = findViewById(R.id.tv_user_sailing);
            User SailingUser = item.getSailingUser();
            String currentUsername = Settings.getString("User", "");
            if(SailingUser.getUserName().equals(currentUsername)){
                isSailing = true;
            } else {
                fab.setVisibility(View.GONE);
                isSailing = false;
            }
            String isSailingText = "Sailing: " + SailingUser.getFirstName() + " " + SailingUser.getLastName();
            tvUserSailing.setText(isSailingText);
        } else {
            CardView cv_user_sailing = findViewById(R.id.cv_user_sailing);
            cv_user_sailing.setVisibility(View.GONE);
        }
        if(isSailing) {
            fab.setImageResource(R.drawable.ic_anchor);
        }

        // *********************** Chart *********************** //
        String data = item.getData().optString("diesel_peil");
        TextView emptyChart = findViewById(R.id.chartEmpty);
        boolean found = false;
        for (int i = 0; i <MapEntries.size() ; i++) {
            DbEntry tmpEntry = MapEntries.get(i);
            if(tmpEntry.getData_start().optInt("diesel_peil", 0)!=0 && !found){
                found = true;
            }
        }
        if(found){
            emptyChart.setVisibility(View.GONE);
        } else {
            emptyChart.setVisibility(View.VISIBLE);
        }
        loadChart(!found);

        // *********************** Map List *********************** //
        MapEntriesVisible.clear();
        for (int i = 0; i <VisibleEntries ; i++) {
            Log.d("VisibleEntries", String.valueOf(VisibleEntries));
            if(i<MapEntries.size()){
                MapEntriesVisible.add(MapEntries.get(i));
            }
        }
        mAdapter.notifyDataSetChanged();

        if(MapEntriesVisible.size()==0){
            TextView EntryEmptyTV = findViewById(R.id.EntryEmpty);
            EntryEmptyTV.setVisibility(View.VISIBLE);
        }

        final Button loadMoreButton = findViewById(R.id.but_loadmore);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapEntriesVisible.clear();
                VisibleEntries += 5;
                for (int i = 0; i <VisibleEntries ; i++) {
                    if(i<MapEntries.size()){
                        MapEntriesVisible.add(MapEntries.get(i));
                    }
                }
                if(VisibleEntries>=MapEntries.size()){
                    loadMoreButton.setEnabled(false);
                    loadMoreButton.setText("No more data");
                }
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    private void loadDataFromFile(){
        item = new FileHandler().getItem(this, ItemID);
        if(item.getImageURL().equals("")){
            item.setImageURL(item.getCategory().getImageURL());
        }
    }

    private void loadChart(boolean bogus){
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setViewPortOffsets(0, 0, 0, 50);
        mChart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setScaleYEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        //mChart.setScaleX(600000);

        //mChart.setDrawGridBackground(false);
        //mChart.setMaxHighlightDistance(300);

        XAxis x = mChart.getXAxis();
        x.setDrawGridLines(true);
        x.setDrawAxisLine(true);
        x.setDrawLabels(true);

        x.setTextColor(Color.WHITE);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setAxisMinimum(0);
        x.setAxisMaximum(1000L);
        x.setAxisLineColor(Color.WHITE);

        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setEnabled(false);

        YAxis y = mChart.getAxisLeft();
        y.setAxisMinimum(0);
        y.setAxisMaximum(100);

        // add data
        if(bogus) {
            setBogusData(45, 100);
        } else {
            setData();
        }

        mChart.getLegend().setEnabled(false);

        mChart.animateX(2000);

        // dont forget to refresh the drawing
        mChart.invalidate();
    }

    private void setBogusData(int count, float range) {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range);
            yVals.add(new Entry(i, val));
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals, "DataSet 1");

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            set1.setCircleColor(Color.WHITE);
            set1.setHighlightEnabled(false);
            //set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(200);
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setFillFormatter(new IFillFormatter() {
            //    @Override
            //    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
            //        return -10;
            //    }
            //});

            // create a data object with the datasets
            LineData data = new LineData(set1);
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            mChart.setData(data);
        }
    }

    private void setData() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        //yVals.add(new Entry(0, 0));
        ArrayList<DbEntry> tmpList = new ArrayList<DbEntry>();
        tmpList.addAll(MapEntries);

        long offset = MapEntries.get(MapEntries.size()-1).getDatetime_start().getTime();

        Log.d("MapEntries", String.valueOf(tmpList.size()));
        if(tmpList.size()==0){
            setBogusData(45, 100);
            return;
        }
        for (int i = 0; i <tmpList.size() ; i++) {
            DbEntry tmpEntry = tmpList.get(i);
            yVals.addAll(tmpEntry.getChartData(offset));
        }

        Log.d("yValsSize", String.valueOf(yVals.size()));

        Collections.sort(yVals, new EntryXComparator());

        float nowDate = new Date().getTime();
        float nowState = yVals.get(yVals.size()-1).getY();
        Entry nowEntry = new Entry(nowDate, nowState);
        yVals.add(nowEntry);


        for (int i = 0; i <yVals.size() ; i++) {
            Log.d("yVals", yVals.get(i).getX() + ":" + yVals.get(i).getY());
        }

        LineDataSet set1;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals, "DataSet 1");

            set1.setMode(LineDataSet.Mode.LINEAR);
            //set1.setCubicIntensity(0.2f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            //set1.setCircleRadius(1f);
            set1.setCircleColor(Color.WHITE);
            set1.setHighlightEnabled(false);
            //set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(200);
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setFillFormatter(new IFillFormatter() {
            //    @Override
            //    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
            //        return -10;
            //    }
            //});

            // create a data object with the datasets
            LineData data = new LineData(set1);
            Log.d("getDataSetCount", String.valueOf(yVals.size()));
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            mChart.getXAxis().setAxisMinimum(set1.getXMin());
            mChart.getXAxis().setAxisMaximum(set1.getXMax());
            mChart.setVisibleXRangeMinimum(10);
            mChart.setData(data);
        }
    }

    public void drawLineMap(ArrayList<LatLng> latLngsRaw, boolean set){
        mapboxMap.clear();

        TextView emptyChart = findViewById(R.id.mapEmpty);
        if(latLngsRaw.size()==0){
            emptyChart.setVisibility(View.VISIBLE);
        } else {
            emptyChart.setVisibility(View.GONE);
        }

        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (int i = 0; i <latLngsRaw.size() ; i++) {
            LatLng TmpLatLng = latLngsRaw.get(i);
            if(!latLngs.contains(TmpLatLng))latLngs.add(TmpLatLng);
        }

        if(latLngs.size()==1){
            MarkerOptions markerOptions = new MarkerOptions().position(latLngs.get(0));

            mapboxMap.addMarker(markerOptions);

            CameraPosition position = new CameraPosition.Builder()
                    .target(latLngs.get(0)) // Sets the new camera position
                    .zoom(18) // Sets the zoom to level 10
                    .build(); // Builds the CameraPosition object from the builder

            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 100);
        } else if(latLngs.size()>1){
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(latLngs)
                    .color(getResources().getColor(R.color.colorAccent))
                    .width(3);

            LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
            mapboxMap.addPolyline(polylineOptions);

            latLngBounds.includes(latLngs);

            Log.d("latLngs", String.valueOf(latLngs.size()));

            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 50));
        }
    }

    public Drawable svgFileDrawable(File dataFile) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(dataFile);
        SVG svg = SVGParser.getSVGFromInputStream(inputStream);
        return svg.createPictureDrawable();
    }

    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( R.menu.menu_item, menu);
        actionFav = menu.findItem(R.id.action_fav);
        updateFavStatus();
        return true;
    }

    public void updateFavStatus() {
        if(isFavorite){
            actionFav.setIcon(R.drawable.ic_fav_yes);
        } else {
            actionFav.setIcon(R.drawable.ic_fav_no);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_fav:
                isFavorite = !isFavorite;
                SharedPreferences PubSettings = getSharedPreferences(getString(R.string.prefs_Pub), MODE_PRIVATE);
                String FavoritesSTR = PubSettings.getString("favorites", "");
                try {
                    JSONArray Favorites;
                    if(FavoritesSTR.equals("")){
                        Favorites = new JSONArray();
                    } else {
                        Favorites = new JSONArray(FavoritesSTR);
                    }
                    int arrayPos = JSONArrayValueOf(Favorites, this.item.getId());
                    if(isFavorite && arrayPos==-1){
                        Favorites.put(this.item.getId());
                    } else if(!isFavorite && arrayPos!=-1){
                        Favorites.remove(arrayPos);
                    }
                    SharedPreferences.Editor editor = PubSettings.edit();
                    editor.putString("favorites", Favorites.toString());
                    editor.commit();
                    //MainActivity.loadCards(this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateFavStatus();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public int JSONArrayValueOf(JSONArray array, Object object) throws JSONException {
        int output = -1;
        for(int i = 0; i<array.length(); i++){
            if(array.get(i).equals(object))output = i;
        }
        return output;
    }

    public void startDialog(Activity activity){
        DialogFragment newFragment = new startDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("itemID", item.getId());
        newFragment.setArguments(bundle);
        newFragment.show(activity.getFragmentManager(), "startDialog" + item.getId());
    }
    public void stopDialog(Activity activity){
        DialogFragment newFragment = new stopDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("itemID", item.getId());
        newFragment.setArguments(bundle);
        newFragment.show(activity.getFragmentManager(), "stopDialog" + item.getId());
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
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.getUiSettings().setAllGesturesEnabled(false);
        mapboxMap.getUiSettings().setAttributionEnabled(false);
        mapboxMap.getUiSettings().setLogoEnabled(false);
        mapboxMap.setMaxZoomPreference(18);

        MapEntriesVisible.get(0).setSelected(true);
        drawLineMap(MapEntries.get(0).getLatLngs(), true);
        mAdapter.notifyDataSetChanged();
    }
}
