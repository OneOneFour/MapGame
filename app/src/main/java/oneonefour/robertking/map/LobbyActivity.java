package oneonefour.robertking.map;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LobbyActivity extends AppCompatActivity {
    private Player me;
    private String hostname;
    private List<Player> players;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        Bundle extras = getIntent().getExtras();
        int lobbyID = extras.getInt("lobbyID");
        String name = extras.getString("CurrentPlayerName");
        boolean isHost = extras.getBoolean("isHost");
        me = new Player(new LatLng(0,0),name);
        me.setIsHost(isHost);
        me.setLobbyID(lobbyID);
        setTitle("Lobby " + lobbyID);
        final String url = "http://86.149.141.247:8080/MapGame/get_all_lobbies.php";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                for(int i = 0;i<response.length()-1;i++){
                    try {
                        if(response.getJSONObject(Integer.toString(i)).getInt("lobbyID") != me.getLobbyId()){
                            continue;
                        }
                        hostname = response.getJSONObject(Integer.toString(i)).getString("HostName");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        },RequestSingleton.getInstance(this));
        RequestSingleton.getInstance(this).addToRequestQueue(request);


        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLobby);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String url = "http://86.149.141.247:8080/MapGame/get_lobby_peeps.php?lobbyID="+ me.getLobbyId();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            updatePlayerArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },RequestSingleton.getInstance(LobbyActivity.this));
                RequestSingleton.getInstance(LobbyActivity.this).addToRequestQueue(request);
            }
        });

    }
    private synchronized void updatePlayerArray(JSONObject response) throws JSONException {
        players = new ArrayList<Player>();
        String[] userNames = new String[response.length() -1];
        for (int i = 0; i < response.length()-1; i++) {
            JSONObject playerObject = response.getJSONObject(Integer.toString(i));
            String userName = playerObject.getString("userName");
            boolean isHost = (userName.equals(hostname));
            Player player = new Player(new LatLng(0,0),userName,me.getLobbyId(),isHost);
            players.add(player);
        }
        for(int i =0; i<userNames.length;i++){
            userNames[i] = players.get(i).getName();
            if(players.get(i).getName().equals(hostname)) userNames[i] = userNames[i].concat(" (host)");
            if(players.get(i).getName().equals(me.getName())) userNames[i] = userNames[i].concat(" (me)");
        }
        ListAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,userNames);
        ListView listView = (ListView) findViewById(R.id.list_playersinLobby);
        listView.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }
    @Override
    protected void onStop() {
        if(me.getIsHost()){
            final String url = "http://86.149.141.247:8080/MapGame/delete_lobby.php?lobbyID="+me.getLobbyId();
            RequestSingleton.getInstance(this).stringRequest(url);
        }
        final String altUrl = "http://86.149.141.247:8080/MapGame/delete_location.php?name="+me.getName();
        RequestSingleton.getInstance(this).stringRequest(altUrl);
        super.onStop();
    }
}
