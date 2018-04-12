package nl.audioware.sagaralogboek.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import nl.audioware.sagaralogboek.Activities.ItemActivity;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.NetworkGetters.NGDataGetter;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.User;
import nl.audioware.sagaralogboek.R;
import nl.audioware.sagaralogboek.Services.SenderService;

public class lockfileDialog extends DialogFragment {
    Item item;
    int itemID;
    int entryID;
    boolean realtime;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        File DataFolder = getActivity().getFilesDir();
        File Lockfile = new File(DataFolder, "lockfile");
        String lockfileDataStr = new FileHandler().readFromFile(Lockfile);
        Log.d("test", lockfileDataStr);
        try {
            JSONObject lockfileData = new JSONObject(lockfileDataStr);
            itemID = lockfileData.getInt("itemID");
            item = new FileHandler().getItem(getActivity(), itemID);
            entryID = lockfileData.getInt("entryID");
            realtime = lockfileData.getBoolean("realtime");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        //Bundle bundle = this.getArguments();
        //if (bundle == null) {
        //    return null;
        //}

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Zender draait al")
                .setMessage("Stop de zender?")
                .setPositiveButton("Stop",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent senderServiceIntent = new Intent(SenderService.ACTION_STOP);
                senderServiceIntent.setClass(getActivity(), SenderService.class);
                senderServiceIntent.putExtra("itemID", itemID);
                senderServiceIntent.putExtra("entryID", entryID);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getActivity().startForegroundService(senderServiceIntent);
                } else {
                    getActivity().startService(senderServiceIntent);
                }

                SharedPreferences Settings = getActivity().getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

                new NGDataGetter(getActivity(), Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", "")).get();
            }
        }).setNegativeButton("Laat aan", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Intent senderServiceIntent = new Intent(SenderService.ACTION_START);
                //senderServiceIntent.setClass(getActivity(), SenderService.class);
                //senderServiceIntent.putExtra("realtime", realtime);
                //senderServiceIntent.putExtra("itemID", itemID);
                //
                //getActivity().startService(senderServiceIntent);

                SharedPreferences Settings = getActivity().getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

                new NGDataGetter(getActivity(), Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", "")).get();
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}