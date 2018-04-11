package co.yonomi.thincloud.tcsdk.thincloud;

import android.util.Log;

import java.io.IOException;
import java.util.Calendar;

import co.yonomi.thincloud.tcsdk.ThincloudConfig;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudAuthError;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.AccessToken;
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
     * Get singleton instance of the ThincloudAPI
     * @return Singleton instance of ThincloudAPI
     */
    public static ThincloudAPI getInstance(){
        return instance;
    }

    private Retrofit retrofit;
    private APISpec spec;
    private ThincloudConfig config;
    private AccessToken accessToken;
    private Calendar expiresAt;

    private ThincloudAPI(){}

    /**
     * Setter for Thincloud config object, will attempt to initialize SDK
     * @param _config Configuration object
     * @return this instance of ThincloudAPI for chaining
     */
    public ThincloudAPI setConfig(ThincloudConfig _config){
        config = _config;
        init();
        return this;
    }

    /**
     * Getter for ThincloudConfig object
     * @return this ThincloudConfig object
     */
    public ThincloudConfig getConfig(){
        return config;
    }

    /**
     * Generate baseUrl using configuration
     * @return String the baseUrl generated using provided configuration
     */
    private String baseUrl(){
//        return String.format("https://api-%s.%s.yonomi.cloud/v1/", config.environment(), config.company());
        return String.format("https://api.%s.yonomi.cloud/", config.company());
    }

    /**
     * Attempt to authenticate using provided credentials
     * @param callback will trigger .complete iff success, else completeExceptionally
     * @throws ThincloudException
     */
    private void authenticate(CompletableFuture<AccessToken> callback) throws ThincloudException{
        if(spec == null)
            throw new ThincloudException("Failed to authenticate, spec not initialized");
        //TODO: Handle refresh token flow
        spec.getTokens(
                new TokenRequest()
                        .username(config.username())
                        .password(config.password())
                        .clientId(config.clientId())
        ).enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if(response.code() >= 400){
                    Log.e(TAG, "Authentication failed");
                    callback.completeExceptionally(new ThincloudAuthError("Authentication failed, status " + response.code()));
                } else {
                    accessToken = response.body();
                    expiresAt = Calendar.getInstance();
                    expiresAt.add(Calendar.SECOND, accessToken.expires());
                    callback.complete(accessToken);
                }
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.e(TAG, "Failed to authenticate", t);
                callback.completeExceptionally(t);
            }
        });
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
                if(!original.url().encodedPath().endsWith("/oauth/tokens"))
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
     * Resolve the API specification implementation
     * @param callback TCAPIFuture containing result handlers
     * @throws ThincloudException iff API is not yet initialized
     */
    public void spec(final TCAPIFuture callback) throws ThincloudException{
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
            if(now.compareTo(expiresAt) > 0) {
                authenticate(gotTokens);
            } else {
                callback.complete(spec);
            }
        }
    }
}
