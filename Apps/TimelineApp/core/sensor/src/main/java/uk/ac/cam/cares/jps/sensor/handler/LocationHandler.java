package uk.ac.cam.cares.jps.sensor.handler;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationHandler implements LocationListener {
    private Context context;
    private LocationManager locationManager;
    private JSONArray locationData; // Change to JSONArray
    private long startTime;
    private String sensorName; // Added sensorName field

    public LocationHandler(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationData = new JSONArray();
        this.sensorName = "location";
    }

    public void start() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 800, 0, this);
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        locationManager.removeUpdates(this);
        locationData = new JSONArray();
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            JSONObject locationObject = new JSONObject();
            locationObject.put("name", this.sensorName);
            locationObject.put("time", System.nanoTime());

            JSONObject values = new JSONObject();
            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
            values.put("altitude", location.getAltitude());
            values.put("speed", location.getSpeed());
            values.put("bearing", location.getBearing());
            values.put("horizontalAccuracy", location.hasAccuracy() ? location.getAccuracy() : null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                values.put("bearingAccuracy", location.hasBearingAccuracy() ? location.getBearingAccuracyDegrees() : null);
                values.put("speedAccuracy", location.hasSpeedAccuracy() ? location.getSpeedAccuracyMetersPerSecond() : null);
                values.put("verticalAccuracy", location.hasVerticalAccuracy() ? location.getVerticalAccuracyMeters() : null);
            }

            locationObject.put("values", values);
            locationData.put(locationObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getLocationData() {
        return locationData;
    }

    public void clearLocationData() {
        locationData = new JSONArray();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Not used
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Not used
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Not used
    }
}
