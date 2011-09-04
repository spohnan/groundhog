package com.andyspohn.ijetty.s.services;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LocationService extends AbstractService {
    private static final String TAG = "Groundhog::LocationService";
    final Map<String, Location> providerLocations = Collections.synchronizedMap(new HashMap<String, Location>());
    Context androidContext = null;
    ServerChannel locationChannel = null;
    Thread gpsLooper = null;
    Thread networkLooper = null;
    LocationManager locationManager = null;

    public LocationService(BayeuxServer bayeuxServer, Context androidContext) {
        // Set up the Comet server channel and save the handle
        super(bayeuxServer, "location");
        addService("/location", "locationChange");
        locationChannel = getBayeux().getChannel("/location");

        this.androidContext = androidContext;
        locationManager = (LocationManager) androidContext.getSystemService(Context.LOCATION_SERVICE);

        startTrackers();
    }

    private void publishMessage() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("location", getLastLocation());
        locationChannel.publish(getServerSession(), output, null);
    }

    public Location getLastLocation() {
        Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        synchronized (providerLocations) {
            Location gps = providerLocations.get(LocationManager.GPS_PROVIDER);
            Location network = providerLocations.get(LocationManager.NETWORK_PROVIDER);

            //if no existing gps location, or if last known location is more recent
            if (gps == null || (lastGps != null && lastGps.getTime() > gps.getTime()))
                providerLocations.put(LocationManager.GPS_PROVIDER, lastGps);


            if (network == null || (lastNetwork != null && lastNetwork.getTime() > network.getTime()))
                providerLocations.put(LocationManager.NETWORK_PROVIDER, lastNetwork);

            return getLocation();
        }
    }

    public Location getLocation() {
        //Go for finest grained location via GPS first
        Location l;
        synchronized (providerLocations) {
            Location gps = providerLocations.get(LocationManager.GPS_PROVIDER);
            Location network = providerLocations.get(LocationManager.NETWORK_PROVIDER);

            if (gps != null) {
                if (network == null)
                    l = gps;
                else {
                    if (network.getTime() > gps.getTime())
                        l = network; //most recent
                    else
                        l = gps;
                }
            } else
                l = network;
        }
        return l;
    }

    public void startTrackers() {
        synchronized (providerLocations) {
            if (gpsLooper == null) {
                gpsLooper = new LooperThread(LocationManager.GPS_PROVIDER);
            }
            if (networkLooper == null) {
                networkLooper = new LooperThread(LocationManager.NETWORK_PROVIDER);
            }
        }

        if (!gpsLooper.isAlive()) {
            Log.i(TAG, "Starting gps looper thread");
            gpsLooper.start();
            Log.i(TAG, "Started gps looper thread");
        }


        if (!networkLooper.isAlive()) {
            Log.i(TAG, "Starting network looper thread");
            networkLooper.start();
            Log.i(TAG, "Started network looper thread");
        }
    }

    public void stopTrackers() {
        synchronized (providerLocations) {
            if (networkLooper != null) {
                ((LooperThread) networkLooper).quit();
                networkLooper.interrupt();
                networkLooper = null;
            }

            if (gpsLooper != null) {
                ((LooperThread) gpsLooper).quit();
                gpsLooper.interrupt();
                gpsLooper = null;
            }
        }
    }

    public void locationChange(ServerSession remote, Message message) {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("location", getLastLocation());
        locationChannel.publish(getServerSession(), output, null);
    }

    public void destroy() {
        stopTrackers();
    }

    class AsyncLocationListener implements LocationListener {
        String provider = null;

        public AsyncLocationListener(String provider) {
            this.provider = provider;
        }

        public void onLocationChanged(Location location) {
            Log.d(TAG, "location change: " + location);
            providerLocations.put(provider, location);
            publishMessage();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    class LooperThread extends Thread {
        String provider;
        Looper looper;

        public LooperThread(String provider) {
            super();
            this.provider = provider;
            setDaemon(true);
        }

        public void run() {
            Looper.prepare();
            looper = Looper.myLooper();
            LocationManager manager = (LocationManager) androidContext.getSystemService(Context.LOCATION_SERVICE);
            AsyncLocationListener listener = new AsyncLocationListener(provider);
            manager.requestLocationUpdates(provider, 60000L, 0F, listener, Looper.getMainLooper()); //Get an update every 60 secs
            Log.d(TAG, "Requested location updates for " + provider);
            Looper.loop();
        }

        public void quit() {
            looper.quit();
        }
    }
}
