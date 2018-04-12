package nl.audioware.sagaralogboek.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Category {
    private int id;
    private String name;
    private String imageURL;
    private JSONArray data;
    public Category(JSONObject object){
        id = object.optInt("id");
        name = object.optString("name");
        imageURL = object.optString("icon_vector");
        String dataString = object.optString("data");
        try {
            data = new JSONArray(dataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getImageURL() {return imageURL;}
    public JSONArray getData() {return data;}
    public Card_itm toCard(){
        Card_itm card_itm = new Card_itm(id, name, imageURL, "large", this);
        return card_itm;
    }
}
