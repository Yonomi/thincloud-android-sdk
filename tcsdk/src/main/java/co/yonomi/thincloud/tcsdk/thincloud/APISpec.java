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

    /**
     * Generate authentication tokens using a {@link TokenRequest}
     * @param tokenRequest
     * @return
     */
    @POST("/v1/oauth/tokens")
    Call<AccessToken> getTokens(@Body TokenRequest tokenRequest);

    /**
     * Get the logged in {@link User}
     * @return
     */
    @GET("/v1/users/@me")
    Call<User> getSelf();

    /**
     * Get information about a {@link Device} by {@link Device#deviceId}
     * @param deviceId
     * @return
     */
    @GET("/v1/devices/{deviceId}")
    Call<Device> getDevice(@Path("deviceId") String deviceId);

    /**
     * Create a new {@link Device}
     * @param device
     * @return
     */
    @POST("/v1/devices")
    Call<Device> createDevice(@Body Device device);

    /**
     * Update a {@link Device} by {@link Device#deviceId}
     * @param deviceId
     * @param device
     * @return
     */
    @PUT("/v1/devices/{deviceId}")
    Call<DeviceResponse> updateDevice(@Path("deviceId") String deviceId, @Body Device device);

    /**
     * Get a {@link User} by {@link User#userId}
     * @param userId
     * @return
     */
    @GET("/v1/users/{userId}")
    Call<User> getUser(@Path("userId") String userId);

    /**
     * Register a new {@link Client} using a {@link ClientRegistration} payload
     * @param registration
     * @return
     */
    @POST("/v1/clients")
    Call<Client> registerClient(@Body ClientRegistration registration);

    /**
     * Get a {@link List} of {@link Command}s associated with a {@link Device#deviceId}
     * @param deviceId
     * @return
     */
    @GET("/v1/devices/{deviceId}/commands?state=pending")
    Call<List<Command>> getCommands(@Path("deviceId") String deviceId);

    /**
     * Update a {@link Command} by a {@link Command#commandId}
     * @param deviceId
     * @param commandId
     * @param command
     * @return
     */
    @PUT("/v1/devices/{deviceId}/commands/{commandId}")
    Call<Command> updateCommand(@Path("deviceId") String deviceId, @Path("commandId") String commandId, @Body Command command);
}
