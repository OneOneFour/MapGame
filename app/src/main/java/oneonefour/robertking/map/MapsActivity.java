package oneonefour.robertking.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Players;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;
    private Location currentPhoneLocation;
    private GoogleApiClient apiClient;
    private HashMap<String,Marker> playToMarker;
    private String userName;
    private HashMap<String, Player> players;
    final int PERMISSION_REQUEST_OKAY = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle extras = getIntent().getExtras();
        if (extras.getString("CurrentPlayerName") != null) {
            userName = (String) extras.getString("CurrentPlayerName");
        } else {
            userName = "TEMP";
        }
        apiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).addApi(AppIndex.API).build();
        players = new HashMap<String, Player>();
        playToMarker = new HashMap<String,Marker>();
    }

    @Override
    protected void onStart() {
        RequestSingleton.getInstance(this).getRequestQueue().start();
        apiClient.connect();
        super.onStart();
        Timer updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() { //update function
                //pull locations....
                    RequestSingleton.getInstance(MapsActivity.this).addToRequestQueue(new JsonObjectRequest(Request.Method.GET, "http://86.149.141.247:8080/MapGame/get_all_locations.php", null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                updatePlayerArray(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },RequestSingleton.getInstance(MapsActivity.this)));
                //push new location
                if(currentPhoneLocation == null)return; //don't send location if it is null duh
                final String url = "http://86.149.141.247:8080/MapGame/update_location.php?name=" + userName + "&latitude=" + currentPhoneLocation.getLatitude() + "&longitude=" + currentPhoneLocation.getLongitude();
                RequestSingleton.getInstance(MapsActivity.this).stringRequest(url);
            }
        }, 1000, 30 * 1000);//TODO Make the run every second.
    }

    private synchronized void updatePlayerArray(JSONObject response) throws JSONException {
        for (int i = 0; i < response.length()-1; i++) {
            JSONObject playerObject = response.getJSONObject(Integer.toString(i));
            String userName = playerObject.getString("userName");
            if (userName.equals(this.userName)) continue;
            Double lat = Double.parseDouble(playerObject.getString("latitude"));
            Double longd = Double.parseDouble(playerObject.getString("longitude"));
            LatLng location = new LatLng(lat, longd);
            if (players.containsKey(userName) && players.get(userName) != null) {
                players.get(userName).updateLocation(location);
            } else {
                players.put(userName, new Player(location, userName));
            }
        }
        updateMap();
    }

    @Override
    protected void onStop() {
        RequestSingleton.getInstance(this).getRequestQueue().stop();
        //remove yourself
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        apiClient.disconnect();
        super.onStop();
    }

    public void setLocation(Location location) {
        this.currentPhoneLocation = location;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;//setup map
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_OKAY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onConnected(null);
                } else {
                    Toast insultToast = Toast.makeText(getApplicationContext(), "Enable the permission you penis", Toast.LENGTH_SHORT);
                    insultToast.show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_OKAY);
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.currentPhoneLocation = location;
    }

    public synchronized void updateMap() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //YES
                    return;
                }
                mMap.setMyLocationEnabled(true);
                Iterator it = players.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry e = (Map.Entry)it.next();
                    Player obj = (Player) e.getValue();
                    Log.d("MApACt",obj.getCurrentLocation().toString());
                    if(playToMarker.get(obj.getName()) == null) {
                        playToMarker.put(obj.getName(), mMap.addMarker(new MarkerOptions().position(obj.getCurrentLocation()).title(obj.getName() + " is Here")));
                    }else{
                        playToMarker.get(obj.getName()).setPosition(obj.getCurrentLocation());
                    }
                }
            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest request = new LocationRequest();
        request.setInterval(5000);
        request.setFastestInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_OKAY);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
