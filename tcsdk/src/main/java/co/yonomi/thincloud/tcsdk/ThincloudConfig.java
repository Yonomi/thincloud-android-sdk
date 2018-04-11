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
    private String environment = null;
    private String company = null;
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
        return  username != null &&
                password != null &&
                environment != null &&
                appName != null &&
                appVersion != null &&
                apiKey != null &&
                company != null &&
                fcmTopic != null &&
                clientId != null;
    }
}