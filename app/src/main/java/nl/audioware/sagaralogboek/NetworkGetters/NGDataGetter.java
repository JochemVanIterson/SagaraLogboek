package nl.audioware.sagaralogboek.NetworkGetters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.Libraries.Encryption;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.ItemComparator;
import nl.audioware.sagaralogboek.R;

public class NGDataGetter {
    DefaultNetGetter netGetter;
    ProgressDialog dialog;
    SwipeRefreshLayout mSwipeRefreshLayout;

    public NGDataGetter(final Activity activity, final String BaseUrl, final String User, final String iv){
        mSwipeRefreshLayout = activity.findViewById(R.id.swipeRefreshLayout);
        String url = BaseUrl + "Scripts/AndroidGetters.php";

        Map<String,String> params = new HashMap<String, String>();
        params.put("username", User);
        params.put("iv", iv);

        netGetter = new DefaultNetGetter(activity, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                try {
                    //dialog.cancel();
                    if(mSwipeRefreshLayout!=null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    Log.d("DataGetter", response);
                    JSONObject JsonResponse = new JSONObject(response);
                    ConstraintLayout constraintLayout = activity.findViewById(R.id.login_constrain);
                    if(JsonResponse.has("login_error")){
                        Snackbar.make(constraintLayout, JsonResponse.getString("login_error"), Snackbar.LENGTH_LONG).show();
                        Log.d("DataGetter", "failed, message: " + JsonResponse.getString("login_error"));
                    } else{
                        File CacheDataFolder = new File(context.getCacheDir(), "data/");
                        File CacheImagesFolder = new File(context.getCacheDir(), "images/");
                        File CacheAdminFolder = new File(context.getCacheDir(), "admin/");
                        Log.d("CacheImageF", CacheImagesFolder.getAbsolutePath());
                        JSONObject data = JsonResponse.getJSONObject("data");
                        try {
                            new FileHandler().WriteFile(new File(CacheDataFolder, "user_data.json"), data.getJSONObject("user_data").toString());

                            new FileHandler().WriteFile(new File(CacheDataFolder, "users.json"), data.getJSONArray("users").toString());

                            JSONArray categories = data.getJSONArray("categories");
                            categories = FixImageCache(categories, CacheImagesFolder, "category");
                            //MainActivity.Categories.clear();
                            //for (int i = 0; i < categories.length(); i++) {
                            //    Category tmpCat = new Category(categories.getJSONObject(i));
                            //    MainActivity.Categories.add(tmpCat);
                            //}
                            new FileHandler().WriteFile(new File(CacheDataFolder, "categories.json"), categories.toString());

                            JSONArray items = data.getJSONArray("items");
                            items = FixImageCache(items, CacheImagesFolder, "item");
                            //MainActivity.Items.clear();
                            //for (int i = 0; i < items.length(); i++) {
                            //    MainActivity.Items.add(new Item(items.getJSONObject(i)));
                            //}
                            //Collections.sort(MainActivity.Items, new ItemComparator());
                            new FileHandler().WriteFile(new File(CacheDataFolder, "items.json"), items.toString());


                            // TODO: Favorite/pinned elements on top
                            //MainActivity.Card_itms.clear();
                            //for (int i = 0; i < categories.length(); i++) {
                            //    Category tmpCat = new Category(categories.getJSONObject(i));
                            //    MainActivity.Card_itms.add(tmpCat.toCard());
                            //}
                            MainActivity.includeFromFile(context);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        MainActivity.mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", response);
                }
            }

            @Override
            public void get(String tag){
                //dialog.show();
                if(mSwipeRefreshLayout!=null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                super.get(tag);
            }
        };
    }

    //private void WriteFile(File outputFile, String data) throws IOException {
    //    File parent = outputFile.getParentFile();
    //    parent.mkdirs();
    //    OutputStream outputStream;
    //    outputStream = new FileOutputStream(outputFile);
    //    outputStream.write(data.getBytes());
    //    outputStream.close();
    //}

    private JSONArray FixImageCache(JSONArray array, File Folder, String prefix) throws IOException, JSONException {
        // Create image cache and remove image data from category array
        for (int i = 0; i < array.length(); i++) {
            JSONObject jsonObject = array.getJSONObject(i);
            //Log.d("icon_vector", prefix + ": " +jsonObject.getString("icon_vector"));
            if(jsonObject.has("icon_vector")){
                String data = jsonObject.optString("icon_vector", "");
                String dataDecoded = new String(Base64.decode(data, Base64.DEFAULT), "UTF-8");
                if(!dataDecoded.equals("null") && !dataDecoded.equals("") && !dataDecoded.equals(null)){
                    File ImageFile = new File(Folder, prefix + jsonObject.getInt("id") + ".svg");
                    new FileHandler().WriteFile(ImageFile, dataDecoded);
                    array.getJSONObject(i).put("icon_vector", ImageFile.getAbsolutePath());
                }
            }
        }
        return array;
    }

    public void get(){
        netGetter.get("DataGetter");
    }
}
