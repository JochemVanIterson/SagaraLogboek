package nl.audioware.sagaralogboek.NetworkGetters;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import nl.audioware.sagaralogboek.Libraries.MySingleton;

public class DefaultNetGetter {
    private StringRequest strRequest;
    private Context context;

    public DefaultNetGetter(final Context context, String url, final Map<String,String> params){
        this.context = context;

        strRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d("Response DefaultNGetter", response);
                ActionDone(response, context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ActionError(context, error);
                Log.d("JsonArrayRequest", error.toString());
            }
        }){
            @Override
            protected Map<String,String> getParams(){
                return params;
            }
        };
        strRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 10000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 6;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                Log.d("VolleyError", error.getMessage());
            }
        });
    }

    public void get(String tag){
        strRequest.setTag(tag);
        MySingleton.getInstance(context).addToRequestQueue(strRequest);
    }

    public void ActionError(Context context, VolleyError error){

    }

    public void ActionDone(String response, Context context){

    }

    public void cancelRequest(){
        MySingleton.getInstance(context).removeFromRequestQueue(strRequest);
    }
}