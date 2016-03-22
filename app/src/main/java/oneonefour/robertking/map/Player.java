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
    private boolean isMe;
    public Player(LatLng location,String name){
        this.location = location;
        this.name = name;
        pastLocations = new ArrayList<LatLng>();
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

}
