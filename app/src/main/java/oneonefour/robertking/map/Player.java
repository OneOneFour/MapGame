package oneonefour.robertking.map;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 20/03/2016.
 */
public class Player {
    private LatLng location;
    private List<LatLng> pastLocations;
    private String name;
    private boolean isHost;
    private int lobbyId;
    public Player(LatLng location,String name){
        this.location = location;
        this.name = name;
        pastLocations = new ArrayList<LatLng>();
        lobbyId = Integer.MAX_VALUE;
        isHost = false;
    }
    public boolean getIsHost(){
        return isHost;
    }
    public void setIsHost(boolean isHost){
        this.isHost = isHost;
    }
    public String getName() {
        return name;
    }
    public void setName(String name){this.name = name;}
    public void updateLocation(LatLng newlocation){
        pastLocations.add(location);
        this.location = newlocation;
    }
    public List<LatLng> getPastLocations(){
        return pastLocations;
    }
    public LatLng getCurrentLocation(){
        return location;
    }
    public void setLobbyID(int id){
        lobbyId = id;
    }
    public int getLobbyId(){
        return lobbyId;
    }

}
