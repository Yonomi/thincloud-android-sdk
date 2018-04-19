package co.yonomi.thincloud.tcsdk.thincloud.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Created by mike on 4/4/18.
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class User extends BaseResponse {
    private String email = null;
    @SerializedName("fullName") private String name = null;
    private String firstName = null;
    private String lastName = null;
    private Boolean active = null;
    private HashMap<String,String> custom = null;
    private String userId = null;
}
