package oneonefour.robertking.map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private JsonObjectRequest hostNameRequest;
    private ProgressDialog dialog;
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
        dialog = new ProgressDialog(this);
        final String url = "http://86.149.141.247:8080/MapGame/get_all_lobbies.php";
        hostNameRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
        RequestSingleton.getInstance(this).addToRequestQueue(hostNameRequest);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLobby);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefresh();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lobby, menu);
        return true;
    }
    private void playGame(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setTitle("Please Wait");
                dialog.setCancelable(false);
                dialog.setIndeterminate(true);
                dialog.setMessage("Waiting for other players to join the game...");
                dialog.show();
            }
        });
        final String url = "http://86.149.141.247:8080/MapGame/update_isReady.php?name="+me.getName();
        RequestSingleton.getInstance(this).stringRequest(url);

        Thread loopThread = new Thread(new Runnable() { //WORKING MULTI-THREADING :-D #efficency #opti
            @Override
            public void run() {
                while(!allReadyToGo(players)){
                    startRefresh();
                }
                loadMap();
            }
        });
        loopThread.start();
    }
    private void loadMap(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
        if(me.getIsHost()){
            final String newUrl = "http://86.149.141.247:8080/MapGame/update_lobby.php?lobbyID="+me.getLobbyId();
            RequestSingleton.getInstance(this).stringRequest(newUrl);
        }
        Intent mapActivityIntent = new Intent(this,MapsActivity.class);
        mapActivityIntent.putExtra("myName",me.getName());
        mapActivityIntent.putExtra("myLobbyId", me.getLobbyId());
        mapActivityIntent.putExtra("myIsHost", me.getIsHost());
        startActivity(mapActivityIntent);
    }
    private boolean allReadyToGo(List<Player> players){
        for(int i =0; i<players.size();i++){
            if(players.get(i).isReady()) continue;
            return false;
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.startPlay){
            playGame();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRefresh(){
        RequestSingleton.getInstance(LobbyActivity.this).addToRequestQueue(hostNameRequest);
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
        while(!request.hasHadResponseDelivered()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        swipeRefreshLayout.setRefreshing(true);
        startRefresh();

    }
    private synchronized void updatePlayerArray(JSONObject response) throws JSONException {
        players = new ArrayList<Player>();
        String[] userNames = new String[response.length() -1];
        for (int i = 0; i < response.length()-1; i++) {
            JSONObject playerObject = response.getJSONObject(Integer.toString(i));
            String userName = playerObject.getString("userName");
            boolean isHost = (userName.equals(hostname));
            Player player = new Player(new LatLng(0,0),userName,me.getLobbyId(),isHost);
            player.setIsReady((playerObject.getString("isReady").equals("1")));
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
    public void onBackPressed() {
        if(me.getIsHost()){
            if(players.size() == 1 ) {
                final String url = "http://86.149.141.247:8080/MapGame/delete_lobby.php?lobbyID=" + me.getLobbyId();
                RequestSingleton.getInstance(this).stringRequest(url);
            }else{
                //Reassaign host
                hostname = players.get(1).getName();
                final String url = "http://86.149.141.247:8080/MapGame/reassign_host.php?name="+me.getName()+"&newname="+hostname;
                RequestSingleton.getInstance(this).stringRequest(url);
            }
        }
        final String altUrl = "http://86.149.141.247:8080/MapGame/delete_location.php?name="+me.getName();
        RequestSingleton.getInstance(this).stringRequest(altUrl);
        super.onBackPressed();
    }
}
