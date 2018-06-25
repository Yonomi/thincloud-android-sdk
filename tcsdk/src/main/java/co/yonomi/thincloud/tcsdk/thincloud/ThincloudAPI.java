package co.yonomi.thincloud.tcsdk.thincloud;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import co.yonomi.thincloud.tcsdk.ThincloudConfig;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudAuthError;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudUnauthorizedException;
import co.yonomi.thincloud.tcsdk.thincloud.models.AccessToken;
import co.yonomi.thincloud.tcsdk.thincloud.models.BaseResponse;
import co.yonomi.thincloud.tcsdk.thincloud.models.RefreshTokenRequest;
import co.yonomi.thincloud.tcsdk.thincloud.models.TokenRequest;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mike on 4/4/18.
 */

public class ThincloudAPI {

    private static final String TAG = "ThincloudAPI";

    private static ThincloudAPI instance = new ThincloudAPI();

    /**
     * Get singleton instance of the {@link ThincloudAPI}
     * @return Singleton instance of {@link ThincloudAPI}
     */
    public static ThincloudAPI getInstance(){
        return instance;
    }

    private Retrofit retrofit;
    private APISpec spec;
    private ThincloudConfig config;
    private AccessToken accessToken;
    private Calendar expiresAt;
    private String cachedUsername;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();

    private ThincloudAPI(){}

    /**
     * Setter for {@link ThincloudConfig} object, will attempt to initialize SDK
     * @param _config Configuration object
     * @return this instance of {@link ThincloudAPI} for chaining
     */
    public ThincloudAPI setConfig(ThincloudConfig _config){
        config = _config;
        init();
        return this;
    }

    /**
     * Setter for {@link SharedPreferences}
     * @param preferences
     * @return this instance of {@link ThincloudAPI}
     */
    public ThincloudAPI setSharedPreferences(SharedPreferences preferences){
        sharedPreferences = preferences;
        return this;
    }

    /**
     * Attempt to load cached data from {@link SharedPreferences}
     */
    private void tryLoadCachedData(){
        if(sharedPreferences != null){
            String refreshToken = sharedPreferences.getString("refreshToken", null);
            if(refreshToken != null)
                setRefreshToken(refreshToken);
            long expiration = sharedPreferences.getLong("expiresAt", Long.MIN_VALUE);
            if(expiration != Long.MIN_VALUE) {
                expiresAt = Calendar.getInstance();
                expiresAt.setTimeInMillis(expiration);
            }
            String username = sharedPreferences.getString("username", null);
            if(username != null){
                cachedUsername = username;
            }
        }
    }

    /**
     * Attempt to clear stored cached information
     */
    private void tryDeleteCachedData(){
        if(sharedPreferences != null){
            sharedPreferences.edit()
                    .remove("username")
                    .remove("refreshToken")
                    .remove("expiresAt")
                    .apply();
            accessToken = null;
            cachedUsername = null;
            expiresAt = null;
            Log.i(TAG, "Deleted cached data");
        } else Log.e(TAG, "Failed to delete cached data, sharedPreferences does not exist.");
    }

    /**
     * Determine if we have a refresh token
     * @return true iff we have a refresh token, else false
     */
    public boolean hasRefreshToken(){
        return (accessToken != null && accessToken.refresh() != null);
    }

    /**
     * Getter for ThincloudConfig object
     * @return this ThincloudConfig object
     */
    public ThincloudConfig getConfig(){
        return config;
    }

    /**
     * Getter for AccessToken object
     * @return AccessToken object, null if not yet set
     */
    public AccessToken getAccessToken(){
        return accessToken;
    }

    /**
     * Attempt to set refresh token, create access token object if it does not exist
     * @param refreshToken
     */
    public synchronized void setRefreshToken(String refreshToken){
        if(accessToken == null)
            accessToken = new AccessToken();
        accessToken.refresh(refreshToken);
    }

    /**
     * Set the {@link AccessToken} and attempt to save it and define expiration properties
     * @param _token
     */
    private synchronized void setAccessToken(AccessToken _token){
        accessToken = _token;
        expiresAt = Calendar.getInstance();
        expiresAt.add(Calendar.SECOND, accessToken.expires());
        if(sharedPreferences != null) {
            sharedPreferences.edit()
                    .putString("username", cachedUsername)
                    .putString("refreshToken", accessToken.refresh())
                    .putLong("expiresAt", expiresAt.getTimeInMillis())
                    .apply();
        }
    }

    /**
     * Attempt to login using username and password
     * @param username
     * @param password
     * @param callback
     * @throws ThincloudException
     */
    public void login(final String username, final String password, final TCFuture<Boolean> callback) throws ThincloudException {
        TCFuture<AccessToken> gotTokens = new TCFuture<AccessToken>(){
            @Override
            public boolean complete(AccessToken token) {
                return callback.complete(true);
            }

            @Override
            public boolean completeExceptionally(Throwable e){
                return callback.completeExceptionally(e);
            }
        };
        authenticate(gotTokens, username, password);
    }

    /**
     * Attempt to logout, deregistering client and clearing cache
     * @throws ThincloudException
     */
    public void logout() throws ThincloudException{
        invalidateCache();
    }

    /**
     * Attempt to grab cached clientId if present
     * @return
     * @throws ThincloudException if clientId is not present in sharedPreferences
     */
    public String getClientId() throws ThincloudException{
        if(sharedPreferences != null){
            String clientId = sharedPreferences.getString("clientId", null);
            if(clientId == null)
                throw new ThincloudException("ClientId does not exist");
            else return clientId;
        } else throw new ThincloudException("Cannot get clientId, sharedPreferences is null");
    }

    /**
     * Attempt to delete all token caches and delete client
     */
    private void invalidateCache() throws ThincloudException{
        if(spec == null)
            throw new ThincloudException("Failed to invalidate cache, spec not initialized.");
        try{
            ThincloudResponse<BaseResponse> deleteResposne = new ThincloudResponse<BaseResponse>() {
                @Override
                public void handle(Call<BaseResponse> call, Response<BaseResponse> response, Throwable error) {
                    if(error != null){
                        Log.e(TAG, "Failed to delete client", error);
                    } else if(response.code() != 204)
                        Log.e(TAG, "Failed to delete client, error code: " + response.code());
                    else
                        Log.i(TAG, "Client deleted successfully");
                    tryDeleteCachedData();
                }
            };
            new ThincloudRequest<BaseResponse>().create(spec.deleteClient(getClientId()), deleteResposne);
        } catch(ThincloudException e){
            Log.e(TAG, "Failed to delete client for invalidateCache", e);
            tryDeleteCachedData();
            throw e;
        }
    }

    /**
     * Generate baseUrl using configuration
     * @return String the baseUrl generated using provided configuration
     */
    private String baseUrl(){
        return String.format("https://api.%s.yonomi.cloud/", config.instanceName());
    }

    /**
     * Login without provided username and password
     * @param callback
     * @throws ThincloudException
     */
    private void authenticate(final TCFuture<AccessToken> callback) throws ThincloudException{
        authenticate(callback, null, null);
    }

    /**
     * Attempt to authenticate using provided credentials
     * @param callback will trigger .complete iff success, else completeExceptionally
     * @throws ThincloudException
     */
    private void authenticate(final TCFuture<AccessToken> callback, final String username, final String password) throws ThincloudException{
        if(spec == null)
            throw new ThincloudException("Failed to authenticate, spec not initialized");
        tryLoadCachedData();

        boolean authUsingUserCredentials = false;

        if(username != null && password != null)
            authUsingUserCredentials = true;

        final AccessToken previousAccessToken = getAccessToken();
        ThincloudResponse<AccessToken> handler = new ThincloudResponse<AccessToken>() {
            @Override
            public void handle(Call<AccessToken> call, Response<AccessToken> response, Throwable error) {
                if (error != null) {
                    callback.completeExceptionally(error);
                } else {
                    if (response.code() >= 400) {
                        Exception exception;
                        try {
                            BaseResponse errorResponse = gson.fromJson(response.errorBody().charStream(), BaseResponse.class);
                            if (errorResponse.message().contains("NotAuthorizedException"))
                                exception = new ThincloudUnauthorizedException(errorResponse.error());
                            else
                                exception = new ThincloudAuthError("Bad status: " + response.code());
                        } catch(NullPointerException e){
                            exception = e;
                        }
                        callback.completeExceptionally(exception);
                    } else {
                        AccessToken updatedAccessToken = response.body();
                        if(updatedAccessToken != null) {
                            if (previousAccessToken != null && previousAccessToken.refresh() != null)
                                updatedAccessToken.refresh(previousAccessToken.refresh());
                            if(username != null)
                                cachedUsername = username;
                            setAccessToken(updatedAccessToken);
                            callback.complete(response.body());
                        } else {
                            callback.completeExceptionally(new ThincloudAuthError("AccessToken null"));
                        }
                    }
                }
            }
        };

        Call<AccessToken> call;

        if(hasRefreshToken()){
            Log.i(TAG, "Authenticating using refresh token.");
            if(cachedUsername == null){
                throw new ThincloudAuthError("Cannot use cached credentials, username missing.");
            }

            RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest()
                    .username(cachedUsername)
                    .clientId(config.clientId())
                    .refreshToken(accessToken.refresh());
            call = spec.refreshToken(refreshTokenRequest);
        }
        else if(authUsingUserCredentials) {
            Log.i(TAG, "Authenticating using username and password.");
            TokenRequest tokenRequest = new TokenRequest()
                    .username(username)
                    .password(password)
                    .clientId(config.clientId());

            call = spec.getTokens(tokenRequest);
        }
        else {
            ThincloudAuthError error = new ThincloudAuthError("No credentials found");
            Log.e(TAG, "Authentication failed, no valid credentials found.", error);
            callback.completeExceptionally(error);
            return;
        }
        new ThincloudRequest<AccessToken>().createWithoutAuth(call, handler);
    }

    /**
     * Initialize the ThincloudAPI handler
     */
    private void init(){
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(5L, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30L, TimeUnit.SECONDS);
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder modified = original.newBuilder()
                        .header("x-api-key", config.apiKey());
                if(!original.url().encodedPath().endsWith("/oauth/tokens") && accessToken != null)
                    modified.header("Authorization", accessToken.bearer());
                return chain.proceed(modified.build());
            }
        });
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();
        spec = retrofit.create(APISpec.class);
    }

    /**
     * Get {@link APISpec}
     * @return {@link #spec}
     */
    public APISpec getSpec(){
        return spec;
    }

    /**
     * Resolve the API specification implementation
     * @param callback TCAPIFuture containing result handlers
     * @throws ThincloudException iff API is not yet initialized
     */
    protected void getAuthenticatedScope(final TCAPIFuture callback) throws ThincloudException{
        if(spec == null)
            throw new ThincloudException("Configuration missing, API not setup");
        TCFuture<AccessToken> gotTokens = new TCFuture<AccessToken>(){
            @Override
            public boolean complete(AccessToken token) {
                return callback.complete(spec);
            }

            @Override
            public boolean completeExceptionally(Throwable e){
                return callback.completeExceptionally(e);
            }
        };
        tryLoadCachedData();
        if(accessToken == null) {
            throw new ThincloudAuthError("Cannot get authenticated scope, user not logged in.");
        } else {
            Calendar now = Calendar.getInstance();
            if(now.compareTo(expiresAt) > 0 || accessToken.access() == null) {
                authenticate(gotTokens);
            } else {
                callback.complete(spec);
            }
        }
    }
}
