package nl.audioware.sagaralogboek.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import nl.audioware.sagaralogboek.Adapters.MainAdapter;
import nl.audioware.sagaralogboek.Dialogs.lockfileDialog;
import nl.audioware.sagaralogboek.Dialogs.startDialog;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.Libraries.ServiceRunning;
import nl.audioware.sagaralogboek.NetworkGetters.NGDataGetter;

import nl.audioware.sagaralogboek.Objects.Card_itm;
import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;

import nl.audioware.sagaralogboek.Objects.ItemComparator;
import nl.audioware.sagaralogboek.Objects.User;
import nl.audioware.sagaralogboek.R;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;

    public static ArrayList<Category> Categories = new ArrayList<>();
    public static ArrayList<Item> Items = new ArrayList<>();
    public static ArrayList<User> Users = new ArrayList<>();
    public static ArrayList<Card_itm> Card_itms = new ArrayList<>();

    private RecyclerView.LayoutManager mLayoutManager;
    private NGDataGetter DataGetter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // *********************** UI setup *********************** //
        super.onCreate(savedInstanceState);
        Log.d("PackagePath", this.getPackageName());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

        // *********************** Fab button *********************** //
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        checkPermissions();

        // *********************** Recycler view *********************** //
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataGetter.get();
            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MainAdapter(this, Card_itms);
        mRecyclerView.setAdapter(mAdapter);

        // *********************** Data *********************** //
        includeFromFile(this);
        DataGetter = new NGDataGetter(this, Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", ""));
        DataGetter.get();

        File Lockfile = new File(getFilesDir(), "lockfile");
        Log.d("Lockfile", Lockfile.getAbsolutePath());

        if(new FileHandler().LockFileExists(this) && !(new ServiceRunning().isServiceRunning(this, "nl.audioware.sagaralogboek.sender_service"))){
            DialogFragment lockfileDialog = new lockfileDialog();
            lockfileDialog.show(getFragmentManager(), "lockfileDialog");
        }
    }

    public void checkPermissions() {
        int LocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int PhoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        ArrayList<String> permissionRequestQueue = new ArrayList<>();
        if (LocationPermission != PackageManager.PERMISSION_GRANTED) {
            permissionRequestQueue.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (PhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            permissionRequestQueue.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (permissionRequestQueue.size() != 0) {
            String[] Array = new String[permissionRequestQueue.size()];
            ActivityCompat.requestPermissions(this, permissionRequestQueue.toArray(Array), 1263);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        DataGetter.get();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout: // Remove credentials when logout, then finish
                SharedPreferences Settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor SettingsEditor = Settings.edit();
                SettingsEditor.remove("User");
                SettingsEditor.remove("PW");
                SettingsEditor.remove("iv");
                SettingsEditor.apply();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void includeFromFile(Context context){
        File CacheDataFolder = new File(context.getCacheDir(), "data/");
        String categoriesFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "categories.json"));
        String itemsFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "items.json"));
        String usersFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "users.json"));
        try {
            JSONArray categoriesArray = new JSONArray(categoriesFileData);
            JSONArray itemsArray = new JSONArray(itemsFileData);
            JSONArray usersArray = new JSONArray(usersFileData);
            Categories.clear();
            Items.clear();
            Users.clear();

            for (int i = 0; i <categoriesArray.length() ; i++) {
                JSONObject tmpObject = categoriesArray.getJSONObject(i);
                Categories.add(new Category(tmpObject));
            }
            for (int i = 0; i <itemsArray.length() ; i++) {
                JSONObject tmpObject = itemsArray.getJSONObject(i);
                Item tmpItm = new Item(tmpObject);
                tmpItm.setCategory(new FileHandler().getCategory(context, tmpObject.optInt("category_id")));
                tmpItm.setSailingUser(new FileHandler().getUser(context, tmpObject.optInt("sailing_user")));
                Items.add(tmpItm);

            }
            Collections.sort(Items, new ItemComparator());

            for (int i = 0; i <usersArray.length() ; i++) {
                JSONObject tmpObject = usersArray.getJSONObject(i);
                Users.add(new User(tmpObject));
            }
            loadCards(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void loadCards(Context context){
        Card_itms.clear();

        SharedPreferences PubSettings = context.getSharedPreferences(context.getString(R.string.prefs_Pub), MODE_PRIVATE);
        String FavoritesSTR = PubSettings.getString("favorites", "");

        try {
            if(!FavoritesSTR.equals("")) {
                JSONArray Favorites = new JSONArray(FavoritesSTR);
                for (int i = 0; i < Favorites.length(); i++) {
                    int tmpID = Favorites.getInt(i);
                    for (int j = 0; j < Items.size(); j++) {
                        Item tmpItem = Items.get(j);
                        if (tmpItem.getId() == tmpID) {
                            if (tmpItem.getImageURL().equals("")) {
                                tmpItem.setImageURL(tmpItem.getCategory().getImageURL());
                            }
                            Card_itms.add(tmpItem.toCard());
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("FavoritesSTR", FavoritesSTR);
        for (int i = 0; i < Categories.size(); i++) {
            Card_itm tmpCard = Categories.get(i).toCard();
            Card_itms.add(tmpCard);
        }
        mAdapter.notifyDataSetChanged();
    }
}
