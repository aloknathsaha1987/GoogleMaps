package com.aloknath.googlemaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.SharedPreferences;

@SuppressWarnings("unused")
public class MapStateManager {
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	private static final String ZOOM = "zoom";
	private static final String BEARING = "bearing";
	private static final String TILT = "tilt";
	private static final String MAPTYPE = "MAPTYPE";
    private static final String MARKER = "marker";

	private static final String PREFS_NAME ="mapCameraState";
    private static final String MARKERLAT = "markerLat" ;
    private static final String MARKERLNG = "markerLng";
    private static final String MARKERTITLE = "markerTitle";

    private SharedPreferences mapStatePrefs;

	public MapStateManager(Context context) {
		mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	public void saveMapState(GoogleMap map, Marker marker) {
		SharedPreferences.Editor editor = mapStatePrefs.edit();
		CameraPosition position = map.getCameraPosition();
		
		editor.putFloat(LATITUDE, (float) position.target.latitude);
		editor.putFloat(LONGITUDE, (float) position.target.longitude);
		editor.putFloat(ZOOM, position.zoom);
		editor.putFloat(TILT, position.tilt);
		editor.putFloat(BEARING, position.bearing);
		editor.putInt(MAPTYPE, map.getMapType());
        editor.putFloat(MARKERLAT, (float) marker.getPosition().latitude);
        editor.putFloat(MARKERLNG, (float) marker.getPosition().longitude);
        editor.putString(MARKERTITLE, marker.getTitle());
		editor.commit();
	}
	
	public CameraPosition getSavedCameraPosition() {
		double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
		if (latitude == 0) {
			return null;
		}
		double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
		LatLng target = new LatLng(latitude, longitude);
		
		float zoom = mapStatePrefs.getFloat(ZOOM, 0);
		float bearing = mapStatePrefs.getFloat(BEARING, 0);
		float tilt = mapStatePrefs.getFloat(TILT, 0);
		
		CameraPosition position = new CameraPosition(target, zoom, tilt, bearing);
		return position;
	}

	public int getSavedMapType() {
		return mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL);
	}

    public Marker getSavedMarker(GoogleMap mMap){
        MarkerOptions markerOptions = new MarkerOptions()
                .title(mapStatePrefs.getString(MARKERTITLE, null))
                .position(new LatLng((double)mapStatePrefs.getFloat(MARKERLAT,0), (double)mapStatePrefs.getFloat(MARKERLNG,0)));

        Marker marker = mMap.addMarker(markerOptions);

        return marker;

    }

}
