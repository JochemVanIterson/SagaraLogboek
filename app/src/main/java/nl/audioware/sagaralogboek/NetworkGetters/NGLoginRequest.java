package nl.audioware.sagaralogboek.NetworkGetters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NGLoginRequest {
    DefaultNetGetter netGetter;
    ProgressDialog dialog;

    public NGLoginRequest(final Activity activity, final SharedPreferences prefs, String BaseUrl, String User, String PWEnc){
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

        netGetter = new DefaultNetGetter(activity, url, params){
            @Override
            public void ActionDone(String response, final Context context){
                //Log.d("Response LoginAttempt", response);
                try {
                dialog.cancel();
                    JSONObject JsonResponse = new JSONObject(response);
                    if(JsonResponse.has("login")){
                        String loginResponse = JsonResponse.getString("login");
                        if(loginResponse.equals("failed")){
                            Log.d("LoginAttempt", "failed, message: " + JsonResponse.getString("message"));
                        } else if(loginResponse.equals("success")){
                            Log.d("LoginAttempt", "success");
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
