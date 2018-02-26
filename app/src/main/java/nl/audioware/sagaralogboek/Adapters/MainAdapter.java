package nl.audioware.sagaralogboek.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
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

import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVG;
import nl.audioware.sagaralogboek.Libraries.svgandroid.SVGParser;
import nl.audioware.sagaralogboek.Objects.Category;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.R;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private ArrayList<Category> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout parent;
        public TextView mTextView;
        public ImageView imgView;
        public ViewHolder(LinearLayout v) {
            super(v);
            parent = v;
            mTextView = v.findViewById(R.id.card_text);
            imgView = v.findViewById(R.id.imageView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainAdapter(Context context, ArrayList arrayList) {
        FileHandler fileHandler = new FileHandler();
        //String Content = fileHandler.readFromFile(contentFile);
        //JSONArray jsonArray = new JSONArray(Content);
        mDataset = arrayList;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_default, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Category object = mDataset.get(position);
        if(!object.getImageURL().equals("")){
            File imageFile = new File(object.getImageURL());
            try {
                Drawable drawable = svgFileDrawable(imageFile);
                holder.imgView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                holder.imgView.setImageDrawable(drawable);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        holder.mTextView.setText(object.getName());

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