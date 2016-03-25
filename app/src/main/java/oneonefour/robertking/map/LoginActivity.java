package oneonefour.robertking.map;

import android.accounts.NetworkErrorException;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeList;
    private static Player me;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences saveData = getPreferences(Context.MODE_PRIVATE);
        String name = saveData.getString(getString(R.string.preference_key_name),"userName");
        me = new Player(new LatLng(0,0),name);
        setContentView(R.layout.activity_login);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String url = "http://86.149.141.247:8080/MapGame/create_lobby.php?name="+me.getName();
                RequestSingleton.getInstance(LoginActivity.this).stringRequest(url);
                final String findUrl = "http://86.149.141.247:8080/MapGame/get_lobby_id.php?name="+me.getName();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, findUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        createLobby(response);
                    }
                    },RequestSingleton.getInstance(LoginActivity.this));
                RequestSingleton.getInstance(LoginActivity.this).addToRequestQueue(request);
            }
        });
        swipeList = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        swipeList.setOnRefreshListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    private void createLobby(JSONObject response){
        int lobbyID = me.getLobbyId();
        try {
            lobbyID = Integer.parseInt(response.getJSONObject("0").getString("lobbyID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        me.setLobbyID(lobbyID);
        me.setIsHost(true);
        //
        if(lobbyID != Integer.MAX_VALUE){
            onJoinLobby();
        }
        else{
            Toast.makeText(getApplicationContext(),"Lobby could not be created for some reason",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onDestroy() {
        Log.d("Destroy", "Destroy the player from DB");
        final String url = "http://86.149.141.247:8080/MapGame/delete_location.php?name="+me.getName();
        RequestSingleton.getInstance(this).stringRequest(url);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }
    public String getUsername(){
        return me.getName();
    }
    public  void  setUsername(String name){
        me.setName(name);
    }
    public void setLobbyID(int lobbyID){
        me.setLobbyID(lobbyID);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.playbutton){
            return true;
        }
        if(id == R.id.editPlayer){
            new ChangeNameFragment().show(getFragmentManager(),"Change Name");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void onJoinLobby() {
        Intent startGameIntent = new Intent(this,LobbyActivity.class);//clear backstack and start new task, do we want these for back button behaviour?
        //SEND DATA HERE
        //
        startGameIntent.putExtra("CurrentPlayerName",me.getName());
        startGameIntent.putExtra("lobbyID",me.getLobbyId());
        startGameIntent.putExtra("isHost",me.getIsHost());
        startActivity(startGameIntent);//
    }
    @Override
    public void onRefresh() {
        final String url = "http://86.149.141.247:8080/MapGame/get_all_locations.php";
            JsonObjectRequest requst = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        addDataToView(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, RequestSingleton.getInstance(this));
        RequestSingleton.getInstance(this).addToRequestQueue(requst);
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
