package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/17/18.
 */

@Data
@Accessors(fluent = true)
public class RefreshTokenRequest {
    public final String grant_type = "refresh_token";
    private String refreshToken = null;
    private String clientId = null;
    private String username = null;
}
