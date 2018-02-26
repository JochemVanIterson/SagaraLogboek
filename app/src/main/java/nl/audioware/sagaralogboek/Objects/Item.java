package nl.audioware.sagaralogboek.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.audioware.sagaralogboek.Activities.MainActivity;

public class Item {
    private int id;
    private String name;
    private Category category;
    private String imageURL;
    private JSONArray data;
    public Item(JSONObject object){
        id = object.optInt("id");
        name = object.optString("name");
        imageURL = object.optString("icon_vector");
        category = findCategory(object.optInt("category_id"));
        String dataString = object.optString("data");
        //try {
        //    data = new JSONArray(dataString);
        //} catch (JSONException e) {
        //    e.printStackTrace();
        //}
    }

    private Category findCategory(int id){
        for (int i = 0; i < MainActivity.Categories.size(); i++) {
            if(MainActivity.Categories.get(i).getId()== id){
                return MainActivity.Categories.get(i);
            }
        }
        return null;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getImageURL() {return imageURL;}
    public JSONArray getData() {return data;}
    public Category getCategory() {return category;}
}

