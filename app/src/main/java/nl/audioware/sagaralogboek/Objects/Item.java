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
    private JSONObject data;
    private User sailingUser = null;
    public Item(JSONObject object){
        id = object.optInt("id");
        name = object.optString("name");
        imageURL = object.optString("icon_vector");
        //category = findCategory(object.optInt("category_id"));
        String dataString = object.optString("data");
        try {
            data = new JSONObject(dataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //int sailingUserID = object.optInt("sailing_user");
        //if(sailingUserID!=-1){
        //    sailingUser = findUser(sailingUserID);
        //}
    }
    public Item(JSONObject object, Category category){
        id = object.optInt("id");
        name = object.optString("name");
        imageURL = object.optString("icon_vector");
        this.category = category;
        String dataString = object.optString("data");
        try {
            data = new JSONObject(dataString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //private Category findCategory(int id){
    //    for (int i = 0; i < MainActivity.Categories.size(); i++) {
    //        if(MainActivity.Categories.get(i).getId()== id){
    //            return MainActivity.Categories.get(i);
    //        }
    //    }
    //    return null;
    //}

    //private User findUser(int id){
    //    for (int i = 0; i < MainActivity.Users.size(); i++) {
    //        if(MainActivity.Users.get(i).getId()== id){
    //            return MainActivity.Users.get(i);
    //        }
    //    }
    //    return null;
    //}

    public int getId() {return id;}
    public String getName() {return name;}
    public String getImageURL() {return imageURL;}
    public JSONObject getData() {return data;}
    public Category getCategory() {return category;}
    public User getSailingUser() {return sailingUser;}

    public void setImageURL(String imageURL){this.imageURL = imageURL;}
    public void setSailingUser(User sailingUser){this.sailingUser = sailingUser;}
    public void setCategory(Category category){this.category = category;}

    public Card_itm toCard(){
        Card_itm card_itm = new Card_itm(id, name, imageURL, "small", this);
        return card_itm;
    }


}

