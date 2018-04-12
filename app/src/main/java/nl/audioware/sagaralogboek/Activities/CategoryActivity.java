package nl.audioware.sagaralogboek.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import nl.audioware.sagaralogboek.Adapters.ItemAdapter;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.NetworkGetters.NGDataGetter;
import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.R;

public class CategoryActivity extends AppCompatActivity {
    File CacheDataFolder;
    Category category;
    ArrayList<Item> Items = new ArrayList<>();
    private RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private NGDataGetter DataGetter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        SharedPreferences Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //TextView textView = findViewById(R.id.textView);
        loadData();
        actionBar.setTitle(category.getName());

        mRecyclerView = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ItemAdapter(this, Items);
        mRecyclerView.setAdapter(mAdapter);

        DataGetter = new NGDataGetter(this, Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", ""));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                DataGetter.get();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void loadData(){
        int categoryID = getIntent().getIntExtra("CategoryID", -1);
        CacheDataFolder = new File(getCacheDir(), "data/");
        String categoriesFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "categories.json"));
        String itemsFileData = new FileHandler().readFromFile(new File(CacheDataFolder, "items.json"));
        try {
            JSONArray categoriesArray = new JSONArray(categoriesFileData);
            JSONArray itemsArray = new JSONArray(itemsFileData);
            for (int i = 0; i <categoriesArray.length() ; i++) {
                JSONObject tmpObject = categoriesArray.getJSONObject(i);
                if(tmpObject.optInt("id", -1)==categoryID){
                    category = new Category(tmpObject);
                    break;
                }
            }
            for (int i = 0; i <itemsArray.length() ; i++) {
                JSONObject tmpObject = itemsArray.getJSONObject(i);
                if(tmpObject.optInt("category_id", -1)==categoryID){
                    Item tmpItem = new Item(tmpObject);
                    Log.d("imgURL_B", "test: "+tmpItem.getImageURL());
                    if(tmpItem.getImageURL().equals("")){
                        tmpItem.setImageURL(category.getImageURL());
                    }
                    Log.d("imgURL_A", "test: "+tmpItem.getImageURL());
                    Items.add(tmpItem);
                }
            }
            TextView emptyTextView = findViewById(R.id.emptyTextView);
            if(Items.size()==0){
                emptyTextView.setVisibility(View.VISIBLE);
            } else {
                emptyTextView.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("catFileData" ,categoriesFileData);
        Log.d("catFileData" ,itemsFileData);
    }
}