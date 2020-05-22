package com.tnsi.appcomp.mapapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    //fonction pour obtenir une place
    private HashMap<String,String> getSingleNearByPlace(JSONObject googlePlaceJSON){
        HashMap<String,String>googlePlaceMap=new HashMap<>();
        String nameOfPlaces="-NA-";
        String vicinity="-NA-"; //voisinage, alentour
        String latitude="-NA-";
        String longitude="-NA-";
        String reference="-NA-";
        try {
            //fetching data
            if(!googlePlaceJSON.isNull("name")){
                nameOfPlaces=googlePlaceJSON.getString("name");
            }
            if(!googlePlaceJSON.isNull("vicinity")){
                vicinity=googlePlaceJSON.getString("vicinity");
            }
            latitude=googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude=googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference=googlePlaceJSON.getString("reference");


            //now we're gone put data in our Hashmap

            googlePlaceMap.put("place_name",nameOfPlaces);
            googlePlaceMap.put("vicinity",vicinity);
            googlePlaceMap.put("lat",latitude);
            googlePlaceMap.put("lng",longitude);
            googlePlaceMap.put("reference",reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;
    }

    //fonction pour avoir tous les places du voisinage
    private List<HashMap<String,String>>getAllNearByPlaces(JSONArray jsonArray){
        int counter=jsonArray.length();
        List<HashMap<String,String>> nearByPlacesList=new ArrayList<>();
        HashMap<String,String> nearByPlaceMap=null;

        for(int i=0;i<counter;i++){
            try {
                nearByPlaceMap=getSingleNearByPlace((JSONObject)jsonArray.get(i));
                nearByPlacesList.add(nearByPlaceMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return nearByPlacesList;
    }
    //parse data
    public List<HashMap<String,String>> parse(String jsonData){
        JSONArray jsonArray=null;
        JSONObject jsonObject;

        try {
            jsonObject=new JSONObject(jsonData);
            jsonArray=jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getAllNearByPlaces(jsonArray);
    }
}
