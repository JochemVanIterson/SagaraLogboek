package nl.audioware.sagaralogboek.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import nl.audioware.sagaralogboek.Adapters.MainAdapter;
import nl.audioware.sagaralogboek.NetworkGetters.NGDataGetter;

import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;

import nl.audioware.sagaralogboek.R;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    public static RecyclerView.Adapter mAdapter;
    public static ArrayList<Category> Categories;
    public static ArrayList<Item> Items;
    private RecyclerView.LayoutManager mLayoutManager;
    private NGDataGetter DataGetter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences Settings = getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

        Categories = new ArrayList<>();
        Items = new ArrayList<>();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        DataGetter = new NGDataGetter(this, Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", ""));
        DataGetter.get();

        mAdapter = new MainAdapter(this, Categories);
        mRecyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                DataGetter.get();
            }
        });
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
            case R.id.action_logout:
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
}
