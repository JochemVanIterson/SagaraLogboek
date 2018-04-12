package nl.audioware.sagaralogboek.Objects;

public class Card_itm {
    private int id;
    private String name;
    private String imageURL;
    private String objectType;
    private Object data;

    public Card_itm(int id, String name, String imageURL, String objectType, Object data){
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.objectType = objectType;
        this.data = data;
    }

    public int getId() {return id;}
    public String getName() {return name;}
    public String getImageURL() {return imageURL;}

    public String getObjectType() {return objectType;}

    public Object getData() {return data;}
}
