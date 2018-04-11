package co.yonomi.thincloud.tcsdk.thincloud.models;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/4/18.
 */

@Data
@Accessors(fluent = true)
public class AccessToken {

    @SerializedName("access_token") String access;
    @SerializedName("refresh_token") String refresh;
    @SerializedName("id_token") String id;
    @SerializedName("token_type") String type;
    @SerializedName("expires_in") Integer expires;

    public String bearer(){
        return "Bearer " + access;
    }
}
