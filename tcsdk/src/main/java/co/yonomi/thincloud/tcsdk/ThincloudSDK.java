package co.yonomi.thincloud.tcsdk;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import co.yonomi.thincloud.tcsdk.cq.CommandHandler;
import co.yonomi.thincloud.tcsdk.cq.CommandQueue;
import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.TCAPIFuture;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.Client;
import co.yonomi.thincloud.tcsdk.thincloud.models.ClientRegistration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mike on 4/4/18.
 */

public class ThincloudSDK {

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
            FirebaseApp.initializeApp(context);
            FirebaseMessaging.getInstance().subscribeToTopic(config.fcmTopic());
        }
        ThincloudAPI.getInstance().setConfig(config);
        getInstance().reportToken(FirebaseInstanceId.getInstance().getToken());
        return _instance;
    }

    /**
     * Determine if singleton is initialized
     * @return true iff initialized, else false
     */
    public static boolean isInitialized(){
        return _instance != null;
    }

    private ThincloudSDK(){}

    /**
     * Attempt to register client token for push notifications,
     * do not call this method manually
     * @param token
     */
    public void reportToken(String token){
        ThincloudConfig config = ThincloudAPI.getInstance().getConfig();
        try {
            ThincloudAPI.getInstance().spec(new TCAPIFuture() {
                @Override
                public boolean complete(APISpec spec) {
                    spec.registerClient(
                            new ClientRegistration()
                                    .applicationName(config.appName())
                                    .applicationVersion(config.appVersion())
                                    .deviceToken(token)
                    ).enqueue(new Callback<Client>() {
                        @Override
                        public void onResponse(Call<Client> call, Response<Client> response) {
                            Log.i(TAG, "Client registered with token " + token);
                        }

                        @Override
                        public void onFailure(Call<Client> call, Throwable t) {
                            Log.e(TAG, "Client registration failed");
                        }
                    });
                    return true;
                }

                @Override
                public boolean completeExceptionally(Throwable e){
                    Log.e(TAG, "Failed to get spec for token report", e);
                    return true;
                }
            });
        } catch(ThincloudException e){
            Log.e(TAG, "Failed to reportToken", e);
        }
    }
}
