package com.zillion.dost.Common;

import android.location.Location;

import com.zillion.dost.Model.User;
import com.zillion.dost.Remote.FCMClient;
import com.zillion.dost.Remote.IFCMService;
import com.zillion.dost.Remote.IGoogleAPI;
import com.zillion.dost.Remote.RetrofitClient;

public class Common {

    public static final String token_tbl = "Tokens";
    public static Location mLAstLocation = null;
    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "Users";
    public static final String user_rider_tbl = "Riders";
    public static final String pickup_req_tbl = "PickupRequest";

    public static User currentUser;



    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";

    public static IGoogleAPI getGoogleAPI() {

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService() {

        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
