package nl.audioware.sagaralogboek.NetworkGetters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Activities.MainActivity;
import nl.audioware.sagaralogboek.R;
import nl.audioware.sagaralogboek.Libraries.Encryption;

public class NGLoginRequest {
    DefaultNetGetter netGetter;
    ProgressDialog dialog;

    public NGLoginRequest(final Activity activity, SharedPreferences prefs, String BaseUrl, String User, String PWEnc, String iv){
        String url = BaseUrl + "Scripts/Login.php";
        dialog = new ProgressDialog(activity);
        dialog.setMessage("Logging in");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                netGetter.cancelRequest();
            }
        });

        Map<String,String> params = new HashMap<String, String>();
        params.put("user", User);
        params.put("pw", PWEnc);
        params.put("iv", iv);

        netGetter = new DefaultNetGetter(activity, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                //Log.d("Response LoginAttempt", response);
                try {
                    dialog.cancel();
                    Log.d("LoginAttempt", response);
                    JSONObject JsonResponse = new JSONObject(response);
                    ConstraintLayout constraintLayout = activity.findViewById(R.id.login_constrain);
                    if(JsonResponse.has("login")){
                        String loginResponse = JsonResponse.getString("login");
                        if(loginResponse.equals("failed")){
                            Snackbar.make(constraintLayout, JsonResponse.getString("message"), Snackbar.LENGTH_LONG).show();
                            Log.d("LoginAttempt", "failed, message: " + JsonResponse.getString("message"));
                        } else if(loginResponse.equals("success")){
                            Intent intent = new Intent(activity, MainActivity.class);
                            activity.startActivity(intent);
                        } else {
                            Log.d("LoginAttempt", "unknown error: " + response);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("JSONException", response);
                }
            }

            @Override
            public void get(String tag){
                dialog.show();
                super.get(tag);
            }
        };
    }

    public void get(){
        netGetter.get("LoginRequest");
    }
}
