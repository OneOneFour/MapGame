package oneonefour.robertking.map;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeList;
    private Player me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = new Player(new LatLng(0,0),"userName");
        setContentView(R.layout.activity_login);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Add a addPlayer activity that adds players to list view that they can use to play against.
            }
        });
        swipeList = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        swipeList.setOnRefreshListener(this);
    }
    //TODO move this to a static parsing class

    @Override
    protected void onStart() {
        super.onStart();
        RequestSingleton.getInstance(this).getRequestQueue().start();
        final String url = "http://86.149.141.247:8080/MapGame/create_location.php?name=" +  me.getName() + "&latitude=" + me.getCurrentLocation().latitude +"&longitude=" + me.getCurrentLocation().longitude;
        StringRequest addPlayer = new StringRequest(Request.Method.GET,url,new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("LogAct",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LogAct", error.getMessage() + "JEU");
            }
        });
        RequestSingleton.getInstance(this).addToRequestQueue(addPlayer);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.playbutton){
            onPlayGame();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void onPlayGame() {
        Intent startGameIntent = new Intent(this,MapsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);//clear backstack and start new task, do we want these for back button behaviour?
        //SEND DATA HERE
        //
        startGameIntent.putExtra("CurrentPlayerName",me.getName());
        startActivity(startGameIntent);//
    }

    @Override
    public void onRefresh() {

        JsonObjectRequest players = new JsonObjectRequest(Request.Method.GET, "http://86.149.141.247:8080/MapGame/get_all_locations.php", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    addDataToView(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("LoginActivity","Something went wrong, along the lines of " + error.getMessage());
                swipeList.setRefreshing(false);
            }
        });
        RequestSingleton.getInstance(this).addToRequestQueue(players);


    }

    private void addDataToView(JSONObject response) throws JSONException {
        String[] usernames = new String[response.length() - 2];
        for(int i=0; i < usernames.length; i++){
            JSONObject player = response.getJSONObject(Integer.toString(i));
            if(player.getString("userName").equals(me.getName())) continue;
            usernames[i] = player.getString("userName");
        }

        ListAdapter adaptList = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,usernames);
        ListView listView = (ListView) findViewById(R.id.list_players);
        listView.setAdapter(adaptList);
        TextView noonearound = (TextView) findViewById(R.id.empty_list_text);
        if(usernames.length <1){

            noonearound.setVisibility(View.VISIBLE);
        }else{
            noonearound.setVisibility(View.GONE);
        }
        swipeList.setRefreshing(false);
    }
}
