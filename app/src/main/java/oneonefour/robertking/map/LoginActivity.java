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
                createLobby();
            }
        });
        swipeList = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);

        swipeList.setOnRefreshListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    private void createLobby(){
        final String url = "http://86.149.141.247:8080/MapGame/create_lobby.php?name="+me.getName();
        RequestSingleton.getInstance(this).stringRequest(url);
        //Check whether lobby was created...
        final String findUrl = "http://86.149.141.247:8080/MapGame/get_lobbyID.php?name="+me.getName();
        JSONObject lobbyIDJson = RequestSingleton.getInstance(this).getJSONRequest(url);

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
        startActivity(startGameIntent);//
    }

    @Override
    public void onRefresh() {
        final String url = "http://86.149.141.247:8080/MapGame/get_all_locations.php";
        try {
            addDataToView(RequestSingleton.getInstance(this).getJSONRequest(url));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
