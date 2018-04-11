package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/6/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class DeviceResponse extends Device {
    private Boolean active;
}
