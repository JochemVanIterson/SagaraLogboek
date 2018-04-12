package nl.audioware.sagaralogboek.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import nl.audioware.sagaralogboek.Activities.CategoryActivity;
import nl.audioware.sagaralogboek.Activities.ItemActivity;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVG;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVGParser;
import nl.audioware.sagaralogboek.Objects.Card_itm;
import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.R;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Card_itm> mDataset;
    Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolderLarge extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout parent;
        public TextView mTextView;
        public ImageView imgView;
        public CardView cardView;
        public ViewHolderLarge(LinearLayout v) {
            super(v);
            parent = v;
            mTextView = v.findViewById(R.id.card_text);
            imgView = v.findViewById(R.id.imageView);
            cardView = v.findViewById(R.id.card_view);
        }
    }
    public static class ViewHolderSmall extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout parent;
        public TextView mTextView;
        public ImageView imgView;
        public CardView cardView;
        public ViewHolderSmall(LinearLayout v) {
            super(v);
            parent = v;
            mTextView = v.findViewById(R.id.card_text);
            imgView = v.findViewById(R.id.imageView);
            cardView = v.findViewById(R.id.card_view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        if(mDataset.get(position).getObjectType().equals("large"))return 0;
        else if(mDataset.get(position).getObjectType().equals("small"))return 1;

        else return 0;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainAdapter(Context context, ArrayList arrayList) {
        FileHandler fileHandler = new FileHandler();
        //String Content = fileHandler.readFromFile(contentFile);
        //JSONArray jsonArray = new JSONArray(Content);
        mDataset = arrayList;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                LinearLayout v_large = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_default, parent, false);
                // set the view's size, margins, paddings and layout parameters
                ViewHolderLarge vh_large = new ViewHolderLarge(v_large);
                return vh_large;
            case 1:
                LinearLayout v_small = (LinearLayout) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_small, parent, false);
                // set the view's size, margins, paddings and layout parameters
                ViewHolderSmall vh_small = new ViewHolderSmall(v_small);
                return vh_small;
        }
        LinearLayout v_large = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_default, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolderLarge vh_large = new ViewHolderLarge(v_large);
        return vh_large;
        // create a new view

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Card_itm object = mDataset.get(position);
        switch (holder.getItemViewType()){
            case 0:
                ViewHolderLarge holderViewL = (ViewHolderLarge)holder;
                if(!object.getImageURL().equals("")){
                    File imageFile = new File(object.getImageURL());
                    try {
                        Drawable drawable = svgFileDrawable(imageFile);
                        holderViewL.imgView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        holderViewL.imgView.setImageDrawable(drawable);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                holderViewL.mTextView.setText(object.getName());
                holderViewL.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Card Sel", object.getName());
                        Intent intent = new Intent(context, CategoryActivity.class);
                        intent.putExtra("CategoryID", object.getId());
                        context.startActivity(intent);
                    }
                });
                break;
            case 1:
                ViewHolderSmall holderViewS = (ViewHolderSmall)holder;
                if(!object.getImageURL().equals("")){
                    File imageFile = new File(object.getImageURL());
                    try {
                        Drawable drawable = svgFileDrawable(imageFile);
                        holderViewS.imgView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                        holderViewS.imgView.setImageDrawable(drawable);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                holderViewS.mTextView.setText(object.getName());
                holderViewS.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("Card Sel", object.getName());
                        Intent intent = new Intent(context, ItemActivity.class);
                        intent.putExtra("ItemID", object.getId());
                        context.startActivity(intent);
                    }
                });
                break;
        }
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public Drawable svgFileDrawable(File dataFile) throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(dataFile);
        Log.d("dataFile", dataFile.getAbsolutePath());
        SVG svg = SVGParser.getSVGFromInputStream(inputStream);
        return svg.createPictureDrawable();
    }
}