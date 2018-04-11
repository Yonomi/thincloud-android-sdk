package co.yonomi.thincloud.tcsdk.cq;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/4/18.
 */

@Data
@Accessors(fluent = true)
public class CommandItem {
    String deviceId;
    Integer pendingCount;

    @Data
    @Accessors(fluent = true)
    public static class LastCommandItem {
        String commandId;
        String issuedBy;
        String createdAt;
    }

}
