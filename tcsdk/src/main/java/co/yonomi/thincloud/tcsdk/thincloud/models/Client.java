package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/5/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class Client extends ClientRegistration {
    private String clientId;
}
