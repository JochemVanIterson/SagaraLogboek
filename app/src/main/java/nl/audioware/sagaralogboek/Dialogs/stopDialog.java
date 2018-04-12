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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import nl.audioware.sagaralogboek.Activities.ItemActivity;
import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.Libraries.FileHandler;
import nl.audioware.sagaralogboek.NetworkGetters.NGDataGetter;
import nl.audioware.sagaralogboek.Objects.Item;
import nl.audioware.sagaralogboek.Objects.User;
import nl.audioware.sagaralogboek.R;
import nl.audioware.sagaralogboek.Services.SenderService;

public class stopDialog extends DialogFragment {
    Item item;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        if (bundle == null) {
            return null;
        }
        final int itemID = bundle.getInt("itemID", -1);
        item = new FileHandler().getItem(getActivity(), itemID);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_stop, null);

        TextView DialogTitle = view.findViewById(R.id.dialog_title);
        DialogTitle.setText(item.getCategory().getName() + " > " + item.getName());


        if(!item.getData().has("diesel_peil")){
            EditText EditDiesel = view.findViewById(R.id.editText_diesel);
            EditDiesel.setVisibility(View.GONE);
        }

        Switch SwitchKapot = view.findViewById(R.id.switch_kapot);
        final EditText EditKapot = view.findViewById(R.id.editText_kapot);
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
                .setPositiveButton("Stop met varen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent senderServiceIntent = new Intent(SenderService.ACTION_STOP);
                        senderServiceIntent.setClass(getActivity(), SenderService.class);
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

                        ((ItemActivity)getActivity()).isSailing = false;
                        fab.setImageResource(R.drawable.ic_directions_run_black_24dp);
                        ((ItemActivity)getActivity()).item.setSailingUser(null);
                        cv_user_sailing.setVisibility(View.GONE);

                        new NGDataGetter(getActivity(), Settings.getString("BaseUrl", ""), Settings.getString("User", ""), Settings.getString("iv", "")).get();

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}