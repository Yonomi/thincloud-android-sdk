package co.yonomi.thincloud.tcsdk.thincloud.models;


import com.google.gson.JsonObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/5/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class Command extends BaseResponse {
    private String deviceId;
    private String commandId;
    private String name;
    private String userId;
    private String state;

    private JsonObject request;
    private JsonObject response;
    private Integer responseStatusCode;


    /**
     * Prepare a new {@link Command} instance stripped of unnecessary fields
     * @return
     */
    public Command respond(){
        return new Command()
                .commandId(commandId())
                .deviceId(deviceId())
                .name(name())
                .userId(userId())
                .state(state())
                .response(response())
                .responseStatusCode(responseStatusCode());
    }
}
