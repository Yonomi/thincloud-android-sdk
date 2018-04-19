package co.yonomi.thincloud.tcsdk;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/5/18.
 */

@Data
@Accessors(fluent = true)
public class ThincloudConfig {
    private String username = null;
    private String password = null;
    private String instanceName = null;
    private String appName = null;
    private String appVersion = null;
    private String apiKey = null;
    private String fcmTopic = null;
    private String clientId = null;

    /**
     * Validate this configuration
     * @return true iff all values are not null
     */
    public boolean validate(){
        return
                appName != null &&
                appVersion != null &&
                apiKey != null &&
                instanceName != null &&
                fcmTopic != null &&
                clientId != null;
    }


    /**
     * Validate whether user credentials are present
     * @return
     */
    public boolean hasUserCredentials(){
        return username != null && password != null;
    }
}