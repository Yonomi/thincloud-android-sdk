package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/4/18.
 */

@Data
@Accessors(fluent = true)
public class TokenRequest {

    private final String grant_type = "password";

    private String clientId = null;
    private String username = null;
    private String password = null;
}
