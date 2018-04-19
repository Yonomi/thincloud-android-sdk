package co.yonomi.thincloud.tcsdk.thincloud;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import co.yonomi.thincloud.tcsdk.ThincloudConfig;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudAuthError;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.AccessToken;
import co.yonomi.thincloud.tcsdk.thincloud.models.RefreshTokenRequest;
import co.yonomi.thincloud.tcsdk.thincloud.models.TokenRequest;
import java9.util.concurrent.CompletableFuture;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
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
     * Determine if we have a refresh token
     * @return true iff we have a refresh token, else false
     */
    private boolean hasRefreshToken(){
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
        String username = cachedUsername;
        if(username == null)
            username = config.username();
        if(sharedPreferences != null) {
            sharedPreferences.edit()
                    .putString("username", username)
                    .putString("refreshToken", accessToken.refresh())
                    .putLong("expiresAt", expiresAt.getTimeInMillis())
                    .apply();
        }
    }

    /**
     * Determine if we have user credentials
     * @return
     */
    private boolean hasUserCredentials(){
        if(config == null)
            return false;
        return config.hasUserCredentials();
    }

    /**
     * Generate baseUrl using configuration
     * @return String the baseUrl generated using provided configuration
     */
    private String baseUrl(){
        return String.format("https://api.%s.yonomi.cloud/", config.instanceName());
    }

    /**
     * Attempt to authenticate using provided credentials
     * @param callback will trigger .complete iff success, else completeExceptionally
     * @throws ThincloudException
     */
    private void authenticate(final CompletableFuture<AccessToken> callback) throws ThincloudException{
        if(spec == null)
            throw new ThincloudException("Failed to authenticate, spec not initialized");
        tryLoadCachedData();

        final AccessToken previousAccessToken = getAccessToken();
        ThincloudResponse<AccessToken> handler = new ThincloudResponse<AccessToken>() {
            @Override
            public void handle(Call<AccessToken> call, Response<AccessToken> response, Throwable error) {
                if (error != null) {
                    callback.completeExceptionally(error);
                } else {
                    if (response.code() >= 400) {
                        callback.completeExceptionally(new ThincloudAuthError("Bad status"));
                    } else {
                        AccessToken updatedAccessToken = response.body();
                        if(updatedAccessToken != null) {
                            if (previousAccessToken != null && previousAccessToken.refresh() != null)
                                updatedAccessToken.refresh(previousAccessToken.refresh());
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
            String refreshUsername = cachedUsername;
            if(cachedUsername == null && config.username() != null)
                refreshUsername = config.username();

            RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest()
                    .username(refreshUsername)
                    .clientId(config.clientId())
                    .refreshToken(accessToken.refresh());
            call = spec.refreshToken(refreshTokenRequest);
        }
        else if(hasUserCredentials()) {
            Log.i(TAG, "Authenticating using username and password.");
            TokenRequest tokenRequest = new TokenRequest()
                    .username(config.username())
                    .password(config.password())
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
        CompletableFuture<AccessToken> gotTokens = new CompletableFuture<AccessToken>(){
            @Override
            public boolean complete(AccessToken token) {
                return callback.complete(spec);
            }

            @Override
            public boolean completeExceptionally(Throwable e){
                return callback.completeExceptionally(e);
            }
        };
        if(accessToken == null) {
            authenticate(gotTokens);
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
