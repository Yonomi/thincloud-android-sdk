package co.yonomi.thincloud.tcsdk;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/5/18.
 */

@Data
@Accessors(fluent = true)
public class ThincloudConfig {
    private String instanceName = null;
    private String appName = null;
    private String appVersion = null;
    private String apiKey = null;
    private String fcmTopic = null;
    private String clientId = null;
    private boolean useJobScheduler = false;
    private Set<String> commandsToIgnore = new HashSet<>();

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
}