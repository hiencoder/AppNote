package com.example.gmo.notesapp.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by GMO on 4/9/2018.
 */

public class User extends BaseResponse{
    @SerializedName("api_key")
    String apiKey;

    public String getApiKey(){
        return apiKey;
    }
}
