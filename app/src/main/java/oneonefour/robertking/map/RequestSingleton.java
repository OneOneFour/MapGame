package oneonefour.robertking.map;


import android.content.Context;
import android.media.MediaRouter;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    public void onErrorResponse(VolleyError error) {
        if(error == null) return;
        Toast.makeText(singletonContext,"A networking error occured. Please ensure you have a network connection",Toast.LENGTH_SHORT).show();
        Log.e("MapsNetworking",error.getMessage());
    }
}
