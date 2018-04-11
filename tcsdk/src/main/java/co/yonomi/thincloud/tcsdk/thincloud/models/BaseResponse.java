package co.yonomi.thincloud.tcsdk.thincloud.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/4/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class BaseResponse {
    private int statusCode;
    private String message;
    private String description;
    private String createdAt;
    private String updatedAt;
}
