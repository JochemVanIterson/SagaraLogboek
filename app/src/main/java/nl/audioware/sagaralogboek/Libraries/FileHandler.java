package nl.audioware.sagaralogboek.Libraries;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.User;

public class FileHandler {
    public final String readFromFile(File file) {

        String contents = "";

        try {
            int length = (int) file.length();

            byte[] bytes = new byte[length];

            FileInputStream in = new FileInputStream(file);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            contents = new String(bytes);
        }
        catch (FileNotFoundException e) {
            Log.e("FileHandler", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileHandler", "Can not read file: " + e.toString());
        }

        return contents;
    }
    public final void WriteFile(File outputFile, String data) throws IOException {
        File parent = outputFile.getParentFile();
        parent.mkdirs();
        OutputStream outputStream;
        outputStream = new FileOutputStream(outputFile);
        outputStream.write(data.getBytes());
        outputStream.close();
    }

    public final Item json2item(Context context, JSONObject object){
        Item item = new Item(object);
        Category category = getCategory(context, object.optInt("category_id"));
        item.setCategory(category);
        if(object.optInt("sailing_user", -1)!=-1){
            User sailingUser = getUser(context, object.optInt("sailing_user", -1));
            item.setSailingUser(sailingUser);
        }

        if(item.getImageURL().equals("")){
            item.setImageURL(category.getImageURL());
        }
        return item;
    }
    public final Item getItem(Context context, int ItemID){
        //int ItemID = getIntent().getIntExtra("ItemID", -1);
        File CacheDataFolder = new File(context.getCacheDir(), "data/");
        String itemsFileData = readFromFile(new File(CacheDataFolder, "items.json"));
        try {
            JSONArray itemsArray = new JSONArray(itemsFileData);
            for (int i = 0; i <itemsArray.length() ; i++) {
                JSONObject tmpObject = itemsArray.getJSONObject(i);
                if(tmpObject.optInt("id", -1)==ItemID){
                    return json2item(context, tmpObject);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final Category getCategory(Context context, int CategoryID){
        File CacheDataFolder = new File(context.getCacheDir(), "data/");
        String categoriesFileData = readFromFile(new File(CacheDataFolder, "categories.json"));
        try {
            JSONArray categoriesArray = new JSONArray(categoriesFileData);
            for (int i = 0; i <categoriesArray.length() ; i++) {
                JSONObject tmpObject = categoriesArray.getJSONObject(i);
                if(tmpObject.optInt("id", -1)==CategoryID){
                    Category category = new Category(tmpObject);
                    return category;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final User getUser(Context context, int UserID){
        File CacheDataFolder = new File(context.getCacheDir(), "data/");
        String usersFileData = readFromFile(new File(CacheDataFolder, "users.json"));
        try {
            JSONArray usersArray = new JSONArray(usersFileData);
            for (int i = 0; i <usersArray.length() ; i++) {
                JSONObject tmpObject = usersArray.getJSONObject(i);
                if(tmpObject.optInt("id", -1)==UserID){
                    User user = new User(tmpObject);
                    return user;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public final User getUser(Context context, String UserName){
        File CacheDataFolder = new File(context.getCacheDir(), "data/");
        String usersFileData = readFromFile(new File(CacheDataFolder, "users.json"));
        try {
            JSONArray usersArray = new JSONArray(usersFileData);
            for (int i = 0; i <usersArray.length() ; i++) {
                JSONObject tmpObject = usersArray.getJSONObject(i);
                if(tmpObject.optString("username").equals(UserName)){
                    User user = new User(tmpObject);
                    return user;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public final boolean WriteLockFile(Context context, JSONObject jsonObject){
        if(LockFileExists(context)){
            return false;
        }
        File DataFolder = context.getFilesDir();
        File Lockfile = new File(DataFolder, "lockfile");
        try {
            WriteFile(Lockfile, jsonObject.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public final boolean LockFileExists(Context context){
        File DataFolder = context.getFilesDir();
        File Lockfile = new File(DataFolder, "lockfile");
        return Lockfile.exists();
    }
    public final boolean ReleaseLockFile(Context context){
        if(!LockFileExists(context)){
            return false;
        }
        File DataFolder = context.getFilesDir();
        File Lockfile = new File(DataFolder, "lockfile");
        return Lockfile.delete();
    }
}
