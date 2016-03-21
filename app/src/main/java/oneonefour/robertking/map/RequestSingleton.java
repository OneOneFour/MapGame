package oneonefour.robertking.map;


import android.content.Context;
import android.media.MediaRouter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Robert on 20/03/2016.
 */
public class RequestSingleton {
    private static RequestSingleton instance;
    private RequestQueue queue;
    private static Context singletonContext;

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


}
