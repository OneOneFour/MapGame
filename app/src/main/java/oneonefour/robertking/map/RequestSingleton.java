package oneonefour.robertking.map;


import android.content.Context;
import android.media.MediaRouter;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by Robert on 20/03/2016.
 */
public class RequestSingleton implements Response.ErrorListener {
    private static RequestSingleton instance;
    private RequestQueue queue;
    private static Context singletonContext;
    private JSONObject mostRecentResponse;

    private RequestSingleton(Context context){
        singletonContext = context;
        queue = getRequestQueue();

    }
    public static synchronized RequestSingleton getInstance(Context context){
        if(instance == null){
            instance = new RequestSingleton(context);
        }
        return instance;
    }
    public RequestQueue getRequestQueue() {
        if(queue == null){
            queue = Volley.newRequestQueue(singletonContext.getApplicationContext());
        }
        return queue;
    }
    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
        return;
    }
    public void stringRequest(String url){
        StringRequest request = new StringRequest(Request.Method.GET, url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("MapNetworking", response);
            }
        },this);
        addToRequestQueue(request);
    }
    public JSONObject getJSONRequest(String url){
        mostRecentResponse = null;//Clears the response
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mostRecentResponse = response;
            }
        },this);
        addToRequestQueue(request);
        while(!request.hasHadResponseDelivered()){ // I don't like having to do this - just for the record
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return mostRecentResponse;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error == null) return;
        Log.e("MapsNetworking",error.getMessage());
    }
}
