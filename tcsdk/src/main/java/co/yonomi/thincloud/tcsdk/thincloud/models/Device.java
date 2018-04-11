package co.yonomi.thincloud.tcsdk.thincloud.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/4/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class Device extends BaseResponse {
    private Boolean active = null;
    private String deviceId = null;
    private String devicetypeId = null;
    private String physicalId = null;
    private Location location = null;
    private Boolean commissioning = null;
    private Boolean isConnected = null;
    private String connectivityUpdateAt = null;
    private String connectivitySessionId = null;
    private HashMap<String,String> custom = null;

    public static class Location {
        String type;
        ArrayList<String> coordinates;
    }
}
