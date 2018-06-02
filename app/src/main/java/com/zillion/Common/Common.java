package com.zillion.Common;

import com.zillion.dost.Remote.IGoogleAPI;
import com.zillion.dost.Remote.RetrofitClient;

public class Common {

    public static final String baseURL = "https://maps.googleapis.com";

    public static IGoogleAPI getGoogleAPI() {

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
