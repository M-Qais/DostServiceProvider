package com.zillion.dost;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zillion.dost.Common.Common;
import com.zillion.dost.Model.FCMResponse;
import com.zillion.dost.Model.Notification;
import com.zillion.dost.Model.Sender;
import com.zillion.dost.Model.Token;
import com.zillion.dost.Remote.IFCMService;
import com.zillion.dost.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustommerCall extends AppCompatActivity {

    TextView txttime, txtDistance, txtAddress;

    MediaPlayer mediaPlayer;
    Button btnAccept,btnDecline;

    IGoogleAPI mService;
    String customerId;
    IFCMService iFCMService;
    double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custommer_call);

        mService = Common.getGoogleAPI();

        txttime = (TextView) findViewById(R.id.txtTime);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtAddress = (TextView) findViewById(R.id.txtAddress);
        btnAccept=(Button)findViewById(R.id.btnAccept);
        btnDecline=(Button)findViewById(R.id.btnDecline);

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        iFCMService = Common.getFCMService();

        if(getIntent() != null)
        {
             lat = getIntent().getDoubleExtra("lat", -1.0);
             lng = getIntent().getDoubleExtra("lng", -1.0);
            customerId=getIntent().getStringExtra("customer");


            getDirection(lat, lng);

        }

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustommerCall.this,DriverTracking.class);

                //send customer location to new activity using put extra :-)
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
//put extraa :-) to send the customer id to next activity,....,...,.,.,.
                intent.putExtra("customerId",customerId);
                startActivity(intent);
                finish();
            }
        });
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId)){

                    cancelBooking(customerId);
                }
            }
        });
    }

    private void cancelBooking(String customerId) {

        Token token = new Token(customerId);
        Notification notification = new Notification("Notice!","Driver has canceled your Request !!!");

        Sender sender = new Sender(token.getToken(),notification);

        iFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success==1)
                        {
                            Toast.makeText(CustommerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void getDirection(double latitude, double longitude) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&" +
                    "origin="+ Common.mLAstLocation.getLatitude()+","+Common.mLAstLocation.getLongitude()+"&"+
                    "destination="+ latitude + "," + longitude +"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.d("chk", requestApi);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //After get routes just get first element of routes

                                JSONObject object = routes.getJSONObject(0);

                                //After get first element, we need get array with name "legs"

                                JSONArray legs = object.getJSONArray("legs");

                                //and get first element of legs array
                                JSONObject legsObject = legs.getJSONObject(0);

                                //Now get distance
                                JSONObject distance = legsObject.getJSONObject("distance");

                                txtDistance.setText(distance.getString("text"));

                                //get time
                                JSONObject duration = legsObject.getJSONObject("duration");

                                txttime.setText(duration.getString("text"));

                                //getAddress

                                String address = legsObject.getString("end_address");

                                txtAddress.setText(address);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustommerCall.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
//        mediaPlayer.setLooping(true);
//        mediaPlayer.start();
    }
}
