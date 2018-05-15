package co.yonomi.thincloud.tcsdktestplatform.example.lights.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode
public class State {
    private Boolean power;
    private Integer hue;
    private Integer saturation;
    private Integer brightness;
    private Integer temperature;

    // Metadata commonly used by integrations
    private String name;
    private String firmwareVersion;
    private String manufacturerInfo;
}
