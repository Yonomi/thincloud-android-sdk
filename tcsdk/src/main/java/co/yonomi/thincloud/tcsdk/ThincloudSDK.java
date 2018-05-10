package co.yonomi.thincloud.tcsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import co.yonomi.thincloud.tcsdk.cq.CommandHandler;
import co.yonomi.thincloud.tcsdk.cq.CommandQueue;
import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.TCAPIFuture;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudRequest;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudResponse;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.BaseResponse;
import co.yonomi.thincloud.tcsdk.thincloud.models.Client;
import co.yonomi.thincloud.tcsdk.thincloud.models.ClientRegistration;
import co.yonomi.thincloud.tcsdk.util.AndThenDo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mike on 4/4/18.
 */

public class ThincloudSDK {

    private static final String SHARED_PREF_FILE = "THINCLOUD_SDK";

    private static final String TAG = "ThincloudSDK";

    private static ThincloudSDK _instance;

    /**
     * Grab ThincloudSDK singleton if initialized
     * @return ThincloudSDK singleton
     * @throws ThincloudException iff not yet initialized
     */
    public static ThincloudSDK getInstance() throws ThincloudException{
        if(_instance == null){
            throw new ThincloudException("Instance not yet initialized");
        }
        return _instance;
    }

    /**
     * Set event handler for incoming commands
     * @param commandHandler
     */
    public static void setHandler(CommandHandler commandHandler) {
        CommandQueue.getInstance().setHandler(commandHandler);
    }

    /**
     * Grab the Google Play Driver
     * @return
     */
    public static GooglePlayDriver getGooglePlayDriver(){
        return _instance.googlePlayDriver;
    }

    /**
     * Attempt to initialize the SDK
     * @param context Android application context used for initializing Firebase
     * @param config ThincloudConfig object
     * @return this singleton instance of ThincloudSDK
     * @throws ThincloudException if ThincloudConfig fails validation
     */
    public static ThincloudSDK initialize(Context context, ThincloudConfig config) throws ThincloudException {
        if(!config.validate())
            throw new ThincloudException("Failed to initialize. Configuration invalid.");
        if(_instance == null) {
            _instance = new ThincloudSDK();
            _instance.googlePlayDriver = new GooglePlayDriver(context);
            FirebaseApp.initializeApp(context);
            FirebaseMessaging.getInstance().subscribeToTopic(config.fcmTopic());
        }
        getInstance().sharedPreferences = context.getApplicationContext().getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE);
        ThincloudAPI.getInstance()
                .setSharedPreferences(getInstance().sharedPreferences)
                .setConfig(config);
        getInstance().reportToken(FirebaseInstanceId.getInstance().getToken());
        CommandQueue.getInstance().setUseJobScheduler(config.useJobScheduler());
        return _instance;
    }

    /**
     * Determine if singleton is initialized
     * @return true iff initialized, else false
     */
    public static boolean isInitialized(){
        return _instance != null;
    }

    private GooglePlayDriver googlePlayDriver;

    private SharedPreferences sharedPreferences;

    private ThincloudSDK(){}

    /**
     * Attempt to delete existing client, register client token,
     * do not call this method manually!
     *
     * Used for push notification routing.
     * @param token
     */
    public void reportToken(final String token){
        ThincloudConfig config = ThincloudAPI.getInstance().getConfig();
        APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
        if(apiSpec != null){
            ClientRegistration clientRegistration = new ClientRegistration()
                    .applicationName(config.appName())
                    .applicationVersion(config.appVersion())
                    .deviceToken(token);
            ThincloudResponse<Client> createResponse = new ThincloudResponse<Client>() {
                @Override
                public void handle(Call<Client> call, Response<Client> response, Throwable error) {
                    if(error != null)
                        Log.e(TAG, "Client registration failed", error);
                    else {
                        Log.i(TAG, "Client registered with token " + token);

                        if(response.body() != null && sharedPreferences != null) {
                            sharedPreferences.edit()
                                    .putString("clientId", response.body().clientId())
                                    .apply();
                            Log.i(TAG, "Cached clientId " + response.body().clientId());
                        }
                    }
                }
            };
            AndThenDo createClient = new AndThenDo() {
                @Override
                public void something() {
                    new ThincloudRequest<Client>().create(apiSpec.registerClient(clientRegistration), createResponse);
                }
            };
            if(sharedPreferences != null && sharedPreferences.contains("clientId")) {
                ThincloudResponse<BaseResponse> deleteResponse = new ThincloudResponse<BaseResponse>() {
                    @Override
                    public void handle(Call<BaseResponse> call, Response<BaseResponse> response, Throwable error) {
                        if (error != null) {
                            Log.e(TAG, "Client deletion failed", error);
                        } else {
                            if (response.code() == 204) {
                                Log.i(TAG, "Deleted client successfully");
                            } else {
                                Log.e(TAG, "Failed to delete client: " + response.code());
                            }
                        }
                        createClient.something();
                    }
                };
                new ThincloudRequest<BaseResponse>().create(
                        apiSpec.deleteClient(sharedPreferences.getString("clientId", "")),
                        deleteResponse
                );
            } else createClient.something();

        } else {
            Log.e(TAG, "Failed to report token, API not initialized.");
        }
    }
}
