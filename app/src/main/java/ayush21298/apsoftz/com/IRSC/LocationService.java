package ayush21298.apsoftz.com.IRSC;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by vipul on 12/13/2015.
 */
public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

//    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);

    Uri notification;
    Ringtone r;

    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;


    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onConnected(Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
        }
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        distance = 0;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {
        MainActivity.locate.dismiss();
        mCurrentLocation = location;
        if (lStart == null) {
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else
            lEnd = mCurrentLocation;

        //Calling the method below updates the  live values of distance and speed to the TextViews.
        updateUI();
        //calculating the speed with getSpeed method it returns speed in m/s so we are converting it into kmph
        speed = location.getSpeed() * 18 / 5;

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {

        public LocationService getService() {
            return LocationService.this;
        }


    }

    //The live feed of Distance and Speed are being set in the method below .
    private void updateUI() {
        if (MainActivity.p == 0) {
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            distance = distance + (lStart.distanceTo(lEnd) / 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toMinutes(diff);
            MainActivity.time.setText("Total Time: " + diff + " minutes");
            Toast toast = Toast.makeText(this, "DANGER !!!", Toast.LENGTH_SHORT);
            ViewGroup group = (ViewGroup) toast.getView();
            TextView messageTextView = (TextView) group.getChildAt(0);
            messageTextView.setTextSize(32);
            if (speed > 0.0) {
                MainActivity.speed.setText("Current speed: " + new DecimalFormat("#.##").format(speed) + " km/hr");
                MainActivity.speedometer.speedTo((float)speed);
//                r.stop();
                if(speed > 10.0){
//                    new AlertDialog.Builder(this).setTitle("Argh").setMessage("Watch out!").setNeutralButton("Close", null).show();
                    r.play();
                    toast.show();
//                    toast.show();
                }
                else {
                    r.stop();
                }
            }
            else {
                MainActivity.speed.setText(".......");
                MainActivity.speedometer.speedTo((float)(0.0));
//                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Argh").setMessage("Watch out!").setNeutralButton("Close", null).create();
//                dialog.show();


//                MediaPlayer mp = new MediaPlayer();
//                mp.reset();
//                mp.setDataSource(notificationsPath+ (String) apptSounds.getSelectedItem());
//                mp.prepare();
//                mp.start();
//                r.play();
//                toast.show();
                r.stop();
            }

            MainActivity.dist.setText(new DecimalFormat("#.###").format(distance) + " Km's.");

            lStart = lEnd;

        }

    }


    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }
}

