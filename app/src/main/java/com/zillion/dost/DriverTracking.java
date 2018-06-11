package com.zillion.dost;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseError;
import com.zillion.dost.Common.Common;
import com.zillion.dost.Helper.DirectionJSONParser;
import com.zillion.dost.Remote.IGoogleAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    double riderLat,riderLng;
    //play services initialization...
    //play service...

    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mlocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    private Circle riderMarker;
    private Marker driverMarker;
    IGoogleAPI mService;
    private com.google.android.gms.maps.model.Polyline direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if(getIntent()!=null)
        {

            //gettting intent data by using get intent functionality and the type is double .. :-)
            riderLat = getIntent().getDoubleExtra("lat",-1.0);
            riderLng = getIntent().getDoubleExtra("lng",-1.0);
        }
        mService = Common.getGoogleAPI();

        setUpLocation();
    }

    private void setUpLocation() {


            if (checkplayservices()) {

                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
    }


    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Common.mLAstLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLAstLocation != null) {


            final double latitude = Common.mLAstLocation.getLatitude();
            final double longitude = Common.mLAstLocation.getLongitude();

            if(driverMarker!=null)

                driverMarker.remove();
                driverMarker =mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title("You")
                .icon(BitmapDescriptorFactory.defaultMarker()));



//                mMap.animateCamera(cam.newLatLngZoom(new LatLng(latitude,longitude),17.0f));.
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));

                if(direction!=null)
                    direction.remove();  //this will remove old direction..
                    getDirection();





        }
          else {
                Log.d("check", "cannot get your location");
                Toast.makeText(this, "Cannot get your Location..", Toast.LENGTH_SHORT).show();


            }
        }
    private void getDirection(){

         LatLng currentPosition = new LatLng(Common.mLAstLocation.getLatitude(), Common.mLAstLocation.getLongitude());
        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&" +
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+riderLat+","+riderLng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.d("chk", requestApi);
            mService.getPath(requestApi)
                    .enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body().toString());


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>

    {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Wait...");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

         return  routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points=null;
            PolylineOptions polylineOptions = null;


            for(int i = 0;i<lists.size();i++)
            {

                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String >> path = lists.get(i);

                for(int j =0;j<path.size();j++){

                    HashMap<String,String> point = path.get(j);
                    double lat =Double.parseDouble(point.get("lat"));
                    double lng =Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat,lng);
                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.color(Color.RED);
                polylineOptions.width(10);
                polylineOptions.geodesic(true);


            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
    private void createLocationRequest() {

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkplayservices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {

                Toast.makeText(this, "This device is not supoorted ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        return true;
    }

    private void startLocationUpdate() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mlocationRequest, this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        riderMarker = mMap.addCircle(new CircleOptions()
        .center(new LatLng(riderLat,riderLng))
                .radius(10)
                .strokeColor(Color.BLUE)
                .fillColor(0*22000)
                .strokeWidth(7.0f)

        );


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLAstLocation = location;
        displayLocation();
    }
}
