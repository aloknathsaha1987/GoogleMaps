package com.aloknath.googlemaps;

import java.io.IOException;
import java.util.List;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity
        implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    private static final float DEFAULTZOOM = 15;
    private static final String LOGTAG = "Maps";

    GoogleMap mMap;
    LocationClient mLocationClient;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
                //mMap.setMyLocationEnabled(true);
                mLocationClient = new LocationClient(this,this,this);
                mLocationClient.connect();

            }
            else {
                Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            setContentView(R.layout.activity_main);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        }
        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        }
        else {
            Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }

    private void gotoLocation(double lat, double lng) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
        mMap.moveCamera(update);
    }

    private void gotoLocation(double lat, double lng,
                              float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    public void geoLocate(View v) throws IOException {


        EditText et = (EditText) findViewById(R.id.editText1);
        String location = et.getText().toString();
        if(location.length() == 0){
            Toast.makeText(this, "Please Enter the Location", Toast.LENGTH_SHORT).show();
            return;
        }

        hideSoftKeyboard(v);

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(location, 1);
        Address add = list.get(0);
        String locality = add.getLocality();
        String country = add.getCountryName();
        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat = add.getLatitude();
        double lng = add.getLongitude();

        gotoLocation(lat, lng, DEFAULTZOOM);

        setMarker(country, locality, lat, lng);

    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.gotoCurrentLocation:
                try {
                    gotoCurrentLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void gotoCurrentLocation() throws IOException {
        Location mLocation = mLocationClient.getLastLocation();
        if(mLocation == null){
            Toast.makeText(this, "My Location is not available", Toast.LENGTH_SHORT).show();
        }else{
            LatLng latLng = new LatLng(mLocation.getLatitude(),mLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,DEFAULTZOOM);
            mMap.animateCamera(cameraUpdate);
            Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(),1);
            Address add = list.get(0);
            String locality = add.getLocality();
            String country = add.getCountryName();
            setMarker(country, locality, mLocation.getLatitude(),mLocation.getLongitude());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MapStateManager mgr = new MapStateManager(this);
        mgr.saveMapState(mMap, marker);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapStateManager mgr = new MapStateManager(this);
        CameraPosition position = mgr.getSavedCameraPosition();
        if (position != null) {
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
            mMap.setMapType(mgr.getSavedMapType());
            marker = mgr.getSavedMarker(mMap);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"Connected to the location services", Toast.LENGTH_SHORT).show();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(30000);
        mLocationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        String msg = "Location" + location.getLatitude() + "," + location.getLongitude();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void setMarker(String country, String locality, double lat, double lng){
        if(marker != null){
            marker.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat, lng))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_CYAN
                ));
        if(country.length() > 0){
            markerOptions.snippet(country);
        }
        marker = mMap.addMarker(markerOptions);
    }
}
