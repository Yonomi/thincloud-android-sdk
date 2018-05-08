package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class ResetPassword {
    private String username;
    private String clientId;
    private String password;
    private String confirmationCode;
}
