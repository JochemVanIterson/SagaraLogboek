package nl.audioware.sagaralogboek.Dialogs;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import nl.audioware.sagaralogboek.Activities.ItemActivity;
import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.NetworkGetters.NGDataGetter;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.User;
import nl.audioware.sagaralogboek.R;
import nl.audioware.sagaralogboek.Services.SenderService;

public class startDialog extends DialogFragment {
    Item item;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle == null) {
            return null;
        }
        final int itemID = bundle.getInt("itemID", -1);
        item = new FileHandler().getItem(getActivity(), itemID);
        //for (int i = 0; i <MainActivity.Items.size() ; i++) {
        //    Item tmpItem = MainActivity.Items.get(i);
        //    if(tmpItem.getId()==itemID){
        //        item = tmpItem;
        //        break;
        //    }
        //}

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_start, null);

        TextView DialogTitle = view.findViewById(R.id.dialog_title);
        DialogTitle.setText(item.getCategory().getName() + " > " + item.getName());


        if(!item.getData().has("diesel_peil")){
            EditText EditDiesel = view.findViewById(R.id.editText_diesel);
            EditDiesel.setVisibility(View.GONE);
        }

        Switch SwitchKapot = view.findViewById(R.id.switch_kapot);
        //final Switch SwitchLocation = view.findViewById(R.id.switch_location);
        final EditText EditKapot = view.findViewById(R.id.editText_kapot);
        final EditText EditDiesel = view.findViewById(R.id.editText_diesel);
        EditKapot.setVisibility(View.GONE);
        SwitchKapot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    EditKapot.setVisibility(View.VISIBLE);
                } else {
                    EditKapot.setVisibility(View.GONE);
                }
            }
        });

        builder.setView(view)
                .setPositiveButton("Start met varen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent senderServiceIntent = new Intent(SenderService.ACTION_START);
                        senderServiceIntent.setClass(getActivity(), SenderService.class);

                        JSONObject data_start = new JSONObject();
                        try {
                            data_start.put("kapot", EditKapot.getText().toString());
                            data_start.put("diesel_peil", EditDiesel.getText().toString());
                            senderServiceIntent.putExtra("data_start", data_start.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        //senderServiceIntent.putExtra("realtime", SwitchLocation.isChecked());
                        senderServiceIntent.putExtra("itemID", itemID);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getActivity().startForegroundService(senderServiceIntent);
                        } else {
                            getActivity().startService(senderServiceIntent);
                        }

                        CardView cv_user_sailing = getActivity().findViewById(R.id.cv_user_sailing);
                        TextView tvUserSailing = getActivity().findViewById(R.id.tv_user_sailing);
                        FloatingActionButton fab = getActivity().findViewById(R.id.fab);

                        SharedPreferences Settings = getActivity().getSharedPreferences(getString(R.string.prefs_Main), Context.MODE_PRIVATE);

                        ((ItemActivity)getActivity()).isSailing = true;
                        fab.setImageResource(R.drawable.ic_anchor);
                        User SailingUser = new FileHandler().getUser(getActivity(), Settings.getString("User", ""));
                        ((ItemActivity)getActivity()).item.setSailingUser(SailingUser);

                        String isSailingText = "Sailing: " + SailingUser.getFirstName() + " " + SailingUser.getLastName();
                        tvUserSailing.setText(isSailingText);
                        cv_user_sailing.setVisibility(View.VISIBLE);

                        new NGDataGetter(getActivity(), Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", "")).get();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}