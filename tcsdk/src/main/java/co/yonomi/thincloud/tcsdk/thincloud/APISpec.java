package co.yonomi.thincloud.tcsdk.thincloud;

import java.util.List;

import co.yonomi.thincloud.tcsdk.thincloud.models.AccessToken;
import co.yonomi.thincloud.tcsdk.thincloud.models.Client;
import co.yonomi.thincloud.tcsdk.thincloud.models.ClientRegistration;
import co.yonomi.thincloud.tcsdk.thincloud.models.Command;
import co.yonomi.thincloud.tcsdk.thincloud.models.Device;
import co.yonomi.thincloud.tcsdk.thincloud.models.DeviceResponse;
import co.yonomi.thincloud.tcsdk.thincloud.models.TokenRequest;
import co.yonomi.thincloud.tcsdk.thincloud.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by mike on 4/4/18.
 */

public interface APISpec {

    @POST("/v1/oauth/tokens")
    Call<AccessToken> getTokens(@Body TokenRequest tokenRequest);

    @GET("/v1/users/@me")
    Call<User> getSelf();

    @GET("/v1/devices/{deviceId}")
    Call<Device> getDevice(@Path("deviceId") String deviceId);

    @POST("/v1/devices")
    Call<Device> createDevice(@Body Device device);

    @PUT("/v1/devices/{deviceId}")
    Call<DeviceResponse> updateDevice(@Path("deviceId") String deviceId, @Body Device device);

    @GET("/v1/users/{userId}")
    Call<User> getUser(@Path("userId") String userId);

    @POST("/v1/clients")
    Call<Client> registerClient(@Body ClientRegistration registration);

    @GET("/v1/devices/{deviceId}/commands?state=pending")
    Call<List<Command>> getCommands(@Path("deviceId") String deviceId);

    @PUT("/v1/devices/{deviceId}/commands/{commandId}")
    Call<Command> updateCommand(@Path("deviceId") String deviceId, @Path("commandId") String commandId, @Body Command command);
}
