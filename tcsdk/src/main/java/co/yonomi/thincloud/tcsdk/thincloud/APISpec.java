package co.yonomi.thincloud.tcsdk.thincloud;

import java.util.List;

import co.yonomi.thincloud.tcsdk.thincloud.models.AccessToken;
import co.yonomi.thincloud.tcsdk.thincloud.models.BaseResponse;
import co.yonomi.thincloud.tcsdk.thincloud.models.Client;
import co.yonomi.thincloud.tcsdk.thincloud.models.ClientRegistration;
import co.yonomi.thincloud.tcsdk.thincloud.models.Command;
import co.yonomi.thincloud.tcsdk.thincloud.models.Device;
import co.yonomi.thincloud.tcsdk.thincloud.models.DeviceResponse;
import co.yonomi.thincloud.tcsdk.thincloud.models.RefreshTokenRequest;
import co.yonomi.thincloud.tcsdk.thincloud.models.ResetPassword;
import co.yonomi.thincloud.tcsdk.thincloud.models.TokenRequest;
import co.yonomi.thincloud.tcsdk.thincloud.models.User;
import co.yonomi.thincloud.tcsdk.thincloud.models.VerifyUser;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by mike on 4/4/18.
 */

public interface APISpec {

    //region Authentication

    /**
     * Generate authentication tokens using a {@link TokenRequest}
     * @param tokenRequest
     * @return
     */
    @POST("/v1/oauth/tokens")
    Call<AccessToken> getTokens(@Body TokenRequest tokenRequest);

    /**
     * Generate new authentication tokens using a {@link RefreshTokenRequest}
     * @param refreshTokenRequest
     * @return
     */
    @POST("/v1/oauth/tokens")
    Call<AccessToken> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    //endregion

    //region User

    /**
     * Create a user
     * @param user
     * @return
     */
    @POST("/v1/users")
    Call<User> createUser(@Body User user);

    /**
     * Verify user
     * @param verifyUser
     * @return
     */
    @POST("/v1/users/verification")
    Call<BaseResponse> verifyUser(@Body VerifyUser verifyUser);

    /**
     * Get the logged in {@link User}
     * @return
     */
    @GET("/v1/users/@me")
    Call<User> getSelf();

    /**
     * Get a {@link User} by {@link User#userId}
     * @param userId
     * @return
     */
    @GET("/v1/users/{userId}")
    Call<User> getUser(@Path("userId") String userId);


    /**
     * Request password reset for user. Called without authentication.
     * Only needs the following parameters:
     * - {@link ResetPassword#username}
     * - {@link ResetPassword#clientId}
     *
     * @param resetPassword
     * @return
     */
    @POST("/v1/users/reset_password")
    Call<BaseResponse> resetPassword(@Body ResetPassword resetPassword);


    /**
     * Verify reset password.
     * Only needs the following parameters:
     * - {@link ResetPassword#username}
     * - {@link ResetPassword#password}
     * - {@link ResetPassword#confirmationCode}
     *
     * @param resetPassword
     * @return
     */
    @POST("/v1/users/reset_password/verification")
    Call<BaseResponse> verifyResetPassword(@Body ResetPassword resetPassword);


    /**
     * Update a user
     * @param userId
     * @param user
     * @return
     */
    @PUT("/v1/users/{userId}")
    Call<User> updateUser(@Path("userId") String userId, @Body User user);


    //endregion

    //region Devices

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
     * Get list of user's devices {@link Device}
     * @return
     */
    @GET("/v1/devices")
    Call<List<Device>> getDevices();

    /**
     * Update a {@link Device} by {@link Device#deviceId}
     * @param deviceId
     * @param device
     * @return
     */
    @PUT("/v1/devices/{deviceId}")
    Call<DeviceResponse> updateDevice(@Path("deviceId") String deviceId, @Body Device device);

    //endregion

    //region Clients

    /**
     * Register a new {@link Client} using a {@link ClientRegistration} payload
     * @param registration
     * @return
     */
    @POST("/v1/clients")
    Call<Client> registerClient(@Body ClientRegistration registration);

    /**
     * Delete a {@link Client} using a ClientId
     * @param clientId
     * @return
     */
    @DELETE("/v1/clients/{clientId}")
    Call<BaseResponse> deleteClient(@Path("clientId") String clientId);

    //endregion

    //region Commands

    /**
     * Get a {@link List} of {@link Command}s associated with a {@link Device#deviceId}
     * @param deviceId
     * @return
     */
    @GET("/v1/devices/{deviceId}/commands?state=pending")
    Call<List<Command>> getCommands(@Path("deviceId") String deviceId);

    /**
     * Create an async command for deviceId
     * @param deviceId
     * @param command
     * @return
     */
    @POST("/v1/devices/{deviceId}/commands?async=true")
    Call<Command> createCommand(@Path("deviceId") String deviceId, @Body Command command);

    /**
     * Update a {@link Command} by a {@link Command#commandId}
     * @param deviceId
     * @param commandId
     * @param command
     * @return
     */
    @PUT("/v1/devices/{deviceId}/commands/{commandId}")
    Call<Command> updateCommand(
            @Path("deviceId") String deviceId,
            @Path("commandId") String commandId,
            @Body Command command);

    //endregion
}
