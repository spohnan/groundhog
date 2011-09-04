package com.andyspohn.ijetty.s.services;

import android.content.Context;
import android.hardware.*;
import android.location.Location;
import android.util.Log;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;

import java.util.HashMap;
import java.util.Map;

public class OrientationService extends AbstractService {
    private static final String TAG = "Groundhog::OrientationService";

    // Less than this amount of change will not send an update (reduces noise from jittery readings)
    private final static int REPORTING_SENSITIVITY_IN_DEGREES = 3;

    // Invalid initialized value so we can tell if we're just starting up
    private final static float NO_ORIENTATION_REPORTED_YET = -0.1f;

    SensorManager sensorManager = null;
    Sensor sensor = null;
    ServerChannel orientationChannel = null;

    float orientation = NO_ORIENTATION_REPORTED_YET;
    float lastReportedOrientation = NO_ORIENTATION_REPORTED_YET;

    Location lastLocation = null;

    private float declination;
    private long lastDeclinationUpdate;

    public OrientationService(BayeuxServer bayeuxServer, Context androidContext) {
        // Set up the Comet server channel and save the handle
        super(bayeuxServer, "orientation");
        addService("/orientation", "orientationChange");
        orientationChannel = getBayeux().getChannel("/orientation");

        addService("/location", "locationChange");

        // Set up the listener for the Android orientation sensor (compass)
        sensorManager = (SensorManager) androidContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            orientation = event.values[0] + declination;
            if (orientation < 0.0f) {
                orientation += 360.0f;
            }

            if (orientation > 360.0f) {
                orientation -= 360.0f;
            }

            Log.d(TAG, "orientation change: " + orientation);

            if (isAboveChangeThreshold()) {
                publishMessage();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void publishMessage() {
        maybeUpdateDeclination();
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("orientation", Math.round(orientation));
        orientationChannel.publish(getServerSession(), output, null);
        Log.d(TAG, "orientation change published: " + orientation);
        lastReportedOrientation = orientation;
    }

    public void locationChange(ServerSession remote, Message message) {
        lastLocation = (Location) message.getDataAsMap().get("location");
    }

    private boolean isAboveChangeThreshold() {
        // If we're just starting up then send a value otherwise wait until we have
        // a change greater than or equal to our sensitivity setting f

        //TODO: Detect the type of network access we're using LAN/3G and throttle accordingly
        return (lastReportedOrientation == NO_ORIENTATION_REPORTED_YET) ||
                (Math.max(orientation, lastReportedOrientation) -
                        Math.min(orientation, lastReportedOrientation) >= REPORTING_SENSITIVITY_IN_DEGREES);
    }

    /**
     * Updates known magnetic declination if needed.
     */
    private void maybeUpdateDeclination() {
        if (lastLocation == null) {
            // We still don't know where we are.
            return;
        }

        // Update the declination every hour
        long now = System.currentTimeMillis();
        if (now - lastDeclinationUpdate < 60 * 60 * 1000) {
            return;
        }

        lastDeclinationUpdate = now;
        long timestamp = lastLocation.getTime();

        declination = getDeclinationFor(lastLocation, timestamp);
        Log.i(TAG, "Updated magnetic declination to " + declination);
    }

    private float getDeclinationFor(Location location, long timestamp) {
        GeomagneticField field = new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                timestamp);
        return field.getDeclination();
    }

    public void orientationChange(ServerSession remote, Message message) {
    }

    public void destroy() {
        sensorManager.unregisterListener(mListener);
    }
}
