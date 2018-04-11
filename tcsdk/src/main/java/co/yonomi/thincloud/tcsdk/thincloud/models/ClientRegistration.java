package co.yonomi.thincloud.tcsdk.thincloud.models;

import android.os.Build;
import android.provider.Settings;

import java.util.HashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/5/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class ClientRegistration extends BaseResponse {
    private String applicationName;
    private String applicationVersion;
    private final String installId = Settings.Secure.ANDROID_ID;
    private final String devicePlatform = "android";
    private final String deviceModel = getDeviceName();
    private final String deviceVersion = String.valueOf(Build.VERSION.SDK_INT);
    private String deviceToken;
    private HashMap<String,String> metadata = new HashMap<>();


    /** Returns the consumer friendly device name */
    private static String getDeviceName() {
        String mfg = Build.MANUFACTURER, model = Build.MODEL;
        return model.startsWith(mfg) ? model : mfg + " " + model;
    }
}
