package oneonefour.robertking.map;

import android.accounts.NetworkErrorException;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Debug;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
    private ProgressDialog dialog;
    private int mInterval = 10000;
    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences saveData = getPreferences(Context.MODE_PRIVATE);
        String name = saveData.getString(getString(R.string.preference_key_name),"userName");
        me = new Player(new LatLng(0,0),name);
        setContentView(R.layout.activity_login);
        dialog = new ProgressDialog(LoginActivity.this);
        dialog.setTitle("Please Wait");
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        mHandler = new Handler();
        startRepeatingTask();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
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
        ListView view = (ListView) findViewById(R.id.list_players);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView v = (TextView) view;
                Log.d("TextView", v.getText().toString());

                dialog.show();
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://86.149.141.247:8080/MapGame/get_lobby_id.php?name=" + v.getText().toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            prepareToEnterLobby(response.getJSONObject("0").getInt("lobbyID"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },RequestSingleton.getInstance(LoginActivity.this));
                RequestSingleton.getInstance(LoginActivity.this).addToRequestQueue(request);





            }
        });
    }
    Runnable internetStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                final ConnectivityManager conmGr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = conmGr.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()){

                }else {
                    Toast toast = Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG);
                    toast.show();
                }

            } finally {
                mHandler.postDelayed(internetStatusChecker, mInterval);
            }

        }
    };
    void startRepeatingTask(){
        internetStatusChecker.run();
    }
    void stopRepeatingTask(){
        mHandler.removeCallbacks(internetStatusChecker);
    }
    private void prepareToEnterLobby(final int lobbyID){
        final String url = "http://86.149.141.247:8080/MapGame/create_location.php?name="+me.getName()+ "&latitude="+me.getCurrentLocation().latitude + "&longitude="+me.getCurrentLocation().longitude +"&lobbyID="+ lobbyID;
        RequestSingleton.getInstance(LoginActivity.this).addToRequestQueue(new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                me.setIsHost(false);
                me.setLobbyID(lobbyID);
                dialog.dismiss();
                onJoinLobby();
            }
        }, RequestSingleton.getInstance(LoginActivity.this)));
    }
    @Override
    protected void onResume() {
        super.onResume();
        me.setLobbyID(Integer.MAX_VALUE);
        me.setIsHost(false);
    }
    private void createLobby(JSONObject response){
        int lobbyID = me.getLobbyId();
        try {
            lobbyID = Integer.parseInt(response.getJSONObject("0").getString("lobbyID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String url = "http://86.149.141.247:8080/MapGame/create_location.php?name="+me.getName() + "&latitude="+me.getCurrentLocation().latitude + "&longitude="+me.getCurrentLocation().longitude+"&lobbyID="+lobbyID;
        RequestSingleton.getInstance(this).stringRequest(url);
        me.setLobbyID(lobbyID);
        me.setIsHost(true);
        Log.d("LoginActivity", Integer.toString(lobbyID));
        //
        dialog.dismiss();
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
        final String url = "http://86.149.141.247:8080/MapGame/get_all_lobbies.php";
            JsonObjectRequest requst = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        addDataToView(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        swipeList.setRefreshing(false);
                    }
                }
            }, RequestSingleton.getInstance(this));
        RequestSingleton.getInstance(this).addToRequestQueue(requst);
    }
    private void addDataToView(JSONObject response) throws JSONException {
        String[] usernames = new String[response.length() - 1];
        for(int i=0; i < usernames.length; i++){
            JSONObject player = response.getJSONObject(Integer.toString(i));
            usernames[i] = player.getString("HostName");
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
