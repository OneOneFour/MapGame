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
    private boolean isReady;
    private boolean hasFlag;
    private int lobbyId;
    public Player(LatLng location,String name){
        pastLocations = new ArrayList<LatLng>();
        this.name= name;
        updateLocation(location);
        lobbyId = Integer.MAX_VALUE;
        isHost = false;
    }
    public Player(LatLng location,String name,int lobbyId,boolean isHost){
        pastLocations = new ArrayList<LatLng>();
        updateLocation(location);
        this.name = name;
        this.lobbyId = lobbyId;
        this.isHost = isHost;
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
        pastLocations.add(newlocation);
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

    public boolean isReady() {
        return isReady;
    }

    public void setIsReady(boolean isReady) {
        this.isReady = isReady;
    }

    public boolean isHasFlag() {
        return hasFlag;
    }

    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }
}
