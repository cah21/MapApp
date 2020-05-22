package com.tnsi.appcomp.mapapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
//import location listner for App
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class GoogleMapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener
{

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    //to get location of moving any object we need to update de location regulary=> use location provider API
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code=99;
    private double latitude, longitude;
    private int proximityRadius=5000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        mapFragment.getMapAsync(this);

    }



    //this method is called when your Map is ready to used
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //get current location
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
       {

            buildGoogleApiClient();
            //to get location of moving any object we need to update de location regulary=> use location provider API
            mMap.setMyLocationEnabled(true);
        }


    }

    //verify permission user
    public  boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code );
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_User_Location_Code );

            }
            //if user click to don't permission
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Request_User_Location_Code:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                        if(googleApiClient == null){
                            //create a new Client
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                    else{
                        Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
        }
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    //this method is call when our location is changed =>ConnectionCallbacks
    @Override
    public void onLocationChanged(Location location) {
        latitude= location.getLatitude();
        longitude=location.getLongitude();

        lastLocation = location;

        if (currentUserLocationMarker != null){
            currentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("user Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentUserLocationMarker= mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));

        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }


    }


//    this method is call when our device is connected => OnConnectionFailedListener
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest= new LocationRequest();
        locationRequest.setInterval(11000);
        locationRequest.setFastestInterval(11000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //to get the current location of user
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    //    this method is call when our device is connected => OnConnectionFailedListener
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //add listner to button
    public void onClick(View v){
        String hospital= "hospital", school="school", restaurant="restaurant";
        Object[] transferData= new Object[2];
        GetNearByPlaces getNearByPlaces=new GetNearByPlaces();
        String url="";
        switch (v.getId()){
            //manage search button: search any place in the map
            case R.id.search_address:
                EditText addressField=(EditText) findViewById(R.id.location_search);
                String address=addressField.getText().toString();
                List<Address> addressList= null;
                MarkerOptions userMarkerOptions=new MarkerOptions();
                if(!TextUtils.isEmpty(address)){
                    Geocoder geocoder= new Geocoder(this);
                    try {
                        addressList=geocoder.getFromLocationName(address,6);
                        if(addressList!=null){
                            for (int i=0;i<addressList.size();i++){
                                Address userAddress= addressList.get(i);
                                LatLng latLng=new LatLng(userAddress.getLatitude(),userAddress.getLongitude());
                                userMarkerOptions.position(latLng);
                                userMarkerOptions.title(address);
                                userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                mMap.addMarker(userMarkerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                            }
                        }else{
                            Toast.makeText(this,"Location not found.....",Toast.LENGTH_SHORT).show();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(this,"Please write any location name......",Toast.LENGTH_SHORT).show();
                }
                break;

                //manage hospital
            case R.id.hospitals_nearby:
                //remove all markers in the map
                mMap.clear();
                url=getUrl(latitude,longitude,hospital);
                transferData[0]=mMap;
                transferData[1]=url;
                getNearByPlaces.execute(transferData);
                Toast.makeText(this,"Searching for nearby hospitals", Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing for nearby hospitals", Toast.LENGTH_SHORT).show();
                break;

            case R.id.schools_nearby:
                //remove all markers in the map
                mMap.clear();
                url=getUrl(latitude,longitude,school);
                transferData[0]=mMap;
                transferData[1]=url;
                getNearByPlaces.execute(transferData);
                Toast.makeText(this,"Searching for nearby schools..", Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing for nearby schools....", Toast.LENGTH_SHORT).show();
                break;

            case R.id.restaurants_nearby:
                //remove all markers in the map
                mMap.clear();
                 url=getUrl(latitude,longitude,restaurant);
                transferData[0]=mMap;
                transferData[1]=url;
                getNearByPlaces.execute(transferData);
                Toast.makeText(this,"Searching for nearby restaurants..", Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing for nearby restaurants....", Toast.LENGTH_SHORT).show();
                break;

            case R.id.pharmacy_nearby:
                //remove all markers in the map
                mMap.clear();
                url=getUrl(latitude,longitude,"pharmacy");
                transferData[0]=mMap;
                transferData[1]=url;
                getNearByPlaces.execute(transferData);
                Toast.makeText(this,"Searching for nearby pharmacies..", Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing for nearby pharmacies....", Toast.LENGTH_SHORT).show();
                break;

            case R.id.supermarket_nearby:
                //remove all markers in the map
                mMap.clear();
                url=getUrl(latitude,longitude,"supermarket");
                transferData[0]=mMap;
                transferData[1]=url;
                getNearByPlaces.execute(transferData);
                Toast.makeText(this,"Searching for nearby supermarkets..", Toast.LENGTH_SHORT).show();
                Toast.makeText(this,"Showing for nearby supermarkets....", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private String getUrl(double latitude,double longitude,String nearByPlace){
        StringBuilder googleURL=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googleURL.append("location="+ latitude+","+longitude);
        googleURL.append("&radius="+ proximityRadius);
        googleURL.append("&type="+nearByPlace);
        googleURL.append("&sensor=true");
        googleURL.append("&key="+ "AIzaSyBdjumlN0P36TqdnLjNSUPGAS-lTSmndkY");

        //show rendu Url
        Log.d("GoogleMapsActivity", "url="+ googleURL.toString());
        return googleURL.toString();
    }
}
