package oneonefour.robertking.map;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;
    private ProgressDialog progDia;
    private Location currentPhoneLocation;
    private Location currentFlagLocation;
    private String flagPlayerName;
    private GoogleApiClient apiClient;
    private HashMap<String,Marker> playToMarker;
    private String userName;
    private Player me;
    private NotificationManager manager;
    private Location baseLocation;
    private double lookupLat;
    private double lookupLong;
    private HashMap<String, Player> players;
    final int PERMISSION_REQUEST_OKAY = 43;
    private Location tmpFlagLoca;
    private boolean locationGo =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Bundle data = getIntent().getExtras();
        String userName = data.getString("myName");
        int lobbyID = data.getInt("myLobbyId");
        boolean isHost = data.getBoolean("myIsHost");
        me = new Player(new LatLng(0,0),userName,lobbyID,isHost);
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
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        apiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).addApi(AppIndex.API).build();
        players = new HashMap<String, Player>();
        playToMarker = new HashMap<String,Marker>();
        progDia = new ProgressDialog(this);
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

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "http://86.149.141.247:8080/MapGame/get_lobby_peeps.php?lobbyID=" + me.getLobbyId(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            updatePlayerArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, RequestSingleton.getInstance(MapsActivity.this));
                RequestSingleton.getInstance(MapsActivity.this).addToRequestQueue(request);
                //push new location
                if (currentPhoneLocation == null) return; //don't send location if it is null duh
                final String url = "http://86.149.141.247:8080/MapGame/update_location.php?name=" + me.getName() + "&latitude=" + currentPhoneLocation.getLatitude() + "&longitude=" + currentPhoneLocation.getLongitude();
                StringRequest sr = new StringRequest(Request.Method.GET, url, RequestSingleton.getInstance(MapsActivity.this), RequestSingleton.getInstance(MapsActivity.this));
                RequestSingleton.getInstance(MapsActivity.this).addToRequestQueue(sr);

                flagPlayerName = getFlagPlayer(players);
                if (flagPlayerName != null) {
                    int id = 1;
                    //Set notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this);
                    builder.setSmallIcon(R.drawable.ic_flag_white_24dp);
                    builder.setContentTitle("FlagStatus");
                    if (flagPlayerName.equals(me.getName())) {
                        builder.setContentText("You have the flag");
                    } else {
                        builder.setContentText(flagPlayerName + " has the flag.");
                    }
                    manager.notify(id,builder.build());
                }
                //check if flag location is null, if so spawn a flag
                //time for more threading.

                if (MapsActivity.this.currentFlagLocation == null) {
                    MapsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MapsActivity.this.progDia.setTitle("Please Wait");
                            MapsActivity.this.progDia.setMessage("Starting game");
                            MapsActivity.this.progDia.setCancelable(false);
                            MapsActivity.this.progDia.setIndeterminate(true);
                            MapsActivity.this.progDia.show();
                        }
                    });
                    while (!sr.hasHadResponseDelivered()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    JsonObjectRequest otherequest = new JsonObjectRequest(Request.Method.GET, "http://86.149.141.247:8080/MapGame/get_lobby_peeps.php?lobbyID=" + me.getLobbyId(), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                updatePlayerArray(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, RequestSingleton.getInstance(MapsActivity.this));
                    RequestSingleton.getInstance(MapsActivity.this).addToRequestQueue(otherequest);
                    while (!otherequest.hasHadResponseDelivered()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    baseLocation = new Location("base location");
                    baseLocation.setLatitude(me.getPastLocations().get(0).latitude);
                    baseLocation.setLongitude(me.getPastLocations().get(0).longitude);

                    if (MapsActivity.this.me.getIsHost()) {
                        double[] lats = new double[players.size()];
                        double[] longs = new double[players.size()];
                        Iterator it = players.entrySet().iterator();
                        int i = 0;
                        while (it.hasNext()) {
                            Player p = (Player) ((Map.Entry) it.next()).getValue();
                            lats[i] = p.getCurrentLocation().latitude;
                            longs[i] = p.getCurrentLocation().longitude;
                            i++;
                        }

                        double avLats = getAverage(lats);
                        double avlongs = getAverage(longs);
                        Random numb = new Random();
                        //Perform Lookup
                        avLats += randomDouble(numb,-1/70.0,1/70.0); //allows flag to jitter by up to a mile
                        avlongs += randomDouble(numb,-1/70.0,1/70.0);
                        lookupLat = avLats;
                        lookupLong = avlongs;
                        currentFlagLocation = getRoadLocation();
                        Log.d("MapsAct", "LONG:" + avlongs + " LAT:" + avLats);
                        final String newURL = "http://86.149.141.247:8080/MapGame/create_location.php?name=Flag" + me.getLobbyId() + "&latitude=" + currentFlagLocation.getLatitude() + "&longitude=" + currentFlagLocation.getLongitude() + "&lobbyID=" + me.getLobbyId();
                        RequestSingleton.getInstance(MapsActivity.this).stringRequest(newURL);

                    } else {
                        //wait for flag to return positive
                        while (currentFlagLocation == null) {
                            RequestSingleton.getInstance(MapsActivity.this).addToRequestQueue(request);
                            while (!request.hasHadResponseDelivered()) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    MapsActivity.this.progDia.dismiss();
                }
                float distanceinMeters = currentPhoneLocation.distanceTo(currentFlagLocation);
                if (distanceinMeters <= 5 && (!flagPlayerName.equals(me.getName()))) {
                    //Rob does his volley magic and updates the hasFlag boolean
                    if (flagPlayerName == null) {
                        flagPlayerName = me.getName();
                        me.setHasFlag(true);
                        final String urlYes = "http://86.149.141.247:8080/MapGame/update_hasFlag_true.php?name="+me.getName();
                        RequestSingleton.getInstance(MapsActivity.this).stringRequest(urlYes);
                    }else{
                        Player them = players.get(flagPlayerName);
                        them.setHasFlag(false);
                        me.setHasFlag(true);
                        flagPlayerName = me.getName();
                        final String urlYes2 = "http://86.149.141.247:8080/MapGame/update_hasFlag_true.php?name="+me.getName();
                        final String urlNo2 = "http://86.149.141.247:8080/MapGame/update_hasFlag_false.php?name="+them.getName();
                        RequestSingleton.getInstance(MapsActivity.this).stringRequest(urlYes2);
                        RequestSingleton.getInstance(MapsActivity.this).stringRequest(urlNo2);
                    }
                }
                float distanceToBase = currentPhoneLocation.distanceTo(baseLocation);
                if(distanceToBase <= 5 && me.isHasFlag()){
                    //YOU WIN
                    Toast.makeText(MapsActivity.this,"YOU WIN",Toast.LENGTH_LONG).show();
                }
            }
            private synchronized double getAverage(double[] array) {
                if (array.length == 0) return 0;
                double sum = 0;
                for (double num : array) {
                    sum += num;
                }
                return sum / array.length;
            }

            private double randomDouble(Random random, double max, double min) {
                double range = max - min;
                return min + range * random.nextDouble();
            }
        }, 1000, 1000);
    }
    private synchronized void updatePlayerArray(JSONObject response) throws JSONException {
        for (int i = 0; i < response.length()-1; i++) {
            JSONObject playerObject = response.getJSONObject(Integer.toString(i));
            String userName = playerObject.getString("userName");
            if (userName.equals(this.userName)) continue;

            Double lat = Double.parseDouble(playerObject.getString("Latitude"));
            Double longd = Double.parseDouble(playerObject.getString("Longitude"));

            LatLng location = new LatLng(lat, longd);
            boolean hasFlag = (playerObject.getString("hasFlag").equals("1"));
            if(hasFlag){
                currentFlagLocation.setLatitude(lat);
                currentFlagLocation.setLongitude(longd);
            }
            if(userName.equals("Flag" + me.getLobbyId())){
                if(currentFlagLocation == null) {
                    currentFlagLocation = new Location("Flag" + me.getLobbyId());
                }
                currentFlagLocation.setLongitude(longd);
                currentFlagLocation.setLatitude(lat);

            }
            if (players.containsKey(userName) && players.get(userName) != null) {
                players.get(userName).updateLocation(location);
                players.get(userName).setHasFlag(hasFlag);
            } else {
                Player p = new Player(location,userName);
                p.setLobbyID(me.getLobbyId());
                p.setHasFlag(hasFlag);
                players.put(userName,p);
            }
        }
        updateMap();
    }
    @Override
    protected void onStop() {
        //remove yourself
        LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
        apiClient.disconnect();
        super.onStop();
    }
    private String getFlagPlayer(HashMap<String,Player> map){
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()){
            Player p = (Player)  ((Map.Entry) it.next()).getValue();
            if(p.isHasFlag()) return p.getName();

        }
        return null;
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

    @Override
    public void onDestroy() {
        if(players.size() == 1){
            final String url = "http://86.149.141.247:8080/MapGame/delete_location.php?name=Flag"+me.getLobbyId();
            RequestSingleton.getInstance(this).stringRequest(url);
        }
        final String otherURL = "http://86.149.141.247:8080/MapGame/update_isReady.php?name="+me.getName();
        RequestSingleton.getInstance(this).stringRequest(otherURL);

        super.onDestroy();
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
                while (it.hasNext()) {
                    Map.Entry e = (Map.Entry) it.next();
                    Player obj = (Player) e.getValue();
                    Log.d("MApACt", obj.getCurrentLocation().toString());
                    if (playToMarker.get(obj.getName()) == null) {
                        playToMarker.put(obj.getName(), mMap.addMarker(new MarkerOptions().position(obj.getCurrentLocation()).title(obj.getName() + " is Here")));
                    } else {
                        playToMarker.get(obj.getName()).setPosition(obj.getCurrentLocation());
                    }
                }
            }
        });
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest request = new LocationRequest();
        request.setInterval(100);
        request.setFastestInterval(50);
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

    // I'm leaving this because I was proud of it but it is useless - I believe in you Ross

    private double distance (double playerLat, double playerLong, double flagLat, double flagLong){
        double earthRadius = 3958.75;

        double dLat = Math.toRadians(Math.abs(playerLat-flagLat));
        double dLong = Math.toRadians(Math.abs(playerLong-flagLong));

        double sindLat = Math.sin(dLat / 2);
        double sindLong = Math.sin(dLong / 2);

        double temp = Math.pow(sindLat, 2) + Math.pow(sindLong, 2) * Math.cos(Math.toRadians(playerLat)) * Math.cos(Math.toRadians(flagLat));

        double c = 2 * Math.atan2(Math.sqrt(temp), Math.sqrt(1-temp));

        double dist = earthRadius * c;

        return dist;

    }

    //More Ross Stuff
    //Function returns location to nearest road. pretty cool right
    public Location getRoadLocation () {
        String toPassLat = String.valueOf(lookupLat);
        String toPassLong = String.valueOf(lookupLong);
        String morePassin = "";
        try {
             morePassin = (URLEncoder.encode(toPassLat, "UTF-8") + ","+URLEncoder.encode(toPassLong, "UTF-8"));
        } catch (UnsupportedEncodingException uee){}

        String nrstRoadLookUp = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                morePassin +"&destination="+ morePassin + "&key=AIzaSyAoc3VYDTdgyQ9azplbsy6nhgMOCEUIySo";
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, nrstRoadLookUp, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject location = response.getJSONArray("routes").getJSONObject(0).getJSONObject("bounds").getJSONObject("northeast");
                    double longdit = location.getDouble("lng");
                    double lati = location.getDouble("lat");
                    getLocation(lati, longdit);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, RequestSingleton.getInstance(MapsActivity.this));
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestSingleton.getInstance(MapsActivity.this).addToRequestQueue(objectRequest);
            }
        }).start();
        while(!objectRequest.hasHadResponseDelivered()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return tmpFlagLoca;
    }
    public void setCurrentFlagLocation(Location currentFlagLocation) {
        this.currentFlagLocation = currentFlagLocation;
    }
    private synchronized void getLocation(double lat,double longd){
        tmpFlagLoca = new Location("TEST");
        tmpFlagLoca.setLongitude(longd);
        tmpFlagLoca.setLatitude(lat);
    }
}
