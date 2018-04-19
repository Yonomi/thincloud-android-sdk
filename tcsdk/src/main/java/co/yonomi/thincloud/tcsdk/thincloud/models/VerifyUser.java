package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class VerifyUser {
    private String username = null;
    private String confirmationCode = null;
}
