package co.yonomi.thincloud.tcsdk.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.ThincloudSDK;

/**
 * Created by mike on 4/4/18.
 */

public class TCFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "TCFirebaseIIDService";

    @Override
    public void onTokenRefresh(){
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        try {
            ThincloudSDK.getInstance().reportToken(refreshedToken);
        }
        catch(ThincloudException e){
            Log.e(TAG, "Failed to get refreshed token", e);
        }
    }
}
