package nl.audioware.sagaralogboek.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import nl.audioware.sagaralogboek.Activities.ItemActivity;
import nl.audioware.sagaralogboek.Objects.DbEntry;
import nl.audioware.sagaralogboek.R;

public class mapAdapter extends RecyclerView.Adapter<mapAdapter.ViewHolder> {
    private ArrayList<DbEntry> mDataset;
    ItemActivity context;
    SimpleDateFormat simpleDate =  new SimpleDateFormat("EEEE dd MMMM HH:mm:ss");

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout parent;
        public TextView tv_title, tv_user;
        public LinearLayout cardLin;
        public ViewHolder(LinearLayout v) {
            super(v);
            parent = v;
            tv_title = v.findViewById(R.id.card_title);
            tv_user = v.findViewById(R.id.card_user);
            cardLin = v.findViewById(R.id.card_lin);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public mapAdapter(ItemActivity context, ArrayList arrayList) {
        mDataset = arrayList;
        Log.d("MapEntries", String.valueOf(mDataset.size()));
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public mapAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_map, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        final DbEntry object = mDataset.get(position);
        holder.tv_title.setText(simpleDate.format(object.getDatetime_start()));
        holder.tv_user.setText(object.getUser().getFirstName() + " "+ object.getUser().getLastName());
        if(object.selected) holder.cardLin.setBackgroundColor(Color.LTGRAY);
        else holder.cardLin.setBackgroundColor(Color.TRANSPARENT);
        holder.cardLin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i <mDataset.size() ; i++) {
                    mDataset.get(i).setSelected(false);
                }
                mDataset.get(position).setSelected(true);
                context.drawLineMap(mDataset.get(position).getLatLngs(), false);
                notifyDataSetChanged();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}