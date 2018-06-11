package com.zillion.dost.Service;

import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.zillion.dost.CustommerCall;

public class MyFirebaaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //Beacuse I will send firebase message which contain lat and long from rider app
        //so i need to convert message to latlong
        LatLng customerLocation = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);

        Intent intent = new Intent(getBaseContext(), CustommerCall.class);

        intent.putExtra("lat", customerLocation.latitude);
        intent.putExtra("lng", customerLocation.longitude);
        intent.putExtra("customer", remoteMessage.getNotification().getTitle());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }
}
